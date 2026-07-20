package com.quan.diabetes.dto.AIChat;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record OllamaGenerateRequest(
        String model,
        String prompt,
        String system,
        boolean stream,
        Options options,
        @JsonProperty("keep_alive") String keepAlive
) {
    public OllamaGenerateRequest(String model, String prompt, String system, boolean stream, Options options) {
        this(model, prompt, system, stream, options, "24h");
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Options(
            double temperature,
            @JsonProperty("top_p") double topP,
            @JsonProperty("top_k") int topK,
            @JsonProperty("repeat_penalty") double repeatPenalty,
            @JsonProperty("num_ctx") int numCtx,
            @JsonProperty("num_predict") int numPredict,
            @JsonProperty("num_thread") Integer numThread
    ) {
        public Options(double temperature, double topP, int topK, double repeatPenalty, int numCtx, int numPredict) {
            this(temperature, topP, topK, repeatPenalty, numCtx, numPredict, 8);
        }
    }
}