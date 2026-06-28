package com.quan.diabetes.dto.AIChat;

public record ChatRequestDto(
        String question,
        String conversationId,
        String patientId
) {}