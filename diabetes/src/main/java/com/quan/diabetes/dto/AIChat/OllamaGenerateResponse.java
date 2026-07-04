package com.quan.diabetes.dto.AIChat;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OllamaGenerateResponse(
        String model,
        @JsonProperty("created_at") String createdAt,
        String response,
        boolean done
) {}
