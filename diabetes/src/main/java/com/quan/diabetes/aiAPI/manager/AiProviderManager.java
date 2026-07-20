package com.quan.diabetes.aiAPI.manager;

import com.quan.diabetes.aiAPI.AiClientStrategy;
import com.quan.diabetes.aiAPI.dto.AiGenerateOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Manager/Factory điều phối các chiến lược kết nối AI (Ollama vs Gemini)
 * dựa vào cấu hình model của AI Assistant.
 */
@Service
public class AiProviderManager {

    private static final Logger logger = LoggerFactory.getLogger(AiProviderManager.class);

    private final List<AiClientStrategy> strategies;

    public AiProviderManager(List<AiClientStrategy> strategies) {
        this.strategies = strategies;
    }

    /**
     * Tìm AiClientStrategy phù hợp nhất để xử lý model Name cụ thể.
     */
    public AiClientStrategy getClientForModel(String modelName) {
        if (modelName != null && !modelName.isBlank()) {
            for (AiClientStrategy strategy : strategies) {
                if (strategy.supportsModel(modelName)) {
                    return strategy;
                }
            }
        }
        // Fallback mặc định về Ollama nếu không khớp hoặc model null
        return strategies.stream()
                .filter(s -> "LOCAL_OLLAMA".equals(s.getProviderId()))
                .findFirst()
                .orElse(strategies.get(0));
    }

    /**
     * Tìm AiClientStrategy theo providerId ("LOCAL_OLLAMA" hoặc "GEMINI").
     */
    public Optional<AiClientStrategy> getClientByProviderId(String providerId) {
        return strategies.stream()
                .filter(s -> s.getProviderId().equalsIgnoreCase(providerId))
                .findFirst();
    }

    /**
     * Sinh văn bản bằng strategy tương ứng với model.
     */
    public String generateWithModel(String modelName, String prompt, String systemPrompt, AiGenerateOptions options) {
        AiClientStrategy strategy = getClientForModel(modelName);
        logger.info("Routing generation request for model '{}' to strategy: {}", modelName, strategy.getDisplayName());

        AiGenerateOptions effectiveOptions = (options != null) ? options : new AiGenerateOptions();
        if (modelName != null && !modelName.isBlank()) {
            effectiveOptions.setModelName(modelName);
        }

        return strategy.generate(prompt, systemPrompt, effectiveOptions);
    }

    /**
     * Sinh văn bản theo luồng (streaming) bằng strategy tương ứng với model.
     */
    public void generateStreamWithModel(String modelName, String prompt, String systemPrompt, AiGenerateOptions options,
                                        java.util.function.Consumer<String> onChunk,
                                        Runnable onComplete,
                                        java.util.function.Consumer<Throwable> onError) {
        AiClientStrategy strategy = getClientForModel(modelName);
        logger.info("Routing stream request for model '{}' to strategy: {}", modelName, strategy.getDisplayName());

        AiGenerateOptions effectiveOptions = (options != null) ? options : new AiGenerateOptions();
        if (modelName != null && !modelName.isBlank()) {
            effectiveOptions.setModelName(modelName);
        }

        strategy.generateStream(prompt, systemPrompt, effectiveOptions, onChunk, onComplete, onError);
    }

    /**
     * Trả về danh sách tất cả các chiến lược AI đang có trong hệ thống.
     */
    public List<AiClientStrategy> getAllStrategies() {
        return strategies;
    }
}
