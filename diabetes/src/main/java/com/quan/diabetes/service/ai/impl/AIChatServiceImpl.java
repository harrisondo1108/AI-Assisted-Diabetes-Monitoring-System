package com.quan.diabetes.service.ai.impl;

import com.quan.diabetes.dto.AIChat.*;
import com.quan.diabetes.entity.AIAssistant;
import com.quan.diabetes.entity.AIConversation;
import com.quan.diabetes.entity.AIMessage;
import com.quan.diabetes.entity.Patient;
import com.quan.diabetes.service.ai.AIAssistantService;
import com.quan.diabetes.service.ai.AIChatService;
import com.quan.diabetes.service.ai.*;
import com.quan.diabetes.service.user.PatientService;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.quan.diabetes.dto.AIChat.RAGPythonAiRequest;
import com.quan.diabetes.dto.AIChat.RAGPythonAiResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class AIChatServiceImpl implements AIChatService {

    private static final Logger logger = LoggerFactory.getLogger(AIChatServiceImpl.class);

    @Value("${python.ai.url:http://127.0.0.1:8000/api/ai/chat}")
    private String pythonAiUrl;

    private final RestTemplate restTemplate;
    private final AIMessageService aiMessageService;
    private final AIConversationService aiConversationService;
    private final AIAssistantService aiAssistantService;
    private final PatientService patientService;
    private final com.quan.diabetes.repository.AiRepository aiRepository;

    public AIChatServiceImpl(
            RestTemplate restTemplate,
            AIMessageService aiMessageService,
            AIConversationService aiConversationService,
            AIAssistantService aiAssistantService,
            PatientService patientService,
            com.quan.diabetes.repository.AiRepository aiRepository) {
        this.restTemplate = restTemplate;
        this.aiMessageService = aiMessageService;
        this.aiConversationService = aiConversationService;
        this.aiAssistantService = aiAssistantService;
        this.patientService = patientService;
        this.aiRepository = aiRepository;
    }

    @Override
    public ChatResponseDto sendMessage(AiChatRequestDto request) {
        return sendMessageWithAssistant(request, null);
    }

    @Override
    public ChatResponseDto sendMessageWithAssistant(AiChatRequestDto request, Integer assistantId) {
        try {
            long startTime = System.currentTimeMillis();

            // 1. Get Patient
            Patient patient = patientService.findById(request.patientId())
                    .orElseThrow(() -> new EntityNotFoundException("Patient not found: " + request.patientId()));

            // 2. Get AI Assistant
            AIAssistant assistant;
            if (assistantId != null) {
                assistant = aiAssistantService.findById(assistantId)
                        .orElseGet(() -> {
                            logger.warn("AI Assistant with id {} not found, using default", assistantId);
                            return aiAssistantService.getOrCreateDefaultAssistant();
                        });
            } else {
                assistant = aiAssistantService.getOrCreateDefaultAssistant();
            }

            String modelToUse = assistant.getModelName();
            if (modelToUse == null || modelToUse.isEmpty()) {
                modelToUse = "diabetesAI";
                assistant.setModelName(modelToUse);
                aiAssistantService.update(assistant.getAiAssistantId(), assistant);
            }

            logger.info("Using AI Assistant: {} (ID: {}, Model: {})",
                    assistant.getAiName(), assistant.getAiAssistantId(), modelToUse);

            // 3. Get or create AIConversation
            AIConversation conversation = getOrCreateConversation(request, patient, assistant);
            logger.info("Using conversation: {}", conversation.getAiConversationId());

            // 4. Save user message
            AIMessage userMessage = new AIMessage();
            userMessage.setContent(request.question());
            userMessage.setSender("Patient");
            userMessage.setTime(LocalDateTime.now());
            userMessage.setAiConversation(conversation);
            aiMessageService.create(userMessage);

            // 1. bắt đầu từ câu hỏi của bệnh nhân (RAGAIChatRequest)
            RAGAiChatRequest ragAiChatRequest = new RAGAiChatRequest();
            ragAiChatRequest.setPatientId(request.patientId());
            ragAiChatRequest.setMessage(request.question());

            // 2. call python (trả về RAGAIChatResponse)
            RAGAiChatResponse ragAiChatResponse = callPythonAiFirst(ragAiChatRequest);

            // 3 & 4. backend check status
            String aiResponse = "";
            if (ragAiChatResponse != null && "FINAL_ANSWER".equals(ragAiChatResponse.getStatus())) {
                // Nếu status là "FINAL_ANSWER" thì lấy content tiếng Việt bình thường
                aiResponse = ragAiChatResponse.getContent();
            } else if (ragAiChatResponse != null && "NEED_SQL_DATA".equals(ragAiChatResponse.getStatus())) {
                // Nếu status là "NEED_SQL_DATA" thì convert RAGAIChatRequest thành RAGPythonAiRequest
                RAGPythonAiRequest pyRequest = new RAGPythonAiRequest(
                        ragAiChatRequest.getPatientId(),
                        ragAiChatRequest.getMessage(),
                        ""
                );

                // Trích xuất tool thông tin từ content chuỗi JSON
                try {
                    String toolJson = ragAiChatResponse.getContent();
                    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    Map<String, Object> toolMap = mapper.readValue(toolJson, Map.class);
                    
                    String action = (String) toolMap.get("action");
                    String patientId = (String) toolMap.get("patient_id");

                    logger.info("[RAG] Python yêu cầu truy xuất SQL - action: {}", action);
                    // truy suất dữ liệu từ cơ sở dữ liệu theo tool tương ứng
                    String sqlData = fetchDataFromRepository(action, patientId);

                    // sau đó lưu vào contextData
                    pyRequest.setContextData(sqlData);

                    // 5. dùng RAGPythonAiRequest để gọi python lần 2 sẽ trả về RAGPythonAiResponse
                    RAGPythonAiResponse secondPyResponse = callPythonAiSecond(pyRequest);
                    if (secondPyResponse != null) {
                        aiResponse = secondPyResponse.getContent();
                    } else {
                        aiResponse = "Không nhận được câu trả lời từ AI Server sau khi truy xuất DB.";
                    }
                } catch (Exception e) {
                    logger.error("Lỗi khi xử lý RAG SQL Data: {}", e.getMessage(), e);
                    aiResponse = "Lỗi xử lý dữ liệu RAG: " + e.getMessage();
                }
            } else {
                aiResponse = (ragAiChatResponse != null) ? ragAiChatResponse.getContent() : "Lỗi không xác định từ AI Server.";
            }

            // 6. Lưu vào cơ sở dữ liệu (Convert thành AIMessage thực thể tương ứng)
            AIMessage aiMessage = new AIMessage();
            aiMessage.setContent(aiResponse);
            aiMessage.setSender("AI");
            aiMessage.setTime(LocalDateTime.now());
            aiMessage.setAiConversation(conversation);
            aiMessageService.create(aiMessage);

            // 8. Update topic if first message
            long messageCount = aiMessageService.countByConversationId(conversation.getAiConversationId());
            if (messageCount == 1) {
                String topic = generateTopic(request.question());
                conversation.setTopic(topic);
                aiConversationService.update(conversation.getAiConversationId(), conversation);
            }

            long endTime = System.currentTimeMillis();
            logger.info("Total response time: {} ms", (endTime - startTime));

            // 7. convert và hiển thị message cho người dùng xem
            return ChatResponseDto.success(conversation.getAiConversationId(), aiResponse);

        } catch (Exception e) {
            logger.error("Error sending message: {}", e.getMessage(), e);
            return ChatResponseDto.error("Failed to process message: " + e.getMessage());
        }
    }

    /**
     * Gọi Python AI Server lần 1 (Nhận RAGAiChatRequest và trả về RAGAiChatResponse)
     */
    private RAGAiChatResponse callPythonAiFirst(RAGAiChatRequest ragRequest) {
        try {
            // Convert RAGAiChatRequest thành RAGPythonAiRequest (contextData = "")
            RAGPythonAiRequest pyRequest = new RAGPythonAiRequest(
                    ragRequest.getPatientId(),
                    ragRequest.getMessage(),
                    ""
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<RAGPythonAiRequest> entity = new HttpEntity<>(pyRequest, headers);

            RAGPythonAiResponse pyResponse = restTemplate.postForObject(pythonAiUrl, entity, RAGPythonAiResponse.class);

            RAGAiChatResponse chatResponse = new RAGAiChatResponse();
            if (pyResponse != null) {
                chatResponse.setStatus(pyResponse.getStatus());
                // Gán trực tiếp content nhận được (có thể là tool JSON hoặc câu trả lời tiếng Việt)
                chatResponse.setContent(pyResponse.getContent());
            } else {
                chatResponse.setStatus("ERROR");
                chatResponse.setContent("Không nhận được phản hồi từ AI Server.");
            }
            return chatResponse;
        } catch (Exception e) {
            logger.error("Lỗi khi kết nối Python AI Server lần 1: {}", e.getMessage(), e);
            return new RAGAiChatResponse("ERROR", "Lỗi kết nối Python AI Server lần 1: " + e.getMessage());
        }
    }

    /**
     * Gọi Python AI Server lần 2 (Nhận RAGPythonAiRequest và trả về RAGPythonAiResponse)
     */
    private RAGPythonAiResponse callPythonAiSecond(RAGPythonAiRequest pyRequest) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<RAGPythonAiRequest> entity = new HttpEntity<>(pyRequest, headers);

            return restTemplate.postForObject(pythonAiUrl, entity, RAGPythonAiResponse.class);
        } catch (Exception e) {
            logger.error("Lỗi khi kết nối Python AI Server lần 2: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Dựa vào action từ Python RAG, lấy dữ liệu tương ứng từ AiRepository (DB).
     */
    private String fetchDataFromRepository(String action, String patientId) {
        switch (action) {
            case "get_general_record":
                return aiRepository.getGeneralRecord(patientId);
            case "get_clinical_examination":
                return aiRepository.getClinicalExamination(patientId);
            case "get_treatment_plan":
                return aiRepository.getTreatmentPlan(patientId);
            case "get_lab_results":
                return aiRepository.getLabResults(patientId);
            case "get_prescriptions":
                return aiRepository.getPrescriptions(patientId);
            default:
                logger.warn("Không tìm thấy action RAG: {}", action);
                return null;
        }
    }

    private AIConversation getOrCreateConversation(AiChatRequestDto request, Patient patient, AIAssistant assistant) {
        if (request.conversationId() != null && !request.conversationId().isEmpty()) {
            AIConversation existing = aiConversationService.findById(request.conversationId())
                    .orElseThrow(() -> new EntityNotFoundException("Conversation not found: " + request.conversationId()));
            if (!existing.getPatient().getUserId().equals(patient.getUserId())) {
                throw new IllegalArgumentException("Conversation does not belong to this patient");
            }
            return existing;
        }

        String conversationId = "CONV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        AIConversation conversation = new AIConversation();
        conversation.setAiConversationId(conversationId);
        conversation.setPatient(patient);
        conversation.setAiAssistant(assistant);
        conversation.setCreatedAt(LocalDateTime.now());
        conversation.setTopic("Consultation with " + assistant.getAiName());

        return aiConversationService.create(conversation);
    }

    private String generateTopic(String question) {
        String[] words = question.split(" ");
        if (words.length > 5) {
            return String.join(" ", java.util.Arrays.copyOf(words, 5)) + "...";
        }
        return question;
    }

    @Override
    public ConversationHistoryDto getConversationHistory(String conversationId) {
        List<AIMessage> messages = aiMessageService.findByConversationId(conversationId);

        List<ConversationHistoryDto.MessageItem> messageItems = messages.stream()
                .map(msg -> new ConversationHistoryDto.MessageItem(
                        msg.getSender().equals("AI") ? "AI" : "User",
                        msg.getContent(),
                        msg.getTime()
                ))
                .collect(Collectors.toList());

        return new ConversationHistoryDto(conversationId, messageItems);
    }

    @Override
    public List<ConversationHistoryDto> getPatientConversations(String patientId) {
        List<AIConversation> conversations = aiConversationService.findByPatientId(patientId);

        return conversations.stream()
                .map(conv -> {
                    List<AIMessage> messages = aiMessageService.findByConversationId(conv.getAiConversationId());
                    List<ConversationHistoryDto.MessageItem> messageItems = messages.stream()
                            .map(msg -> new ConversationHistoryDto.MessageItem(
                                    msg.getSender().equals("AI") ? "AI" : "User",
                                    msg.getContent(),
                                    msg.getTime()
                            ))
                            .collect(Collectors.toList());
                    return new ConversationHistoryDto(conv.getAiConversationId(), messageItems);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<ConversationHistoryDto> getPatientConversationsWithAssistant(String patientId, Integer assistantId) {
        List<AIConversation> conversations = aiConversationService.findByPatientIdAndAssistantId(patientId, assistantId);

        return conversations.stream()
                .map(conv -> {
                    List<AIMessage> messages = aiMessageService.findByConversationId(conv.getAiConversationId());
                    List<ConversationHistoryDto.MessageItem> messageItems = messages.stream()
                            .map(msg -> new ConversationHistoryDto.MessageItem(
                                    msg.getSender().equals("AI") ? "AI" : "User",
                                    msg.getContent(),
                                    msg.getTime()
                            ))
                            .collect(Collectors.toList());
                    return new ConversationHistoryDto(conv.getAiConversationId(), messageItems);
                })
                .collect(Collectors.toList());
    }

    @Override
    public void deleteConversation(String conversationId) {
        if (!aiConversationService.existsById(conversationId)) {
            throw new EntityNotFoundException("Conversation not found: " + conversationId);
        }
        aiMessageService.deleteByConversationId(conversationId);
        aiConversationService.deleteById(conversationId);
        logger.info("Deleted conversation: {}", conversationId);
    }

    @Override
    public List<AIAssistantDto> getAvailableAssistants() {
        List<AIAssistant> assistants = aiAssistantService.findAll();
        if (assistants.isEmpty()) {
            AIAssistant defaultAssistant = aiAssistantService.getOrCreateDefaultAssistant();
            assistants = List.of(defaultAssistant);
        }
        return assistants.stream()
                .filter(a -> "Active".equalsIgnoreCase(a.getStatus()))
                .map(a -> new AIAssistantDto(
                        a.getAiAssistantId(),
                        a.getAiName(),
                        a.getStatus(),
                        a.getModelName()
                ))
                .collect(Collectors.toList());
    }
}