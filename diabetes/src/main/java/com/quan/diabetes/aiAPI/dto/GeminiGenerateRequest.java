package com.quan.diabetes.aiAPI.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record GeminiGenerateRequest(
        @JsonProperty("system_instruction") Content systemInstruction,
        List<Content> contents,
        @JsonProperty("generationConfig") GenerationConfig generationConfig
) {
    public static GeminiGenerateRequest create(String prompt, String systemPrompt, double temperature, double topP, int maxTokens) {
        Content sysContent = (systemPrompt != null && !systemPrompt.isBlank())
                ? new Content(List.of(new Part(systemPrompt)), null)
                : null;
        Content userContent = new Content(List.of(new Part(prompt)), "user");
        GenerationConfig config = new GenerationConfig(temperature, topP, maxTokens);
        return new GeminiGenerateRequest(sysContent, List.of(userContent), config);
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Content(
            List<Part> parts,
            String role
    ) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Part(
            String text
    ) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record GenerationConfig(
            double temperature,
            @JsonProperty("topP") double topP,
            @JsonProperty("maxOutputTokens") Integer maxOutputTokens
    ) {}
}
