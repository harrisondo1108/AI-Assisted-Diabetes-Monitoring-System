package com.quan.diabetes.dto.AIChat;

import java.time.LocalDateTime;

public record ChatResponseDto(
        String conversationId,
        String message,
        String sender,
        LocalDateTime timestamp,
        boolean success,
        String error
) {
    public static ChatResponseDto success(String conversationId, String message) {
        return new ChatResponseDto(conversationId, message, "AI", LocalDateTime.now(), true, null);
    }

    public static ChatResponseDto error(String error) {
        return new ChatResponseDto(null, null, null, null, false, error);
    }
}