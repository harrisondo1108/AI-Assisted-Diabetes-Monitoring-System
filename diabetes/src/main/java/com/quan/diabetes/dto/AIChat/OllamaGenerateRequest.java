package com.quan.diabetes.dto.AIChat;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OllamaGenerateRequest(
        String model,
        String prompt,
        boolean stream,
        Options options
) {
    public record Options(
            double temperature,
            @JsonProperty("top_p") double topP,
            @JsonProperty("top_k") int topK,
            @JsonProperty("repeat_penalty") double repeatPenalty,
            @JsonProperty("num_ctx") int numCtx,
            @JsonProperty("num_predict") int numPredict
    ) {}
}