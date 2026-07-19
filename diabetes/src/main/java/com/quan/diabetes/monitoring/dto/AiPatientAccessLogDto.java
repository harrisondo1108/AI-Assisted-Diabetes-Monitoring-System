package com.quan.diabetes.monitoring.dto;

import java.time.LocalDateTime;

public class AiPatientAccessLogDto {
    private Long id;
    private Long queryLogId;
    private String patientId;
    private String dataType;
    private LocalDateTime accessedAt;
    private String question;

    public AiPatientAccessLogDto() {
    }

    public AiPatientAccessLogDto(Long id, Long queryLogId, String patientId, String dataType, LocalDateTime accessedAt, String question) {
        this.id = id;
        this.queryLogId = queryLogId;
        this.patientId = patientId;
        this.dataType = dataType;
        this.accessedAt = accessedAt;
        this.question = question;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getQueryLogId() {
        return queryLogId;
    }

    public void setQueryLogId(Long queryLogId) {
        this.queryLogId = queryLogId;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public LocalDateTime getAccessedAt() {
        return accessedAt;
    }

    public void setAccessedAt(LocalDateTime accessedAt) {
        this.accessedAt = accessedAt;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }
}
