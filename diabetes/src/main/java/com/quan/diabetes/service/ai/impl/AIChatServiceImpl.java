package com.quan.diabetes.service.ai.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.quan.diabetes.dto.AIChat.AIAssistantDto;
import com.quan.diabetes.dto.AIChat.AiChatRequestDto;
import com.quan.diabetes.dto.AIChat.ChatResponseDto;
import com.quan.diabetes.dto.AIChat.ConversationHistoryDto;
import com.quan.diabetes.dto.AIChat.OllamaGenerateRequest;
import com.quan.diabetes.dto.AIChat.OllamaGenerateResponse;
import com.quan.diabetes.dto.AIChat.RAGAiChatRequest;
import com.quan.diabetes.dto.AIChat.RAGAiChatResponse;
import com.quan.diabetes.dto.AIChat.RAGPythonAiRequest;
import com.quan.diabetes.dto.AIChat.RAGPythonAiResponse;
import com.quan.diabetes.entity.AIAssistant;
import com.quan.diabetes.entity.AIConversation;
import com.quan.diabetes.entity.AIMessage;
import com.quan.diabetes.entity.Patient;
import com.quan.diabetes.service.ai.AIAssistantService;
import com.quan.diabetes.service.ai.AIChatService;
import com.quan.diabetes.service.ai.AIConversationService;
import com.quan.diabetes.service.ai.AIMessageService;
import com.quan.diabetes.service.user.PatientService;

import jakarta.persistence.EntityNotFoundException;

@Service
@Transactional
public class AIChatServiceImpl implements AIChatService {

    private static final Logger logger = LoggerFactory.getLogger(AIChatServiceImpl.class);

    @Value("${python.ai.url:http://127.0.0.1:8000/api/ai/chat}")
    private String pythonAiUrl;

    @Value("${ollama.url:http://localhost:11434}")
    private String ollamaUrl;

    @Value("${ollama.model:diabetesAI}")
    private String ollamaDefaultModel;

    private static final String STAGE_2_SYSTEM_PROMPT = """
            Bạn là Bác sĩ nội tiết và trợ lý AI thông minh chuyên tư vấn y khoa và kiểm soát bệnh tiểu đường.
            
            QUY TẮC TRẢ LỜI TỐI THƯỢNG:
            1. CÁCH ĐỌC VÀ TƯ VẤN TỪ DỮ LIỆU BỆNH ÁN (RAG):
               - Trong câu hỏi hoặc prompt sẽ có phần [DỮ LIỆU BỆNH ÁN] chứa các thông tin, chỉ số, hồ sơ của bệnh nhân được hệ thống lưu dưới cấu trúc JSON.
               - NHIỆM VỤ CỦA BẠN: BẮT BUỘC phải trích xuất, phân tích và DIỄN ĐẠT LẠI các chỉ số đó thành lời tư vấn bác sĩ bằng tiếng Việt tự nhiên, thân thiện và dễ hiểu (Ví dụ: "Chào bạn, theo hồ sơ y khoa, bạn có nhóm máu..., các chỉ số hiện tại là...").
               - TUYỆT ĐỐI KHÔNG từ chối trả lời, KHÔNG nói "không thể cung cấp dưới dạng JSON" hay "không thể đọc được trực tiếp", KHÔNG lặp lại mã bệnh nhân hay chuỗi JSON thô. Hãy đóng vai bác sĩ giải thích rõ ràng cho bệnh nhân!
            2. Cấu trúc & Bố cục (Markdown):
               - Mở đầu trả lời trực tiếp (1-2 câu) → Phân tích thông tin bệnh án/cơ chế → Hướng dẫn cụ thể áp dụng được → Lưu ý an toàn.
               - BẮT BUỘC sử dụng định dạng Markdown rõ ràng: chia thành các đoạn văn ngắn, sử dụng tiêu đề (như ### hoặc **...**) để tách biệt các ý.
               - Sử dụng gạch đầu dòng (-) hoặc đánh số (1, 2, 3...) khi liệt kê thông tin, nguyên nhân, triệu chứng hoặc hướng dẫn.
               - In đậm (**chữ in đậm**) các chỉ số y khoa, tên bệnh hoặc từ khóa quan trọng để người đọc dễ nắm bắt.
               - Nếu có trích dẫn hoặc nguồn tham khảo (Refer), BẮT BUỘC phải tách riêng xuống cuối câu trả lời dưới mục "**📚 Nguồn tham khảo:**" rõ ràng, không để lộn xộn trong đoạn văn.
            3. Văn phong thân thiện, ân cần, chuyên nghiệp như bác sĩ thực thụ. Không xưng hô máy móc.
            4. KHÔNG chẩn đoán bệnh mới, KHÔNG kê đơn thuốc. Khuyên gặp bác sĩ khi có triệu chứng bất thường.
            5. Trong câu trả lời, luôn dùng lời văn thông thường để nói chuyện với bệnh nhân, không in các ngoặc nhọn {} hay định dạng lập trình ra màn hình.
            6. Luôn kết thúc bằng câu: "Thông tin này mang tính tham khảo, không thay thế tư vấn của bác sĩ."
            """;

