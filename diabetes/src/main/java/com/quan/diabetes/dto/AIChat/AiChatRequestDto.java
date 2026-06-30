package com.quan.diabetes.dto.AIChat;

public record AiChatRequestDto(
        String question,
        String conversationId,
        String patientId
) {}