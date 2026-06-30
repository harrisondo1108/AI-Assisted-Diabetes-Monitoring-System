package com.quan.diabetes.dto.AIChat;

import java.time.LocalDateTime;
import java.util.List;

public record ConversationHistoryDto(
        String conversationId,
        List<MessageItem> messages
) {
    public record MessageItem(
            String sender,
            String content,
            LocalDateTime time
    ) {}
}