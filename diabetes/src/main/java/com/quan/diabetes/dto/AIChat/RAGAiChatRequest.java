package com.quan.diabetes.dto.AIChat;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RAGAiChatRequest {
    private String patientId;
    private String message;

    @JsonProperty("conversation_history")
    private String conversationHistory;

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getConversationHistory() {
        return conversationHistory;
    }

    public void setConversationHistory(String conversationHistory) {
        this.conversationHistory = conversationHistory;
    }
}
