package com.quan.diabetes.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "PatientType")
public class PatientType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PatientTypeID")
    private Integer patientTypeId;

    @Column(name = "TypeName", nullable = false, length = 50, columnDefinition = "NVARCHAR(50)")
    private String typeName;

    @Column(name = "MinAge")
    private Integer minAge;

    @Column(name = "MaxAge")
    private Integer maxAge;

    public PatientType() {}

    // Getters and Setters
    public Integer getPatientTypeId() {
        return patientTypeId;
    }

    public void setPatientTypeId(Integer patientTypeId) {
        this.patientTypeId = patientTypeId;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public Integer getMinAge() {
        return minAge;
    }

    public void setMinAge(Integer minAge) {
        this.minAge = minAge;
    }

    public Integer getMaxAge() {
        return maxAge;
    }

    public void setMaxAge(Integer maxAge) {
        this.maxAge = maxAge;
    }

    @Override
    public String toString() {
        return "PatientType{" +
                "patientTypeId=" + patientTypeId +
                ", typeName='" + typeName + '\'' +
                ", minAge=" + minAge +
                ", maxAge=" + maxAge +
                '}';
    }
}