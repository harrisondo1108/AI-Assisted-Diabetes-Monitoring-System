package com.quan.diabetes.dto.AIChat;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RAGPythonAiRequest {
    @JsonProperty("patient_id")
    private String patientId;

    private String message;

    @JsonProperty("context_data")
    private String contextData;

    @JsonProperty("conversation_history")
    private String conversationHistory;

    public RAGPythonAiRequest() {
    }

    public RAGPythonAiRequest(String patientId, String message, String contextData, String conversationHistory) {
        this.patientId = patientId;
        this.message = message;
        this.contextData = contextData;
        this.conversationHistory = conversationHistory;
    }

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

    public String getContextData() {
        return contextData;
    }

    public void setContextData(String contextData) {
        this.contextData = contextData;
    }

    public String getConversationHistory() {
        return conversationHistory;
    }

    public void setConversationHistory(String conversationHistory) {
        this.conversationHistory = conversationHistory;
    }

    @Override
    public String toString() {
        return "RAGPythonAiRequest{" +
                "patientId='" + patientId + '\'' +
                ", message='" + message + '\'' +
                ", contextData='" + contextData + '\'' +
                ", conversationHistory='" + conversationHistory + '\'' +
                '}';
    }
}
