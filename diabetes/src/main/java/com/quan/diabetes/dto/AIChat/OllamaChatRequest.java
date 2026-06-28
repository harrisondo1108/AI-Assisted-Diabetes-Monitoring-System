package com.quan.diabetes.dto.AIChat;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record OllamaChatRequest(
        String model,
        List<Message> messages,
        boolean stream,
        Options options
) {
    public record Message(String role, String content) {}
    public record Options(
            double temperature,
            @JsonProperty("top_p") double topP,
            @JsonProperty("top_k") int topK,
            @JsonProperty("repeat_penalty") double repeatPenalty,
            @JsonProperty("num_ctx") int numCtx,
            @JsonProperty("num_predict") int numPredict
    ) {}
}