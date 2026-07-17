package com.quan.diabetes.aiAPI.client;

import com.quan.diabetes.aiAPI.AiClientStrategy;
import com.quan.diabetes.aiAPI.dto.AiGenerateOptions;
import com.quan.diabetes.aiAPI.dto.GeminiGenerateRequest;
import com.quan.diabetes.aiAPI.dto.GeminiGenerateResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class GeminiAiClient implements AiClientStrategy {

    private static final Logger logger = LoggerFactory.getLogger(GeminiAiClient.class);

    private final RestTemplate restTemplate;

    @Value("${gemini.api.key:}")
    private String geminiApiKey;

    @Value("${gemini.api.url:https://generativelanguage.googleapis.com/v1beta/models}")
    private String geminiApiUrl;

    @Value("${gemini.model:gemini-2.5-flash}")
    private String defaultGeminiModel;

    public GeminiAiClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public String generate(String prompt, String systemPrompt, AiGenerateOptions options) {
        if (geminiApiKey == null || geminiApiKey.trim().isEmpty()) {
            logger.warn("Gemini API Key is not configured (gemini.api.key is empty).");
            return "⚠️ **Hệ thống hiện chưa được cấu hình API Key cho Gemini AI.**\n\nQuản trị viên vui lòng cập nhật `gemini.api.key` trong file `application.properties` để sử dụng dịch vụ Google Gemini.";
        }

        String model = (options != null && options.getModelName() != null && !options.getModelName().isBlank())
                ? options.getModelName().trim()
                : defaultGeminiModel.trim();

        // Chuẩn hóa model cũ sang gemini-2.5-flash
        if ("gemini-1.5-pro".equalsIgnoreCase(model) || "gemini-1.5-flash".equalsIgnoreCase(model)) {
            model = "gemini-2.5-flash";
        }

        double temp = (options != null && options.getTemperature() != null) ? options.getTemperature() : 0.15;
        double topP = (options != null && options.getTopP() != null) ? options.getTopP() : 0.9;
        int maxTokens = (options != null && options.getMaxOutputTokens() != null) ? options.getMaxOutputTokens() : 1024;
        if (maxTokens <= 1024) {
            maxTokens = 8192; // Đảm bảo đủ token cho các model thinking (gemini-2.5-flash, gemini-2.0-flash) vừa suy luận vừa trả lời chi tiết
        }

        String baseUrl = geminiApiUrl.endsWith("/") ? geminiApiUrl.substring(0, geminiApiUrl.length() - 1) : geminiApiUrl;
        GeminiGenerateRequest requestBody = GeminiGenerateRequest.create(prompt, systemPrompt, temp, topP, maxTokens);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<GeminiGenerateRequest> entity = new HttpEntity<>(requestBody, headers);

        try {
            String url = String.format("%s/%s:generateContent?key=%s", baseUrl, model, geminiApiKey.trim());
            logger.info("Calling Google Gemini API for model: {}", model);
            GeminiGenerateResponse response = restTemplate.postForObject(url, entity, GeminiGenerateResponse.class);
            return parseGeminiResponse(response);
        } catch (Exception e) {
            logger.warn("Lỗi gọi Gemini API model '{}': {}. Đang tự động thử lại với fallback model...", model, e.getMessage());
            try {
                String fallbackModel = "gemini-2.0-flash";
                if ("gemini-2.0-flash".equalsIgnoreCase(model)) {
                    fallbackModel = "gemini-pro";
                }
                logger.info("Calling Google Gemini API fallback model: {}", fallbackModel);
                String fallbackUrl = String.format("%s/%s:generateContent?key=%s", baseUrl, fallbackModel, geminiApiKey.trim());
                GeminiGenerateResponse fallbackResponse = restTemplate.postForObject(fallbackUrl, entity, GeminiGenerateResponse.class);
                return parseGeminiResponse(fallbackResponse);
            } catch (Exception fallbackEx) {
                logger.warn("Fallback model thứ nhất thất bại: {}. Đang thử fallback cuối cùng 'gemini-pro'...", fallbackEx.getMessage());
                try {
                    String finalUrl = String.format("%s/%s:generateContent?key=%s", baseUrl, "gemini-pro", geminiApiKey.trim());
                    GeminiGenerateResponse finalResponse = restTemplate.postForObject(finalUrl, entity, GeminiGenerateResponse.class);
                    return parseGeminiResponse(finalResponse);
                } catch (Exception finalEx) {
                    logger.error("Lỗi khi gọi Google Gemini API (kể cả fallback): {}", finalEx.getMessage(), finalEx);
                    return "Xin lỗi bạn, hiện tại hệ thống AI của Google đang bị quá tải hoặc gián đoạn tạm thời. Bạn vui lòng gửi lại câu hỏi sau ít phút nhé!";
                }
            }
        }
    }

    private String parseGeminiResponse(GeminiGenerateResponse response) {
        if (response != null && response.candidates() != null && !response.candidates().isEmpty()) {
            GeminiGenerateResponse.Candidate firstCandidate = response.candidates().get(0);
            if (firstCandidate.content() != null && firstCandidate.content().parts() != null && !firstCandidate.content().parts().isEmpty()) {
                StringBuilder sb = new StringBuilder();
                for (GeminiGenerateResponse.Part part : firstCandidate.content().parts()) {
                    if (part.text() != null && !Boolean.TRUE.equals(part.thought())) {
                        sb.append(part.text());
                    }
                }
                String result = sb.toString().trim();
                if (result.isEmpty()) {
                    for (GeminiGenerateResponse.Part part : firstCandidate.content().parts()) {
                        if (part.text() != null) {
                            sb.append(part.text());
                        }
                    }
                    result = sb.toString().trim();
                }
                return result;
            }
        }
        logger.warn("Gemini API returned empty candidates or missing content.");
        return "Xin lỗi, hiện tại hệ thống Gemini AI đang gặp sự cố khi tạo câu trả lời. Vui lòng thử lại sau.";
    }

    @Override
    public String getProviderId() {
        return "GEMINI";
    }

    @Override
    public String getDisplayName() {
        return "API Gemini (Google Cloud)";
    }

    @Override
    public boolean supportsModel(String modelName) {
        if (modelName == null || modelName.isBlank()) return false;
        String lower = modelName.toLowerCase();
        return lower.contains("gemini") || lower.contains("palm") || lower.contains("google");
    }
}
