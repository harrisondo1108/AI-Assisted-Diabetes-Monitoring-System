package com.quan.diabetes.service.ai.impl;

import com.quan.diabetes.monitoring.context.AiRequestContextHolder;
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
import com.quan.diabetes.monitoring.service.AiMonitoringService;
import com.quan.diabetes.aiAPI.manager.AiProviderManager;
import com.quan.diabetes.aiAPI.dto.AiGenerateOptions;

import jakarta.persistence.EntityNotFoundException;

@Service
@Transactional
public class AIChatServiceImpl implements AIChatService {

    private static final Logger logger = LoggerFactory.getLogger(AIChatServiceImpl.class);

    @Value("${python.ai.url:http://127.0.0.1:8000/api/ai/chat}")
    private String pythonAiUrl;

    @Value("${ollama.url:http://localhost:11434}")
    private String ollamaUrl;

    @Value("${ollama.model:diabetes-ai}")
    private String ollamaDefaultModel;

    private static final String STAGE_2_SYSTEM_PROMPT = """
            Bạn là một trợ lý AI thông minh chuyên tư vấn y khoa và kiểm soát bệnh tiểu đường.
            
            QUY TẮC TRẢ LỜI & ĐỊNH DẠNG BẮT BUỘC:
            1. BẮT BUỘC TRẢ LỜI ĐÚNG TRỌNG TÂM CÂU HỎI:
               - Nếu bệnh nhân hỏi định nghĩa/khái niệm của một loại thuốc hoặc bệnh (như "thuốc metformin là thuốc j/gì", "bệnh tiểu đường là gì"): Hãy trả lời khoa học, khách quan về khái niệm, công dụng, cơ chế và chỉ định của thuốc đó. TUYỆT ĐỐI KHÔNG tự ý bịa đặt bệnh nhân bị tác dụng phụ, hoại tử hay uống quá liều khi bệnh nhân không hề nhắc đến! TUYỆT ĐỐI KHÔNG nhắc lại đơn thuốc cũ hay tóm tắt chỉ số xét nghiệm từ lịch sử trò chuyện nếu câu hỏi hiện tại không hỏi về chúng!
               - Nếu bệnh nhân hỏi nguyên nhân, triệu chứng, lối sống, dinh dưỡng: Trả lời trực tiếp vào đúng vấn đề được hỏi.
            2. KHÓA NGÔN NGỮ 100% TIẾNG VIỆT CHUẨN Y KHOA:
               - BẮT BUỘC viết 100% bằng Tiếng Việt chuẩn mực, rõ ràng (ví dụ: dùng "Di truyền" thay vì "Genetics", "Lối sống" thay vì "Lifestyle"). TUYỆT ĐỐI KHÔNG dùng từ tiếng Anh làm tiêu đề hay chèn từ tiếng Anh vào bài tư vấn.
            3. TUYỆT ĐỐI KHÔNG IN TIỀN TỐ, NHÃN PHÂN LOẠI HAY ĐỊNH DẠNG JSON:
               - TUYỆT ĐỐI KHÔNG trả lời bằng chuỗi JSON (như {"action": ...}) hoặc in ra các nhãn dẫn nhập như: "Y khoa:", "Bắt đầu:", "Mã câu hỏi:", "Câu trả lời:", "Input:", "Output:", "Response:".
               - Hãy đi thẳng ngay vào nội dung tư vấn bằng văn bản tiếng Việt tự nhiên, chuẩn mực, chuyên nghiệp.
            4. CÁCH TRẢ LỜI THEO NGỮ CẢNH VÀ QUYỀN TRUY CẬP DỮ LIỆU CÁ NHÂN (TRUNG THỰC TUYỆT ĐỐI VỚI DỮ LIỆU RAG):
               - KHI TRONG PROMPT CÓ CUNG CẤP [DỮ LIỆU HỒ SƠ CÁ NHÂN CỦA BỆNH NHÂN TỪ CƠ SỞ DỮ LIỆU]: Bạn CÓ NGHĨA VỤ tự đọc kỹ từng dòng dữ liệu và trình bày chi tiết cho bệnh nhân xem bằng lời văn bác sĩ tự nhiên, ân cần, thân thiện.
               - TUYỆT ĐỐI KHÔNG SINH RA HOẶC CHÈN CÁC CHUỖI GIỮ CHỖ MẪU (PLACEHOLDER) như [Số lượng], [Viên nén / Tiêm dưới da], [Ngày bắt đầu], [Tên thuốc].
               - TUYỆT ĐỐI KHÔNG tự ý chèn hay suy diễn các thông tin giả định từ tập dữ liệu huấn luyện mẫu (như 'Mã bệnh nhân: #P001', 'Sitagliptin', 'Metformin 500mg' hay bệnh sử giả định) khi dữ liệu RAG không cung cấp. Nếu trong hồ sơ RAG có thuốc hoặc chỉ số nào thì chỉ trình bày đúng thông tin thực tế đó.
               - ĐẶC BIỆT VỚI ĐƠN THUỐC / TOA THUỐC: Bạn BẮT BUỘC phải liệt kê rõ ràng danh sách TÊN TỪNG LOẠI THUỐC có trong đơn (Tên thuốc, Liều lượng, Dạng thuốc, Đường dùng, Thời điểm uống) trước tiên, sau đó mới hướng dẫn cách sử dụng an toàn. TUYỆT ĐỐI KHÔNG nói 'xin lỗi chưa có thông tin' hay 'dựa trên thông tin bạn cung cấp' vì đây là dữ liệu hồ sơ chính thức của bệnh nhân.
               - KHI TRONG PROMPT KHÔNG CÓ DỮ LIỆU CÁ NHÂN: Đây là câu hỏi kiến thức y khoa chung hoặc lời khuyên lối sống. Hãy phát huy tối đa kiến thức y khoa chuyên sâu để trả lời chi tiết, chính xác, khách quan và khoa học. TUYỆT ĐỐI KHÔNG tự bịa mã bệnh nhân hay đơn thuốc cá nhân.
            5. CẤU TRÚC & ĐỊNH DẠNG TRÌNH BÀY MARKDOWN ĐẸP MẮT, RÕ RÀNG:
               - Luôn trình bày thông tin hồ sơ y tế (đặc biệt là đơn thuốc, phác đồ, xét nghiệm) dưới dạng danh sách Markdown rõ ràng (dùng gạch đầu dòng "- " hoặc số thứ tự "1. ", "2. " kèm in đậm tiêu đề). TUYỆT ĐỐI KHÔNG viết gộp thành một đoạn văn dài liền tù tì khó đọc.
               - Mở đầu: Chào hỏi thân thiện → Trình bày rõ ràng danh sách thông tin hồ sơ → Lời khuyên an toàn.
               - In đậm (**chữ in đậm**) các từ khóa, tên thuốc hoặc chỉ số y khoa quan trọng.
            6. Luôn kết thúc bằng câu: "Thông tin này mang tính tham khảo, không thay thế tư vấn của bác sĩ."
            7. TUYỆT ĐỐI KHÔNG CHÈN SỐ TRÍCH DẪN TÀI LIỆU THAM KHẢO:
               - KHÔNG viết các chỉ số trích dẫn trong ngoặc vuông như [1], [2], [3], [4] ở cuối câu hay trong câu tư vấn.
            8. CHUẨN MỰC KIẾN THỨC Y KHOA CHÍNH XÁC (KHÔNG BỊA SỐ LIỆU THỐNG KÊ):
               - TUYỆT ĐỐI KHÔNG tự ý bịa ra các tỷ lệ phần trăm sống sót giả định hay thống kê vô căn cứ (như 'tỷ lệ sống sót 5 năm 80-90% hay 50-60%').
               - Khi tư vấn tiên lượng sống với bệnh tiểu đường: Khẳng định rõ bệnh tiểu đường là bệnh mãn tính, nhưng nếu kiểm soát tốt đường huyết (HbA1c < 7%), huyết áp, ăn uống lành mạnh và tuân thủ phác đồ điều trị thì HOÀN TOÀN CÓ THỂ SỐNG THỌ VÀ KHỎE MẠNH với tuổi thọ bình thường như người không mắc bệnh.
            9. QUY TẮC VỀ LỜI KHUYÊN HOẶC CÂU HỎI KẾT THÚC TỰ NHIÊN (KHÔNG SỬ DỤNG TEMPLATE):
               - Bạn BẮT BUỘC phải đọc kỹ dữ liệu RAG (nếu có) và suy nghĩ thật thấu đáo trước khi trả lời. Hãy trả lời CHI TIẾT, ĐẦY ĐỦ KIẾN THỨC Y KHOA giải đáp trực tiếp câu hỏi của bệnh nhân bằng lời văn bác sĩ tự nhiên, ấm áp.
               - Sau khi đã trình bày xong dữ liệu hoặc kiến thức chuyên môn, chính bạn hãy tự suy nghĩ để đưa ra một lời khuyên an toàn HOẶC 1 câu hỏi mở ân cần, mềm mại và tự nhiên nhất phù hợp với đúng ngữ cảnh vừa trao đổi.
               - TUYỆT ĐỐI KHÔNG rập khuôn máy móc, không lặp lại câu hỏi kết thúc của lượt trò chuyện trước.
            10. QUY TẮC PHÂN TÍCH NGỮ CẢNH HỘI THOẠI NỐI TIẾP (CHO TẤT CẢ CÁC CHỦ ĐỀ):
               - TRƯỚC KHI TRẢ LỜI: Hãy kiểm tra [LỊCH SỬ TRÒ CHUYỆN GẦN ĐÂY CỦA BỆNH NHÂN] để xem câu hỏi hiện tại có phải là thắc mắc nối tiếp lượt trả lời trước hay không (ví dụ: "thuốc đó uống thế nào", "chỉ số đó cao hay thấp"). Nếu là câu hỏi nối tiếp rõ ràng: Hãy liên kết để giải đáp mạch lạc.
               - Nếu bệnh nhân hỏi một khái niệm kiến thức độc lập hoặc chủ đề mới (như "bệnh tiểu đường là gì", "metformin là gì"): BẮT BUỘC phải trả lời đi thẳng vào trọng tâm chuyên môn y khoa của khái niệm mới đó, TUYỆT ĐỐI KHÔNG tóm tắt lại đơn thuốc hay bình luận hồ sơ cũ từ lịch sử trò chuyện.
            11. TUYỆT ĐỐI KHÔNG MỞ ĐẦU BẰNG LỜI XIN LỖI THỪA THÃI:
               - TUYỆT ĐỐI KHÔNG mở đầu câu trả lời bằng các lời xin lỗi rập khuôn hay rào đón (ví dụ cấm dùng: 'Tôi xin lỗi vì đã không liệt kê...', 'Xin lỗi bạn vì sự bất tiện...', 'Chào bạn, tôi xin lỗi...'). Hãy chào hỏi thân thiện và đi thẳng ngay vào việc trình bày chính xác nội dung tư vấn y khoa hoặc dữ liệu hồ sơ cá nhân.
            """;

