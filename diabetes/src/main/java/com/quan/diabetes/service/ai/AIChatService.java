package com.quan.diabetes.service.ai;

import com.quan.diabetes.dto.AIChat.AIAssistantDto;
import com.quan.diabetes.dto.AIChat.ChatRequestDto;
import com.quan.diabetes.dto.AIChat.ChatResponseDto;
import com.quan.diabetes.dto.AIChat.ConversationHistoryDto;

import java.util.List;

public interface AIChatService {
    ChatResponseDto sendMessage(ChatRequestDto request);
    ChatResponseDto sendMessageWithAssistant(ChatRequestDto request, Integer assistantId);
    ConversationHistoryDto getConversationHistory(String conversationId);
    List<ConversationHistoryDto> getPatientConversations(String patientId);
    List<ConversationHistoryDto> getPatientConversationsWithAssistant(String patientId, Integer assistantId);
    void deleteConversation(String conversationId);
    List<AIAssistantDto> getAvailableAssistants();
}