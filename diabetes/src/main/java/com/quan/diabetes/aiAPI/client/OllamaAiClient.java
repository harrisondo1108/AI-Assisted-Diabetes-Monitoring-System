package com.quan.diabetes.aiAPI.client;

import com.quan.diabetes.aiAPI.AiClientStrategy;
import com.quan.diabetes.aiAPI.dto.AiGenerateOptions;
import com.quan.diabetes.dto.AIChat.OllamaGenerateRequest;
import com.quan.diabetes.dto.AIChat.OllamaGenerateResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class OllamaAiClient implements AiClientStrategy {

    private static final Logger logger = LoggerFactory.getLogger(OllamaAiClient.class);

    private final RestTemplate restTemplate;

    @Value("${ollama.url:http://localhost:11434}")
    private String ollamaUrl;

    @Value("${ollama.model:diabetes-ai}")
    private String ollamaDefaultModel;

    @Value("${ollama.num-ctx:8192}")
    private int ollamaNumCtx;

    @Value("${ollama.num-predict:2048}")
    private int ollamaNumPredict;

    public OllamaAiClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public String generate(String prompt, String systemPrompt, AiGenerateOptions options) {
        try {
            String model = (options != null && options.getModelName() != null && !options.getModelName().isBlank())
                    ? options.getModelName()
                    : ollamaDefaultModel;

            double temp = (options != null && options.getTemperature() != null) ? options.getTemperature() : 0.15;
            double topP = (options != null && options.getTopP() != null) ? options.getTopP() : 0.9;
            int numPredict = (options != null && options.getMaxOutputTokens() != null && options.getMaxOutputTokens() > 0)
                    ? options.getMaxOutputTokens()
                    : ollamaNumPredict;

            String baseUrl = ollamaUrl.endsWith("/") ? ollamaUrl.substring(0, ollamaUrl.length() - 1) : ollamaUrl;
            String url = baseUrl + "/api/generate";

            // Đặt repeat_penalty = 1.05 (thay vì 1.15) để tránh lỗi trừng phạt từ khi liệt kê thuốc trong đơn
            // Đặt num_ctx = ollamaNumCtx (mặc định 8192) để không bao giờ bị cắt xén dữ liệu RAG và system prompt
            OllamaGenerateRequest.Options ollamaOptions = new OllamaGenerateRequest.Options(
                    temp, topP, 20, 1.05, ollamaNumCtx, numPredict, 8
            );

            OllamaGenerateRequest generateRequest = new OllamaGenerateRequest(model, prompt, systemPrompt, false, ollamaOptions);

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

    @Override
    public String getProviderId() {
        return "LOCAL_OLLAMA";
    }

    @Override
    public String getDisplayName() {
        return "AI Local (Ollama)";
    }

    @Override
    public boolean supportsModel(String modelName) {
        if (modelName == null || modelName.isBlank()) return true;
        String lower = modelName.toLowerCase();
        return lower.contains("diabetes") || lower.contains("ollama") || lower.contains("llama") || lower.contains("qwen") || lower.contains("gemma");
    }
}