    private final RestTemplate restTemplate;
    private final AIMessageService aiMessageService;
    private final AIConversationService aiConversationService;
    private final AIAssistantService aiAssistantService;
    private final PatientService patientService;
    private final com.quan.diabetes.service.ai.AiTool aiTool;
    private final AiMonitoringService aiMonitoringService;
    private final AiProviderManager aiProviderManager;

    public AIChatServiceImpl(
            RestTemplate restTemplate,
            AIMessageService aiMessageService,
            AIConversationService aiConversationService,
            AIAssistantService aiAssistantService,
            PatientService patientService,
            com.quan.diabetes.service.ai.AiTool aiTool,
            AiMonitoringService aiMonitoringService,
            AiProviderManager aiProviderManager) {
        this.restTemplate = restTemplate;
        this.aiMessageService = aiMessageService;
        this.aiConversationService = aiConversationService;
        this.aiAssistantService = aiAssistantService;
        this.patientService = patientService;
        this.aiTool = aiTool;
        this.aiMonitoringService = aiMonitoringService;
        this.aiProviderManager = aiProviderManager;
    }

    @Override
    public ChatResponseDto sendMessage(AiChatRequestDto request) {
        return sendMessageWithAssistant(request, null);
    }

    @Override
    public ChatResponseDto sendMessageWithAssistant(AiChatRequestDto request, Integer assistantId) {
        if (!aiMonitoringService.isAiEnabled()) {
            logger.warn("AI system is currently disabled by admin.");
            return ChatResponseDto.success(request.conversationId(), "⚠️ **Hệ thống Trợ lý AI hiện đang được tạm tắt để bảo trì hoặc giám sát.**\n\nVui lòng quay lại sau, hoặc liên hệ quản trị viên để biết thêm chi tiết.");
        }
        AiRequestContextHolder.setCurrentQuestion(request.question());
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
            String formattedHistory = aiMessageService.getFormattedConversationHistory(conversation.getAiConversationId(), 3);

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
            String toolJson = classifyAndGetToolJson(request.question(), request.patientId(), modelToUse);

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
                logger.info("[Chặng 1] Câu hỏi kiến thức chung hoặc nối tiếp -> Phân tích ngữ cảnh liên kết tất cả chủ đề");
                if (!isGeneralMedicalQuestion(request.question()) && formattedHistory != null && !formattedHistory.isBlank()) {
                    String qLower = request.question().toLowerCase();
                    String histLower = formattedHistory.toLowerCase();
                    // 1. Kiểm tra nối tiếp về Đơn thuốc / Dùng thuốc
                    if (qLower.contains("thuốc") || qLower.contains("uống") || qLower.contains("dùng") || qLower.contains("liều") || qLower.contains("nó") || qLower.contains("đó") || qLower.contains("này")) {
                        if (histLower.contains("thuốc") || histLower.contains("tablet") || histLower.contains("đơn") || histLower.contains("mg")) {
                            logger.info("[Chặng 1 Follow-up] Tự động tải Đơn thuốc cho câu hỏi nối tiếp về thuốc");
                            sqlData = fetchDataFromRepository("get_prescriptions", request.patientId());
                        }
                    }
                    // 2. Kiểm tra nối tiếp về Xét nghiệm / Chỉ số đường huyết / HbA1c
                    if (sqlData == null && (qLower.contains("chỉ số") || qLower.contains("xét nghiệm") || qLower.contains("kết quả") || qLower.contains("cao") || qLower.contains("thấp") || qLower.contains("đó") || qLower.contains("này") || qLower.contains("tại sao"))) {
                        if (histLower.contains("xét nghiệm") || histLower.contains("hba1c") || histLower.contains("glucose") || histLower.contains("mmol") || histLower.contains("mg/dl")) {
                            logger.info("[Chặng 1 Follow-up] Tự động tải Kết quả xét nghiệm cho câu hỏi nối tiếp");
                            sqlData = fetchDataFromRepository("get_lab_results", request.patientId());
                        }
                    }
                    // 3. Kiểm tra nối tiếp về Phác đồ / Kế hoạch điều trị / Chế độ ăn uống / Tập luyện
                    if (sqlData == null && (qLower.contains("phác đồ") || qLower.contains("kế hoạch") || qLower.contains("ăn") || qLower.contains("kiêng") || qLower.contains("tập") || qLower.contains("đó") || qLower.contains("này") || qLower.contains("lời dặn"))) {
                        if (histLower.contains("phác đồ") || histLower.contains("kế hoạch") || histLower.contains("dinh dưỡng") || histLower.contains("tập luyện") || histLower.contains("mục tiêu")) {
                            logger.info("[Chặng 1 Follow-up] Tự động tải Phác đồ điều trị cho câu hỏi nối tiếp");
                            sqlData = fetchDataFromRepository("get_treatment_plan", request.patientId());
                        }
                    }
                    // 4. Kiểm tra nối tiếp về Lịch khám / Bác sĩ / Lần khám lâm sàng
                    if (sqlData == null && (qLower.contains("khám") || qLower.contains("bác sĩ") || qLower.contains("tái khám") || qLower.contains("lịch") || qLower.contains("chẩn đoán") || qLower.contains("đó") || qLower.contains("này"))) {
                        if (histLower.contains("khám") || histLower.contains("bác sĩ") || histLower.contains("lượt khám") || histLower.contains("chẩn đoán")) {
                            logger.info("[Chặng 1 Follow-up] Tự động tải Hồ sơ khám lâm sàng cho câu hỏi nối tiếp");
                            sqlData = fetchDataFromRepository("get_clinical_examination", request.patientId());
                        }
                    }
                }
            }