    private final RestTemplate restTemplate;
    private final AIMessageService aiMessageService;
    private final AIConversationService aiConversationService;
    private final AIAssistantService aiAssistantService;
    private final PatientService patientService;
    private final com.quan.diabetes.service.ai.AiTool aiTool;

    public AIChatServiceImpl(
            RestTemplate restTemplate,
            AIMessageService aiMessageService,
            AIConversationService aiConversationService,
            AIAssistantService aiAssistantService,
            PatientService patientService,
            com.quan.diabetes.service.ai.AiTool aiTool) {
        this.restTemplate = restTemplate;
        this.aiMessageService = aiMessageService;
        this.aiConversationService = aiConversationService;
        this.aiAssistantService = aiAssistantService;
        this.patientService = patientService;
        this.aiTool = aiTool;
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
            if (modelToUse == null || modelToUse.isEmpty() || "diabetesAI".equalsIgnoreCase(modelToUse)) {
                modelToUse = ollamaDefaultModel;
                assistant.setModelName(modelToUse);
                aiAssistantService.update(assistant.getAiAssistantId(), assistant);
            }

            logger.info("Using AI Assistant: {} (ID: {}, Model: {})",
                    assistant.getAiName(), assistant.getAiAssistantId(), modelToUse);

            // 3. Get or create AIConversation
            AIConversation conversation = getOrCreateConversation(request, patient, assistant);
            logger.info("Using conversation: {}", conversation.getAiConversationId());

            // Fetch history before saving the current message
            String formattedHistory = aiMessageService.getFormattedConversationHistory(conversation.getAiConversationId(), 20);

            // 4. Save user message
            AIMessage userMessage = new AIMessage();
            userMessage.setContent(request.question());
            userMessage.setSender("Patient");
            userMessage.setTime(LocalDateTime.now());
            userMessage.setAiConversation(conversation);
            aiMessageService.create(userMessage);

            // =========================================================================
            // CHẶNG 1: Phân loại câu hỏi (dữ liệu cá nhân vs kiến thức chung)
            // =========================================================================
            logger.info("--- CHẶNG 1: Phân loại câu hỏi cho patient {} ---", request.patientId());
            String toolJson = classifyAndGetToolJson(request.question(), request.patientId(), modelToUse, formattedHistory);

            String aiResponse = "";
            String sqlData = null;

            if (toolJson != null && !toolJson.isEmpty() && !toolJson.equalsIgnoreCase("NONE")) {
                logger.info("[Chặng 1] Phát hiện yêu cầu truy xuất dữ liệu cá nhân (RAG Tool): {}", toolJson);
                try {
                    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    Map<String, Object> toolMap = mapper.readValue(toolJson, Map.class);

                    String action = (String) toolMap.get("action");
                    String patientId = (String) toolMap.get("patient_id");

                    logger.info("[Chặng 1] Thực thi tool RAG - action: {}, patientId: {}", action, patientId);
                    sqlData = fetchDataFromRepository(action, patientId);
                    logger.info("[Chặng 1] Dữ liệu SQL thu thập được:\n{}", sqlData);
                } catch (Exception e) {
                    logger.error("Lỗi khi xử lý RAG tool JSON ở Chặng 1: {}", e.getMessage(), e);
                    sqlData = "Không thể truy xuất dữ liệu bệnh án do lỗi: " + e.getMessage();
                }
            } else {
                logger.info("[Chặng 1] Câu hỏi kiến thức chung hoặc đã có trong lịch sử -> Chuyển thẳng sang Chặng 2");
            }

            // =========================================================================
            // CHẶNG 2: Gởi input (lịch sử + RAG data nếu có + câu hỏi) cho AI phân tích
            // =========================================================================
            logger.info("--- CHẶNG 2: Gọi AI Ollama phân tích ---");
            String chang2Prompt = buildStage2Prompt(request.question(), sqlData, formattedHistory);

            aiResponse = callOllamaGenerate(modelToUse, chang2Prompt, STAGE_2_SYSTEM_PROMPT, null);
            if (aiResponse == null || aiResponse.trim().isEmpty()) {
                aiResponse = "Xin lỗi, hiện tại hệ thống AI đang gặp sự cố khi xử lý câu hỏi của bạn. Vui lòng thử lại sau.";
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
     * CHẶNG 1: Phân loại câu hỏi là kiến thức y tế chung hay yêu cầu dữ liệu cá
     * nhân. Trả về chuỗi JSON tool (ví dụ: {"action": "get_general_record",
     * "patient_id": "..."}) nếu là dữ liệu cá nhân, hoặc trả về null nếu là
     * kiến thức y tế chung.
     */
    private String classifyAndGetToolJson(String question, String patientId, String modelToUse, String history) {
        // Step 1: Lọc nhanh bằng keyword fallback (giống logic trong file Python) để tối ưu hiệu năng và độ chính xác
        Map<String, String> keywordTool = checkKeywordToolFallback(question, patientId);
        if (keywordTool != null) {
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                return mapper.writeValueAsString(keywordTool);
            } catch (Exception e) {
                logger.error("Lỗi chuyển đổi JSON tool từ keyword: {}", e.getMessage());
            }
        }

        // Step 2: Nếu không khớp keyword, gọi AI Ollama để phân loại
        String systemPromptTool = """
                Bạn là hệ thống phân loại câu hỏi y tế thông minh. Nhiệm vụ của bạn là phân loại câu hỏi của bệnh nhân và trả về kết quả dưới định dạng JSON duy nhất, KHÔNG giải thích gì thêm.
                Dựa vào câu hỏi và lịch sử, nếu bệnh nhân hỏi về dữ liệu cá nhân của họ (chiều cao, cân nặng, bệnh án, kế hoạch điều trị, xét nghiệm, đơn thuốc):
                - Hỏi hồ sơ cá nhân/tiền sử: {"action": "get_general_record", "patient_id": "%s"}
                - Hỏi bệnh án/lịch sử khám/chẩn đoán: {"action": "get_clinical_examination", "patient_id": "%s"}
                - Hỏi kế hoạch điều trị/chế độ ăn/tập luyện: {"action": "get_treatment_plan", "patient_id": "%s"}
                - Hỏi kết quả xét nghiệm/đường huyết/HbA1c: {"action": "get_lab_results", "patient_id": "%s"}
                - Hỏi đơn thuốc/thuốc đang uống: {"action": "get_prescriptions", "patient_id": "%s"}
                
                NẾU LÀ CÂU HỎI KIẾN THỨC Y KHOA CHUNG, CHÀO HỎI, HOẶC THÔNG TIN ĐÃ CÓ TRONG LỊCH SỬ:
                Hãy trả về duy nhất từ: NONE
                """.formatted(patientId, patientId, patientId, patientId, patientId);

        StringBuilder promptBuilder = new StringBuilder();
        if (history != null && !history.trim().isEmpty()) {
            promptBuilder.append("[LỊCH SỬ TRÒ CHUYỆN]:\n").append(history).append("\n\n");
        }
        promptBuilder.append("[YÊU CẦU HIỆN TẠI]\nMã bệnh nhân: ").append(patientId)
                .append("\nCâu hỏi: ").append(question);

        OllamaGenerateRequest.Options options = new OllamaGenerateRequest.Options(0.1, 0.9, 20, 1.15, 4096, 128);
        String responseText = callOllamaGenerate(modelToUse, promptBuilder.toString(), systemPromptTool, options);
        if (responseText != null) {
            String extractedJson = extractToolCallJson(responseText);
            if (extractedJson != null) {
                return extractedJson;
            }
        }
        return null;
    }

    /**
     * Kiểm tra keyword fallback để xác định tool RAG cá nhân nhanh chóng mà
     * không cần tốn tài nguyên gọi LLM
     */
    private Map<String, String> checkKeywordToolFallback(String message, String patientId) {
        if (message == null) {
            return null;
        }
        String msgLower = message.toLowerCase();
        if (msgLower.contains("đơn thuốc") || msgLower.contains("toa thuốc") || msgLower.contains("thuốc của tôi") || msgLower.contains("thuốc đang uống") || msgLower.contains("lịch sử dùng thuốc")) {
            return Map.of("action", "get_prescriptions", "patient_id", patientId);
        }
        if (msgLower.contains("xét nghiệm") || msgLower.contains("chỉ số xét nghiệm") || msgLower.contains("kết quả xét nghiệm") || msgLower.contains("hba1c của tôi") || msgLower.contains("đường huyết của tôi")) {
            return Map.of("action", "get_lab_results", "patient_id", patientId);
        }
        if (msgLower.contains("kế hoạch điều trị") || msgLower.contains("chế độ ăn của tôi") || msgLower.contains("chế độ tập luyện") || msgLower.contains("mục tiêu điều trị") || msgLower.contains("dặn dò")) {
            return Map.of("action", "get_treatment_plan", "patient_id", patientId);
        }
        if (msgLower.contains("bệnh án") || msgLower.contains("lịch sử khám") || msgLower.contains("chẩn đoán của tôi") || msgLower.contains("lịch tái khám") || msgLower.contains("lịch hẹn")) {
            return Map.of("action", "get_clinical_examination", "patient_id", patientId);
        }
        if (msgLower.contains("hồ sơ") || msgLower.contains("hổ sơ") || msgLower.contains("hồ của tôi") || msgLower.contains("thông tin cá nhân") || msgLower.contains("thông tin của tôi") || msgLower.contains("tiền sử bệnh") || msgLower.contains("dị ứng") || msgLower.contains("nhóm máu") || msgLower.contains("chiều cao") || msgLower.contains("cân nặng")) {
            return Map.of("action", "get_general_record", "patient_id", patientId);
        }
        return null;
    }

    /**
     * Trích xuất chuỗi JSON tool call từ phản hồi của Ollama
     */
    private String extractToolCallJson(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        java.util.Set<String> validActions = java.util.Set.of(
                "get_general_record",
                "get_clinical_examination",
                "get_treatment_plan",
                "get_lab_results",
                "get_prescriptions"
        );
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\{[^{}]*\"action\"\\s*:\\s*\"([^\"]+)\"[^{}]*\\}", java.util.regex.Pattern.DOTALL);
        java.util.regex.Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            String actionValue = matcher.group(1);
            if (validActions.contains(actionValue)) {
                String jsonStr = matcher.group(0);
                try {
                    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    mapper.readTree(jsonStr);
                    logger.info("[Ollama Router] Tool call detected: {}", jsonStr);
                    return jsonStr;
                } catch (Exception e) {
                    continue;
                }
            }
        }
        return null;
    }

