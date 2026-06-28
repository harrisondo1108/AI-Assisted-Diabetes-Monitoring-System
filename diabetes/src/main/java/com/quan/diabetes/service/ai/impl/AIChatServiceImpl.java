package com.quan.diabetes.service.ai.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quan.diabetes.dto.AIChat.*;
import com.quan.diabetes.entity.AIAssistant;
import com.quan.diabetes.entity.AIConversation;
import com.quan.diabetes.entity.AIMessage;
import com.quan.diabetes.entity.Patient;
import com.quan.diabetes.service.ai.AIAssistantService;
import com.quan.diabetes.service.ai.AIChatService;
import com.quan.diabetes.service.ai.*;
import com.quan.diabetes.service.masterdata.PromptTemplateService;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class AIChatServiceImpl implements AIChatService {

    private static final Logger logger = LoggerFactory.getLogger(AIChatServiceImpl.class);

    @Value("${ollama.url:http://localhost:11434}")
    private String ollamaUrl;

    @Value("${ollama.model:diabetesAI}")
    private String defaultModel;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final AIMessageService aiMessageService;
    private final AIConversationService aiConversationService;
    private final AIAssistantService aiAssistantService;
    private final PatientService patientService;
    private final PromptTemplateService promptTemplateService;

    public AIChatServiceImpl(
            RestTemplate restTemplate,
            ObjectMapper objectMapper,
            AIMessageService aiMessageService,
            AIConversationService aiConversationService,
            AIAssistantService aiAssistantService,
            PatientService patientService,
            PromptTemplateService promptTemplateService) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.aiMessageService = aiMessageService;
        this.aiConversationService = aiConversationService;
        this.aiAssistantService = aiAssistantService;
        this.patientService = patientService;
        this.promptTemplateService = promptTemplateService;
    }

    @Override
    public ChatResponseDto sendMessage(ChatRequestDto request) {
        return sendMessageWithAssistant(request, null);
    }

    @Override
    public ChatResponseDto sendMessageWithAssistant(ChatRequestDto request, Integer assistantId) {
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
                modelToUse = defaultModel;
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

            // 5. === SỬ DỤNG GENERATE API ===
            String aiResponse = callOllamaGenerate(request.question(), modelToUse);

            // 6. Save AI response
            AIMessage aiMessage = new AIMessage();
            aiMessage.setContent(aiResponse);
            aiMessage.setSender("AI");
            aiMessage.setTime(LocalDateTime.now());
            aiMessage.setAiConversation(conversation);
            aiMessageService.create(aiMessage);

            // 7. Update topic if first message
            long messageCount = aiMessageService.countByConversationId(conversation.getAiConversationId());
            if (messageCount == 1) {
                String topic = generateTopic(request.question());
                conversation.setTopic(topic);
                aiConversationService.update(conversation.getAiConversationId(), conversation);
            }

            long endTime = System.currentTimeMillis();
            logger.info("Total response time: {} ms", (endTime - startTime));

            return ChatResponseDto.success(conversation.getAiConversationId(), aiResponse);

        } catch (Exception e) {
            logger.error("Error sending message: {}", e.getMessage(), e);
            return ChatResponseDto.error("Failed to process message: " + e.getMessage());
        }
    }

    // ===== SỬ DỤNG GENERATE API =====
    private String callOllamaGenerate(String question, String model) {
        try {
            long startTime = System.currentTimeMillis();

            // Lấy system prompt
            String systemPrompt = getSystemPrompt();

            // GHÉP PROMPT: System Prompt + Câu hỏi (giống như terminal)
            String fullPrompt = systemPrompt + "\n\n" + question;

            // Tạo request cho Generate API
            OllamaGenerateRequest request = new OllamaGenerateRequest(
                    model,
                    fullPrompt,
                    false,
                    new OllamaGenerateRequest.Options(
                            0.5,   // temperature
                            0.85,  // top_p
                            40,    // top_k
                            1.1,   // repeat_penalty
                            2048,  // num_ctx
                            250    // num_predict
                    )
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(
                    objectMapper.writeValueAsString(request),
                    headers
            );

            // Gọi Generate API (NHANH HƠN)
            String url = ollamaUrl + "/api/generate";
            logger.info("Calling Ollama Generate API with model: {}", model);

            String responseJson = restTemplate.postForObject(url, entity, String.class);

            long endTime = System.currentTimeMillis();
            logger.info("Ollama Generate responded in {} ms", (endTime - startTime));

            JsonNode root = objectMapper.readTree(responseJson);
            String content = root.path("response").asText();

            return cleanResponse(content);

        } catch (Exception e) {
            logger.error("Error calling Ollama Generate API: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get response from AI model: " + e.getMessage());
        }
    }

    // ===== CÁC METHODS KHÁC =====

    private AIConversation getOrCreateConversation(ChatRequestDto request, Patient patient, AIAssistant assistant) {
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

    private String getSystemPrompt() {
        return promptTemplateService.getActiveSystemPrompt()
                .orElse("Bạn là trợ lý AI chuyên tư vấn bệnh tiểu đường tại Việt Nam. Trả lời ngắn gọn, đúng trọng tâm, khoảng 5-7 câu. Luôn kết thúc bằng câu hỏi mở và thông tin tham khảo.");
    }

    private String cleanResponse(String response) {
        response = response.replaceAll("```[a-z]*\\n", "");
        response = response.replaceAll("```", "");
        response = response.trim();
        return response;
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