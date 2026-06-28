package com.quan.diabetes.dto.AIChat;

public record OllamaGenerateResponse(
        String model,
        String response,
        boolean done
) {}