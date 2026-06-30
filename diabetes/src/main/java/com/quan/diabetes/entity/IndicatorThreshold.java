package com.quan.diabetes.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "IndicatorThreshold")
public class IndicatorThreshold {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ThresholdID")
    private Integer thresholdId;

    @ManyToOne
    @JoinColumn(name = "LabTestID", nullable = false)
    private LabTestCatalog labTest;

    @ManyToOne
    @JoinColumn(name = "PatientTypeID", nullable = false)
    private PatientType patientType;

    @Column(name = "MinValue", precision = 10, scale = 2)
    private BigDecimal minValue;

    @Column(name = "MaxValue", precision = 10, scale = 2)
    private BigDecimal maxValue;

    @Column(name = "CreatedAt")
    private LocalDateTime createdAt;

    public IndicatorThreshold() {}

    // Getters and Setters
    public Integer getThresholdId() {
        return thresholdId;
    }

    public void setThresholdId(Integer thresholdId) {
        this.thresholdId = thresholdId;
    }

    public LabTestCatalog getLabTest() {
        return labTest;
    }

    public void setLabTest(LabTestCatalog labTest) {
        this.labTest = labTest;
    }

    public PatientType getPatientType() {
        return patientType;
    }

    public void setPatientType(PatientType patientType) {
        this.patientType = patientType;
    }

    public BigDecimal getMinValue() {
        return minValue;
    }

    public void setMinValue(BigDecimal minValue) {
        this.minValue = minValue;
    }

    public BigDecimal getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(BigDecimal maxValue) {
        this.maxValue = maxValue;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}