    /**
     * Xây dựng chuỗi prompt cho Chặng 2 (Tư vấn bởi bác sĩ AI)
     */
    private String buildStage2Prompt(String question, String sqlData, String history) {
        StringBuilder promptBuilder = new StringBuilder();
        if (history != null && !history.trim().isEmpty()) {
            promptBuilder.append("[LỊCH SỬ TRÒ CHUYỆN]:\n").append(history).append("\n\n");
        }
        if (sqlData != null && !sqlData.trim().isEmpty()) {
            promptBuilder.append("[DỮ LIỆU BỆNH ÁN]:\n").append(sqlData).append("\n\n");
        }
        promptBuilder.append("Câu hỏi của bệnh nhân: ").append(question);
        return promptBuilder.toString();
    }

    /**
     * Gọi API /api/generate của Ollama bằng RestTemplate
     */
    private String callOllamaGenerate(String model, String prompt, String system, OllamaGenerateRequest.Options options) {
        try {
            String baseUrl = ollamaUrl.endsWith("/") ? ollamaUrl.substring(0, ollamaUrl.length() - 1) : ollamaUrl;
            String url = baseUrl + "/api/generate";
            OllamaGenerateRequest generateRequest = new OllamaGenerateRequest(model, prompt, system, false, options);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<OllamaGenerateRequest> entity = new HttpEntity<>(generateRequest, headers);

            logger.info("Calling Ollama API at {} with model: {}", url, model);
            OllamaGenerateResponse response = restTemplate.postForObject(url, entity, OllamaGenerateResponse.class);

            if (response != null && response.response() != null) {
                return response.response().trim();
            }
            logger.warn("Ollama API returned null or empty response");
            return null;
        } catch (Exception e) {
            logger.error("Lỗi khi gọi Ollama API (/api/generate): {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Gọi Python AI Server lần 1 (Nhận RAGAiChatRequest và trả về
     * RAGAiChatResponse)
     */
    private RAGAiChatResponse callPythonAiFirst(RAGAiChatRequest ragRequest) {
        try {
            // Convert RAGAiChatRequest thành RAGPythonAiRequest (contextData = "")
            RAGPythonAiRequest pyRequest = new RAGPythonAiRequest(
                    ragRequest.getPatientId(),
                    ragRequest.getMessage(),
                    "",
                    ragRequest.getConversationHistory()
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
     * Gọi Python AI Server lần 2 (Nhận RAGPythonAiRequest và trả về
     * RAGPythonAiResponse)
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
                return aiTool.getGeneralRecord(patientId);
            case "get_clinical_examination":
                return aiTool.getClinicalExamination(patientId);
            case "get_treatment_plan":
                return aiTool.getTreatmentPlan(patientId);
            case "get_lab_results":
                return aiTool.getLabResults(patientId);
            case "get_prescriptions":
                return aiTool.getPrescriptions(patientId);
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
