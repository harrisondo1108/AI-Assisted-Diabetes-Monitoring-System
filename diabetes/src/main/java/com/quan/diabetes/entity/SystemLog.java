package com.quan.diabetes.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "SystemLog")
public class SystemLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "LogID")
    private Integer logId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "AccountID", referencedColumnName = "UserID")
    private User account;

    @Column(name = "Action", length = 50, nullable = false)
    private String action;

    @Column(name = "EntityName", length = 100)
    private String entityName;

    @Column(name = "EntityID", length = 100)
    private String entityId;

    @Column(name = "Description", columnDefinition = "NVARCHAR(1000)")
    private String description;

    @Column(name = "OldValue", columnDefinition = "NVARCHAR(MAX)")
    private String oldValue;

    @Column(name = "NewValue", columnDefinition = "NVARCHAR(MAX)")
    private String newValue;

    @Column(name = "Status", length = 20)
    private String status;

    @Column(name = "CreatedAt")
    private LocalDateTime createdAt;

    public SystemLog() {
    }

    public Integer getLogId() {
        return logId;
    }

    public void setLogId(Integer logId) {
        this.logId = logId;
    }

    public User getAccount() {
        return account;
    }

    public void setAccount(User account) {
        this.account = account;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOldValue() {
        return oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