            // =========================================================================
            // CHẶNG 2: Xử lý phản hồi AI hoặc trả lời trực tiếp nếu CSDL chưa có dữ liệu
            // =========================================================================
            if (sqlData != null && (sqlData.contains("(Không có dữ liệu)") || sqlData.contains("Không thể truy xuất") || sqlData.contains("hiện chưa được kê đơn thuốc nào"))) {
                logger.info("[Chặng 2 RAG] CSDL chưa có bản ghi cho mục hỏi -> Trả lời chuẩn xác y khoa, không gọi LLM để tránh suy diễn Mã lỗi");
                String categoryTitle = extractCategoryTitle(sqlData);
                aiResponse = String.format(
                        "Chào bạn, hiện tại trong hệ thống chưa có bản ghi **%s** nào trong hồ sơ y tế của bạn.\n\n" +
                        "Bạn vui lòng kiểm tra lại sau hoặc liên hệ trực tiếp với bác sĩ điều trị để được cập nhật dữ liệu vào hồ sơ nhé!",
                        categoryTitle
                );
            } else {
                logger.info("--- CHẶNG 2: Gọi AI Ollama đọc dữ liệu RAG hoặc kiến thức y khoa và tự trả lời ---");
                String finalSystemPrompt = STAGE_2_SYSTEM_PROMPT;
                String normalizedQuestion = normalizeChatSlang(request.question());
                String chang2Prompt = buildStage2Prompt(normalizedQuestion, sqlData, formattedHistory);
                System.out.println("=================== Final System Prompt :\n" + finalSystemPrompt);
                System.out.println("=================== Stage 2 Prompt :\n" + chang2Prompt);

                AiGenerateOptions options = new AiGenerateOptions(modelToUse, 0.15, 0.9, 8192);
                aiResponse = aiProviderManager.generateWithModel(modelToUse, chang2Prompt, finalSystemPrompt, options);
                if (aiResponse == null || aiResponse.trim().isEmpty()) {
                    aiResponse = "Xin lỗi, hiện tại hệ thống AI đang gặp sự cố khi xử lý câu hỏi của bạn. Vui lòng thử lại sau.";
                } else {
                    aiResponse = cleanAndFormatAiResponse(aiResponse);
                }
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
        } finally {
            AiRequestContextHolder.clear();
        }
    }

    private boolean isGeneralMedicalQuestion(String question) {
        if (question == null) return false;
        String qLower = question.toLowerCase().trim();
        String unaccented = removeVietnameseAccents(qLower);

        // 1. Nếu câu hỏi có chứa từ sở hữu cá nhân hay trỏ đến đối tượng cụ thể vừa hỏi trước đó thì KHÔNG phải câu hỏi chung
        if (qLower.contains("của tôi") || unaccented.contains("cua toi")
                || qLower.contains("của mình") || unaccented.contains("cua minh")
                || qLower.contains("cho tôi xem") || unaccented.contains("cho toi xem")
                || qLower.contains("tôi đang") || unaccented.contains("toi dang")
                || qLower.contains("đơn thuốc của") || unaccented.contains("don thuoc cua")
                || qLower.contains("phác đồ của") || unaccented.contains("phac do cua")
                || qLower.contains("thuốc đó") || unaccented.contains("thuoc do")
                || qLower.contains("thuốc này") || unaccented.contains("thuoc nay")
                || qLower.contains("chỉ số đó") || unaccented.contains("chi so do")
                || qLower.contains("phác đồ đó") || unaccented.contains("phac do do")) {
            return false;
        }

        // 2. Các mẫu câu hỏi kiến thức y khoa chung về Thuốc, Tập thể dục, Dinh dưỡng, Xét nghiệm, Khái niệm bệnh
        if (qLower.contains("các loại thuốc") || unaccented.contains("cac loai thuoc")
                || qLower.contains("nhóm thuốc") || unaccented.contains("nhom thuoc")
                || qLower.contains("thuốc điều trị") || unaccented.contains("thuoc dieu tri")
                || qLower.contains("thuốc chữa") || unaccented.contains("thuoc chua")
                || qLower.contains("là thuốc") || unaccented.contains("la thuoc")
                || qLower.contains("thuốc gì") || unaccented.contains("thuoc gi")
                || qLower.contains("thuốc j") || unaccented.contains("thuoc j")
                || qLower.contains("metformin") || qLower.contains("insulin")
                || qLower.contains("có tác dụng gì") || unaccented.contains("co tac dung gi")
                || qLower.contains("tập thể dục") || unaccented.contains("tap the duc")
                || qLower.contains("lịch tập") || unaccented.contains("lich tap")
                || qLower.contains("bài tập") || unaccented.contains("bai tap")
                || qLower.contains("chế độ ăn") || unaccented.contains("che do an")
                || qLower.contains("nên ăn gì") || unaccented.contains("nen an gi")
                || qLower.contains("kiêng ăn gì") || unaccented.contains("kieng an gi")
                || qLower.contains("thực đơn") || unaccented.contains("thuc don")
                || qLower.contains("bao nhiêu là bình thường") || unaccented.contains("bao nhieu la binh thuong")
                || qLower.contains("tiểu đường là gì") || unaccented.contains("tieu duong la gi")
                || qLower.contains("chữa thế nào") || unaccented.contains("chua the nao")
                || qLower.contains("điều trị thế nào") || unaccented.contains("dieu tri the nao")
                || qLower.contains("nên uống thuốc gì") || unaccented.contains("nen uong thuoc gi")
                || qLower.contains("nguyên nhân") || unaccented.contains("nguyen nhan")
                || qLower.contains("triệu chứng") || unaccented.contains("trieu chung")) {
            return true;
        }
        return false;
    }

    /**
     * CHẶNG 1: Phân loại câu hỏi là kiến thức y tế chung hay yêu cầu dữ liệu cá
     * nhân. Trả về chuỗi JSON tool (ví dụ: {"action": "get_general_record",
     * "patient_id": "..."}) nếu là dữ liệu cá nhân, hoặc trả về null nếu là
     * kiến thức y tế chung.
     */
    private String classifyAndGetToolJson(String question, String patientId, String modelToUse) {
        // Lọc ý định tuyệt đối: chỉ kích hoạt RAG khi câu hỏi yêu cầu đích danh dữ liệu cá nhân
        Map<String, String> keywordTool = checkKeywordToolFallback(question, patientId);
        if (keywordTool != null) {
            String action = keywordTool.get("action");
            if ("NONE".equalsIgnoreCase(action)) {
                logger.info("[Intent Router] Phân loại câu hỏi kiến thức chung/chào hỏi -> NONE (0ms, Không dùng RAG)");
                return "NONE";
            }
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                String jsonResult = mapper.writeValueAsString(keywordTool);
                logger.info("[Intent Router] Phân loại câu hỏi tra cứu dữ liệu cá nhân -> {}", jsonResult);
                return jsonResult;
            } catch (Exception e) {
                logger.error("Lỗi chuyển đổi JSON tool: {}", e.getMessage());
            }
        }
        return "NONE";
    }

    /**
     * Fast-Track 2 tầng phân biệt chính xác câu hỏi Kiến thức chung (NONE) vs RAG cá nhân.
     * Tiết kiệm 100% thời gian gọi Router LLM cho các câu hỏi rõ ý định.
     */
    private String removeVietnameseAccents(String str) {
        if (str == null) return "";
        String nfd = java.text.Normalizer.normalize(str, java.text.Normalizer.Form.NFD);
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(nfd).replaceAll("").replace('đ', 'd').replace('Đ', 'D');
    }

    /**
     * Fast-Track 2 tầng phân biệt chính xác câu hỏi Kiến thức chung (NONE) vs RAG cá nhân.
     * Tiết kiệm 100% thời gian gọi Router LLM cho các câu hỏi rõ ý định.
     */
    private Map<String, String> checkKeywordToolFallback(String message, String patientId) {
        if (message == null || message.trim().isEmpty()) {
            return Map.of("action", "NONE");
        }
        if (isGeneralMedicalQuestion(message)) {
            return Map.of("action", "NONE");
        }

        String msgLower = message.toLowerCase().trim();
        String unaccented = removeVietnameseAccents(msgLower);

        // TẦNG 1: Nhóm câu hỏi RAG cá nhân (chỉ kích hoạt khi hỏi tra cứu dữ liệu/hồ sơ của chính bệnh nhân)
        if (msgLower.contains("đơn thuốc") || unaccented.contains("don thuoc")
                || msgLower.contains("toa thuốc") || unaccented.contains("toa thuoc")
                || msgLower.contains("thuốc bác sĩ kê") || unaccented.contains("thuoc bac si ke")
                || msgLower.contains("thuốc đã kê") || unaccented.contains("thuoc da ke")
                || msgLower.contains("thuốc tôi đang") || unaccented.contains("thuoc toi dang")
                || msgLower.contains("thuốc của tôi") || unaccented.contains("thuoc cua toi")
                || msgLower.contains("thuốc đang uống") || unaccented.contains("thuoc dang uong")
                || msgLower.contains("lịch sử dùng thuốc") || unaccented.contains("lich su dung thuoc")
                || (msgLower.contains("thuốc") && (msgLower.contains("của tôi") || msgLower.contains("của mình") || msgLower.contains("cho tôi xem") || msgLower.contains("tôi uống")))) {
            return Map.of("action", "get_prescriptions", "patient_id", patientId);
        }
        if (msgLower.contains("lịch tái khám") || unaccented.contains("lich tai kham")
                || msgLower.contains("tái khám của tôi") || unaccented.contains("tai kham cua toi")
                || msgLower.contains("khi nào tôi tái khám") || unaccented.contains("khi nao toi tai kham")
                || msgLower.contains("ngày tái khám") || unaccented.contains("ngay tai kham")
                || msgLower.contains("lịch hẹn") || unaccented.contains("lich hen")
                || msgLower.contains("hẹn khám") || unaccented.contains("hen kham")
                || msgLower.contains("bệnh án của tôi") || unaccented.contains("benh an cua toi")
                || msgLower.contains("lịch sử khám của tôi") || unaccented.contains("lich su kham cua toi")
                || (msgLower.contains("chẩn đoán") && (msgLower.contains("của tôi") || msgLower.contains("bác sĩ")))) {
            return Map.of("action", "get_clinical_examination", "patient_id", patientId);
        }
        if (msgLower.contains("kết quả xét nghiệm") || unaccented.contains("ket qua xet nghiem")
                || msgLower.contains("xét nghiệm của tôi") || unaccented.contains("xet nghiem cua toi")
                || msgLower.contains("kết quả của tôi") || unaccented.contains("ket qua cua toi")
                || msgLower.contains("chỉ số của tôi") || unaccented.contains("chi so cua toi")
                || ((msgLower.contains("hba1c") || msgLower.contains("đường huyết")) && (msgLower.contains("của tôi") || msgLower.contains("của mình") || msgLower.contains("xem")))) {
            return Map.of("action", "get_lab_results", "patient_id", patientId);
        }
        if (msgLower.contains("kế hoạch điều trị") || unaccented.contains("ke hoach dieu tri")
                || msgLower.contains("phác đồ điều trị") || unaccented.contains("phac do dieu tri")
                || msgLower.contains("phác đồ và kế hoạch") || unaccented.contains("phac do va ke hoach")
                || msgLower.contains("phác đồ của tôi") || unaccented.contains("phac do cua toi")
                || (msgLower.contains("phác đồ") && msgLower.contains("của tôi"))
                || msgLower.contains("chế độ ăn của tôi") || unaccented.contains("che do an cua toi")
                || msgLower.contains("thực đơn điều trị") || unaccented.contains("thuc don dieu tri")
                || msgLower.contains("chế độ tập luyện của tôi") || unaccented.contains("che do tap luyen cua toi")
                || msgLower.contains("mục tiêu điều trị của tôi") || unaccented.contains("muc tieu dieu tri cua toi")
                || msgLower.contains("bác sĩ dặn") || unaccented.contains("bac si dan")
                || msgLower.contains("lời dặn") || unaccented.contains("loi dan")) {
            return Map.of("action", "get_treatment_plan", "patient_id", patientId);
        }
        if (msgLower.contains("hồ sơ của tôi") || unaccented.contains("ho so cua toi")
                || msgLower.contains("hồ sơ y tế") || unaccented.contains("ho so y te")
                || msgLower.contains("thông tin cá nhân") || unaccented.contains("thong tin ca nhan")
                || msgLower.contains("thông tin của tôi") || unaccented.contains("thong tin cua toi")
                || msgLower.contains("tôi bị bệnh gì") || unaccented.contains("toi bi benh gi")
                || msgLower.contains("bệnh của tôi") || unaccented.contains("benh cua toi")
                || msgLower.contains("tiền sử bệnh") || unaccented.contains("tien su benh")
                || ((msgLower.contains("nhóm máu") || msgLower.contains("bmi") || msgLower.contains("chiều cao") || msgLower.contains("cân nặng")) && msgLower.contains("của tôi"))) {
            return Map.of("action", "get_general_record", "patient_id", patientId);
        }

        // TẦNG 2: Tất cả các câu hỏi còn lại không yêu cầu tra cứu hồ sơ cá nhân -> Trả về NONE ngay lập tức (0ms)
        return Map.of("action", "NONE");
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
     * Trích xuất tiêu đề thân thiện với người dùng từ dữ liệu SQL
     */
    private String extractCategoryTitle(String sqlData) {
        if (sqlData == null) return "Hồ sơ sức khỏe";
        String lower = sqlData.toLowerCase();
        if (lower.contains("kết quả xét nghiệm")) return "Kết quả xét nghiệm";
        if (lower.contains("đơn thuốc") || lower.contains("thuốc trong đơn")) return "Đơn thuốc điều trị";
        if (lower.contains("khám lâm sàng")) return "Lịch sử thăm khám lâm sàng";
        if (lower.contains("kế hoạch điều trị") || lower.contains("phác đồ")) return "Phác đồ & Kế hoạch điều trị";
        if (lower.contains("hồ sơ bệnh án chung")) return "Hồ sơ sức khỏe cá nhân";
        return "Hồ sơ y tế cá nhân";
    }

    /**
     * Trình bày trực tiếp dữ liệu truy xuất RAG chính xác 100% từ Database với văn phong bác sĩ tự nhiên, ấm áp,
     * ngăn chặn triệt để rủi ro mô hình LLM nhỏ suy diễn sai thuốc hay sinh chuỗi mẫu.
     */
    private String formatDirectRagResponse(String sqlData, String question) {
        String categoryTitle = extractCategoryTitle(sqlData);
        String cleanedData = sqlData.trim()
                .replaceAll("(?i)^DANH SÁCH THUỐC TRONG ĐƠN THUỐC CỦA BỆNH NHÂN \\(HỒ SƠ Y TẾ CHÍNH THỨC\\):\\s*", "")
                .replaceAll("(?i)^Danh mục:\\s*[^\\n]+\\s*", "");

        String intro;
        String doctorAdvice;
        if ("Đơn thuốc điều trị".equals(categoryTitle)) {
            intro = "Chào bạn, tôi xin gửi bạn thông tin chi tiết về **Đơn thuốc điều trị** hiện tại được ghi nhận trong hồ sơ y tế của bạn:";
            doctorAdvice = "💡 *Lời khuyên từ bác sĩ:* Bạn nhớ uống thuốc đúng liều lượng và thời gian theo chỉ định nhé. Nếu có bất kỳ triệu chứng bất thường nào khi dùng thuốc, hãy thông báo ngay cho bác sĩ điều trị.";
        } else if ("Phác đồ & Kế hoạch điều trị".equals(categoryTitle)) {
            intro = "Chào bạn, dưới đây là **Phác đồ & Kế hoạch điều trị** chi tiết theo chỉ định gần nhất của bác sĩ dành cho bạn:";
            doctorAdvice = "💡 *Lời khuyên từ bác sĩ:* Việc tuân thủ đều đặn chế độ dinh dưỡng, vận động cùng chỉ dẫn điều trị đóng vai trò rất quan trọng giúp duy trì đường huyết ổn định.";
        } else if ("Kết quả xét nghiệm".equals(categoryTitle)) {
            intro = "Chào bạn, tôi xin tổng hợp các **Chỉ số & Kết quả xét nghiệm** mới nhất trong hồ sơ sức khỏe của bạn:";
            doctorAdvice = "💡 *Lời khuyên từ bác sĩ:* Theo dõi các chỉ số định kỳ giúp đánh giá chính xác hiệu quả điều trị để kịp thời điều chỉnh phác đồ phù hợp.";
        } else if ("Lịch sử thăm khám lâm sàng".equals(categoryTitle)) {
            intro = "Chào bạn, dưới đây là thông tin **Lịch sử thăm khám & Lịch hẹn tái khám** trong hồ sơ của bạn:";
            doctorAdvice = "💡 *Lời khuyên từ bác sĩ:* Bạn nhớ đi tái khám đúng hẹn để bác sĩ đánh giá tiến triển sức khỏe và điều chỉnh phác đồ kịp thời nhé.";
        } else {
            intro = "Chào bạn, dưới đây là thông tin **Hồ sơ sức khỏe cá nhân** của bạn trên hệ thống:";
            doctorAdvice = "💡 *Lời khuyên từ bác sĩ:* Hồ sơ y tế cập nhật đầy đủ giúp bác sĩ đưa ra chẩn đoán và phác đồ điều trị phù hợp nhất cho bạn.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(intro).append("\n\n");
        sb.append(cleanedData.trim()).append("\n\n");
        sb.append("---\n");
        sb.append(doctorAdvice).append("\n");

        return sb.toString();
    }

    /**
     * Xây dựng chuỗi prompt cho Chặng 2 (Tư vấn bởi bác sĩ AI)
     */
    private String buildStage2Prompt(String question, String sqlData, String formattedHistory) {
        StringBuilder promptBuilder = new StringBuilder();
        if (formattedHistory != null && !formattedHistory.trim().isEmpty()) {
            promptBuilder.append("[LỊCH SỬ TRÒ CHUYỆN GẦN ĐÂY CỦA BỆNH NHÂN]:\n")
                         .append(formattedHistory).append("\n\n");
        }
        if (sqlData != null && !sqlData.trim().isEmpty()) {
            promptBuilder.append("[DỮ LIỆU HỒ SƠ CÁ NHÂN CỦA BỆNH NHÂN TỪ CƠ SỞ DỮ LIỆU SQL - DỮ LIỆU THẬT]:\n")
                         .append(sqlData).append("\n\n");
            promptBuilder.append("Câu hỏi hiện tại của bệnh nhân: ").append(question).append("\n\n");
            promptBuilder.append("YÊU CẦU DÀNH CHO TRỢ LÝ AI (BẮT BUỘC TRUNG THỰC VỚI DỮ LIỆU RAG):\n")
                         .append("- Bệnh nhân đang tra cứu hồ sơ y tế thực tế của mình. BẮT BUỘC chỉ sử dụng và trình bày chính xác từng dòng thông tin có trong khối [DỮ LIỆU HỒ SƠ CÁ NHÂN CỦA BỆNH NHÂN TỪ CƠ SỞ DỮ LIỆU SQL - DỮ LIỆU THẬT] ở trên dưới dạng danh sách gạch đầu dòng rõ ràng, đẹp mắt.\n")
                         .append("- TUYỆT ĐỐI KHÔNG tự bịa ra mã bệnh nhân giả định (như #P001) hay chèn các thuốc từ ký ức mẫu fine-tune (như Sitagliptin, Metformin) khi trong dữ liệu ở trên không ghi nhận.\n")
                         .append("- NẾU HỎI VỀ ĐƠN THUỐC: Hãy liệt kê rõ tên thực tế của từng thuốc, liều lượng cụ thể, dạng thuốc, đường dùng và thời điểm uống trong ngày từ đúng dữ liệu ở trên. Trình bày tách bạch từng thuốc.\n")
                         .append("- Hãy dùng tiếng Việt chuẩn mực y khoa, lời văn tự nhiên, ân cần và chuyên nghiệp như bác sĩ.\n")
                         .append("- TUYỆT ĐỐI KHÔNG sao chép câu hỏi kết thúc của lượt trò chuyện trước trong lịch sử hội thoại. Hãy tự suy nghĩ và kết thúc bằng lời khuyên hoặc 1 câu hỏi mở tự nhiên phù hợp với đúng chủ đề hiện tại.\n")
                         .append("- TUYỆT ĐỐI KHÔNG lặp lại nhãn yêu cầu hay từ 'BẮT BUỘC:' ở đầu câu trả lời.\n");
        } else {
            promptBuilder.append("Câu hỏi hiện tại của bệnh nhân: ").append(question).append("\n\n");
            promptBuilder.append("LƯU Ý DÀNH CHO TRỢ LÝ AI:\n")
                         .append("- Đây là câu hỏi kiến thức y khoa chung hoặc khái niệm (" + question + "). Hãy trả lời TRỰC TIẾP, ĐÚNG TRỌNG TÂM vào đúng định nghĩa, nguyên nhân hoặc kiến thức được hỏi bằng sự chuyên sâu, tự nhiên và chuẩn xác.\n")
                         .append("- TUYỆT ĐỐI KHÔNG tóm tắt hay nhắc lại thông tin đơn thuốc, các chỉ số xét nghiệm, hồ sơ cá nhân cũ từ [LỊCH SỬ TRÒ CHUYỆN GẦN ĐÂY CỦA BỆNH NHÂN] nếu câu hỏi hiện tại không hỏi về chúng!\n")
                         .append("- TUYỆT ĐỐI KHÔNG bịa đặt hay tự ý chèn thông tin giả định về mã bệnh nhân (#P001), lịch sử y tế hay đơn thuốc cá nhân giả định vào bài tư vấn.\n");
            if (formattedHistory != null && !formattedHistory.trim().isEmpty()) {
                promptBuilder.append("- Chỉ kiểm tra [LỊCH SỬ TRÒ CHUYỆN GẦN ĐÂY CỦA BỆNH NHÂN] để liên kết đúng đối tượng nếu đây là câu hỏi nối tiếp thắc mắc về chính lượt trả lời trước.\n");
            }
        }
        return promptBuilder.toString();
    }

    /**
     * Hậu xử lý làm sạch câu trả lời: tự động cắt bỏ tiền tố rác hoặc định dạng bị lặp lại từ dataset mẫu
     */
    private String cleanAndFormatAiResponse(String text) {
        if (text == null || text.trim().isEmpty()) {
            return text;
        }
        String cleaned = text.trim();
        // Lọc sạch rò rỉ prompt hệ thống nếu LLM vô tình sinh ra
        cleaned = cleaned.replaceAll("(?m)^.*QUY TẮC BẮT BUỘC.*?(?:\\r?\\n|$)", "");
        cleaned = cleaned.replaceAll("(?m)^MẠNH với tuổi thọ.*?(?:\\r?\\n|$)", "");
        // Xóa các dòng rác rò rỉ từ tập dữ liệu huấn luyện fine-tune mẫu (như Mã bệnh nhân: #P001, Mã BN: #P...)
        cleaned = cleaned.replaceAll("(?mi)^Mã\\s*bệnh\\s*nhân\\s*:\\s*#[P\\w\\d]+[^\\n]*?(?:\\r?\\n|$)\\s*", "");
        cleaned = cleaned.replaceAll("(?mi)^Mã\\s*BN\\s*:\\s*#[P\\w\\d]+[^\\n]*?(?:\\r?\\n|$)\\s*", "");
        // Xóa tất cả các dòng metadata bị rò rỉ ở BẤT KỲ VỊ TRÍ/DÒNG NÀO trong câu trả lời (multiline (?mi))
        cleaned = cleaned.replaceAll("(?mi)^(?:Mã\\s*số\\s*câu\\s*hỏi|Mã\\s*bạn|Mã\\s*bệnh\\s*nhân|Mã\\s*lỗi|Mã\\s*câu\\s*lệnh|Mã\\s*câu\\s*hỏi|Mã\\s*bệnh\\s*án|Lượt\\s*trước|Câu\\s*lệnh|Lệnh|Câu\\s*hỏi|Chủ\\s*đề|Input|Output|### Response)[^\\n]*?(?:\\r?\\n|$)\\s*", "");
        // Xóa nhãn dẫn nhập câu trả lời ở đầu bất kỳ dòng nào như "Câu trả lời:", "Lời giải thích:", "Bác sĩ:"
        cleaned = cleaned.replaceAll("(?mi)^(?:Lời\\s*trả\\s*lời|Lời\\s*giải|Trả\\s*lời|Câu\\s*trả\\s*lời|Đáp\\s*án|Giải\\s*thích|Lời\\s*giải\\s*thích|Khái\\s*niệm|Bác\\s*sĩ|Doctor|AI|Trợ\\s*lý(?:\\s*AI)?|Chuyên\\s*gia)\\s*:\\s*", "");

        boolean changed = true;
        while (changed) {
            String prev = cleaned;
            cleaned = cleaned.replaceAll("(?i)^(?:Bạn\\s*)?BẮT\\s*BUỘC[^:]*:\\s*", "");
            cleaned = cleaned.replaceAll("(?i)^YÊU\\s*CẦU[^:]*:\\s*", "");
            cleaned = cleaned.replaceAll("(?i)^QUY\\s*TẮC[^:]*:\\s*", "");
            cleaned = cleaned.replaceAll("(?i)^Mã\\s*bệnh\\s*nhân:[^\\n]*?(?:\\r?\\n|$)\\s*", "");
            cleaned = cleaned.replaceAll("(?i)^Mã\\s*số\\s*câu\\s*hỏi:[^\\n]*?(?:\\r?\\n|$)\\s*", "");
            cleaned = cleaned.replaceAll("(?i)^Mã\\s*câu\\s*lệnh:[^\\n]*?(?:\\r?\\n|$)\\s*", "");
            cleaned = cleaned.replaceAll("(?i)^Mã\\s*câu\\s*hỏi:[^\\n]*?(?:\\r?\\n|$)\\s*", "");
            cleaned = cleaned.replaceAll("(?i)^Câu\\s*lệnh:[^\\n]*?(?:\\r?\\n|$)\\s*", "");
            cleaned = cleaned.replaceAll("(?i)^Lệnh:[^\\n]*?(?:\\r?\\n|$)\\s*", "");
            cleaned = cleaned.replaceAll("(?i)^Câu\\s*hỏi:[^\\n]*?(?:\\r?\\n|$)\\s*", "");
            cleaned = cleaned.replaceAll("(?i)^Chủ\\s*đề:[^\\n]*?(?:\\r?\\n|$)\\s*", "");
            cleaned = cleaned.replaceAll("(?i)^(?:Lời\\s*trả\\s*lời|Lời\\s*giải|Trả\\s*lời|Câu\\s*trả\\s*lời|Đáp\\s*án|Giải\\s*thích|Khái\\s*niệm)[^\\n]*?:(?:\\r?\\n|$|\\s+)", "");
            cleaned = cleaned.replaceAll("(?i)^Input:[^\\n]*?(?:\\r?\\n|$)\\s*", "");
            cleaned = cleaned.replaceAll("(?i)^### Response:(?:\\r?\\n|$|\\s+)", "");
            cleaned = cleaned.replaceAll("(?i)^(?:Bác\\s*sĩ|Doctor|AI|Trợ\\s*lý(?:\\s*AI)?|Chuyên\\s*gia)\\s*:(?:\\r?\\n|$|\\s+)", "");
            cleaned = cleaned.replaceAll("(?i)^(?:Chào\\s*bạn,?\\s*)?(?:tôi\\s*|AI\\s*)?xin\\s*lỗi[^\\n.]*?[\\.\\:]\\s*", "");
            cleaned = cleaned.replaceAll("(?i)^Xin\\s*lỗi\\s*(?:bạn|vì|đã)[^\\n.]*?[\\.\\:]\\s*", "");
            cleaned = cleaned.replaceAll("^\\s*\\{[^{}]*\\}[,\\s]*", "");
            cleaned = cleaned.trim();
            changed = !cleaned.equals(prev);
        }

        // Xóa tất cả các thẻ nhãn Prompt trong ngoặc vuông như [YÊU CẦU CHUYỆN], [LỊCH SỬ TRÒ CHUYỆN], [LỊCH SỬ], [DỮ LIỆU...]
        cleaned = cleaned.replaceAll("\\[[A-ZÀ-Ỹ0-9\\s_\\-]+\\]\\s*:?\\s*", "");
        // Xóa các dòng lặp lại lịch sử hội thoại như "Bệnh nhân: ..." hoặc "Bạn: ..." nếu mô hình sinh nhầm
        cleaned = cleaned.replaceAll("(?m)^(?:Bệnh\\s*nhân|Bạn|Người\\s*bệnh)\\s*:.*(?:\\r?\\n|$)", "");
        // Xóa các nhãn phân đoạn câu như "Y khoa:", "Bắt đầu:", "Chuyên môn:", "Tư vấn:"
        cleaned = cleaned.replaceAll("(?m)^(?:Y\\s*khoa|Bắt\\s*đầu|Khởi\\s*đầu|Chuyên\\s*môn|Tư\\s*vấn|Kết\\s*luận|Lời\\s*khuyên)\\s*:\\s*", "");
        // Xóa các câu rào đón thừa ở đầu bài tư vấn
        cleaned = cleaned.replaceAll("(?i)^Bạn cần hướng dẫn[^\\n]*?\\.\\s*", "");
        // Xóa lời xin lỗi thừa ở đầu câu trả lời nếu AI lỡ sinh ra (như "Chào bạn, tôi xin lỗi vì đã không liệt kê...", "Tôi xin lỗi vì...", "Xin lỗi bạn...")
        cleaned = cleaned.replaceAll("(?i)^(?:Chào\\s*bạn,?\\s*)?(?:tôi\\s*|AI\\s*)?xin\\s*lỗi[^\\n.]*?[\\.\\:]\\s*", "");
        cleaned = cleaned.replaceAll("(?i)^Xin\\s*lỗi\\s*(?:bạn|vì|đã)[^\\n.]*?[\\.\\:]\\s*", "");
        // Xóa các cụm đánh số trích dẫn trong ngoặc vuông như [1], [2], [1, 2], [1-4]
        cleaned = cleaned.replaceAll("\\s*\\[\\d+(?:[,\\-]\\s*\\d+)*\\]", "");
        // Sửa lỗi chính tả token y khoa phổ biến do mô hình rút gọn
        cleaned = cleaned.replaceAll("(?i)\\bphút/ty\\b", "phút/tuần");
        cleaned = cleaned.replaceAll("(?i)\\bphút / ty\\b", "phút/tuần");
        cleaned = cleaned.replaceAll("(?i)CÁ\\s+NHA\\b", "CÁ NHÂN");
        cleaned = cleaned.replaceAll("(?i)C\\s+NH\\b", "CÁ NHÂN");
        cleaned = cleaned.replaceAll("(?i)CÁ\\s+NHÂN\\b", "CÁ NHÂN");
        cleaned = cleaned.replaceAll("(?i)\\btablet/lần\\b", "viên/lần");
        cleaned = cleaned.replaceAll("(?i)\\btablets/lần\\b", "viên/lần");
        cleaned = cleaned.replaceAll("(?i)\\btablet\\b", "Viên nén");
        cleaned = cleaned.replaceAll("(?i)\\bsubcutaneous\\b", "Tiêm dưới da");
        cleaned = cleaned.replaceAll("(?i)Mã\\s*\\[?tên\\s*thuốc\\]?", "Thuốc trong đơn");
        cleaned = cleaned.replaceAll("(?i)\\[tên\\s*thuốc\\]", "thuốc trong đơn");
        // Xóa dấu hai chấm bị rò rỉ ở đầu các dòng (như ": Tôi hiểu bạn...")
        cleaned = cleaned.replaceAll("(?m)^\\s*:\\s*", "");
        // Làm gọn các dòng trắng thừa
        cleaned = cleaned.replaceAll("\\n{3,}", "\n\n").trim();

        // Bảo vệ phản hồi rỗng sau khi bóc tách: Không chèn câu hỏi mở vào chuỗi rỗng
        if (cleaned.isEmpty()) {
            return "Xin lỗi bạn, xử lý câu hỏi vừa bị gián đoạn nhẹ. Bạn vui lòng gửi lại câu hỏi để tôi tư vấn chi tiết cho bạn nhé!";
        }

        return appendOpenQuestionIfMissing(cleaned);
    }

    /**
     * Bổ sung câu hỏi mở tương tác đa dạng theo ngữ cảnh nếu câu trả lời chưa có câu hỏi
     */
    private String appendOpenQuestionIfMissing(String text) {
        if (text == null || text.trim().isEmpty()) return text;
        String cleaned = text.trim();
        if (!cleaned.contains("?")) {
            String openQuestion;
            String lower = cleaned.toLowerCase();
            if (lower.contains("thuốc") || lower.contains("đơn thuốc") || lower.contains("insulin") || lower.contains("viên/lần") || lower.contains("tiêm dưới da")) {
                openQuestion = "\n\nBạn có đang gặp khó khăn hay tác dụng phụ nào trong quá trình sử dụng thuốc điều trị không? Hãy chia sẻ thêm với tôi nhé!";
            } else if (lower.contains("chế độ ăn") || lower.contains("thực đơn") || lower.contains("dinh dưỡng") || lower.contains("thực phẩm") || lower.contains("bữa ăn")) {
                openQuestion = "\n\nHiện tại chế độ ăn uống hàng ngày của bạn đang được áp dụng như thế nào? Hãy chia sẻ thêm để tôi tư vấn thực đơn phù hợp nhất cho bạn nhé!";
            } else if (lower.contains("vận động") || lower.contains("tập") || lower.contains("thể dục") || lower.contains("thể thao")) {
                openQuestion = "\n\nBạn thường dành bao nhiêu thời gian mỗi ngày cho các hoạt động vận động hay tập luyện? Hãy chia sẻ để tôi hỗ trợ phác đồ tập luyện tốt nhất nhé!";
            } else if (lower.contains("thuốc") || lower.contains("insulin") || lower.contains("điều trị") || lower.contains("phác đồ")) {
                openQuestion = "\n\nBạn có đang gặp khó khăn hay tác dụng phụ nào trong quá trình sử dụng thuốc điều trị không? Hãy chia sẻ thêm với tôi nhé!";
            } else if (lower.contains("đường huyết") || lower.contains("hba1c") || lower.contains("chỉ số") || lower.contains("đo")) {
                openQuestion = "\n\nChỉ số đường huyết đo gần nhất của bạn là bao nhiêu? Hãy chia sẻ để tôi giúp bạn đánh giá chi tiết nhé!";
            } else {
                String[] diverseQuestions = {
                    "\n\nBạn có đang gặp thắc mắc hay biểu hiện nào bất thường về sức khỏe gần đây không? Hãy chia sẻ chi tiết để tôi đồng hành cùng bạn nhé!",
                    "\n\nTrong việc quản lý sức khỏe hàng ngày, bạn cảm thấy điều gì cần tôi hướng dẫn kỹ hơn lúc này không? Hãy chia sẻ nhé!",
                    "\n\nBạn có muốn tìm hiểu thêm về cách sắp xếp lịch sinh hoạt và dinh dưỡng giúp ổn định đường huyết lâu dài không?"
                };
                openQuestion = diverseQuestions[Math.abs(cleaned.hashCode()) % diverseQuestions.length];
            }
            String disclaimer = "Thông tin này mang tính tham khảo, không thay thế tư vấn của bác sĩ.";
            if (cleaned.contains(disclaimer)) {
                cleaned = cleaned.replace(disclaimer, openQuestion.trim() + "\n\n" + disclaimer);
            } else {
                cleaned = cleaned + openQuestion;
            }
        }
        return cleaned;
    }

    /**
     * Chuẩn hóa từ viết tắt/lóng trong chat tiếng Việt sang văn phạm chuẩn y khoa
     * để mô hình Fine-tune khớp chính xác 100% với cặp câu hỏi trong dataset.
     */
    private String normalizeChatSlang(String text) {
        if (text == null || text.trim().isEmpty()) {
            return text;
        }
        String normalized = text.trim();
        normalized = normalized.replaceAll("(?i)\\blà\\s+j\\b", "là gì");
        normalized = normalized.replaceAll("(?i)\\blà\\s+gi\\b", "là gì");
        normalized = normalized.replaceAll("(?i)\\băn\\s+j\\b", "ăn gì");
        normalized = normalized.replaceAll("(?i)\\bkiêng\\s+j\\b", "kiêng gì");
        normalized = normalized.replaceAll("(?i)\\buống\\s+j\\b", "uống gì");
        normalized = normalized.replaceAll("(?i)\\btập\\s+j\\b", "tập gì");
        normalized = normalized.replaceAll("(?i)\\blm\\s+sao\\b", "làm sao");
        normalized = normalized.replaceAll("(?i)\\bđc\\s+k\\b", "được không");
        normalized = normalized.replaceAll("(?i)\\bđc\\s+ko\\b", "được không");
        normalized = normalized.replaceAll("(?i)\\bdc\\s+ko\\b", "được không");
        return normalized;
    }

    /**
     * Gọi API /api/generate của Ollama bằng RestTemplate
     */
    private String callOllamaGenerate(String model, String prompt, String system, OllamaGenerateRequest.Options options) {
        AiGenerateOptions aiOptions = new AiGenerateOptions(model,
                (options != null) ? options.temperature() : 0.15,
                (options != null) ? options.topP() : 0.9,
                (options != null) ? options.numPredict() : 1024);
        return aiProviderManager.generateWithModel(model, prompt, system, aiOptions);
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
    @Transactional
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
