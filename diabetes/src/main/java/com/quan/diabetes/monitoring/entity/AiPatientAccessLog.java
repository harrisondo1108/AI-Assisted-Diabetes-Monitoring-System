package com.quan.diabetes.monitoring.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "ai_patient_access_log")
public class AiPatientAccessLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "queryLogId")
    private Long queryLogId;

    @Column(name = "patientId", nullable = false, length = 50)
    private String patientId;

    @Column(name = "dataType", nullable = false, length = 100)
    private String dataType;

    @Column(name = "accessedAt", nullable = false)
    private LocalDateTime accessedAt;

    public AiPatientAccessLog() {
    }

    public AiPatientAccessLog(Long queryLogId, String patientId, String dataType, LocalDateTime accessedAt) {
        this.queryLogId = queryLogId;
        this.patientId = patientId;
        this.dataType = dataType;
        this.accessedAt = accessedAt != null ? accessedAt : LocalDateTime.now();
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
}
