package com.quan.diabetes.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "Symptoms_Catalog")
public class SymptomsCatalog {

    @Id
    @Column(name = "SymptomID", length = 50)
    private String symptomId;

    @Column(name = "SymptomName", length = 200, unique = true, columnDefinition = "NVARCHAR(200)")
    private String symptomName;
    @Column(name = "Status", columnDefinition = "BIT")
    private Boolean status = true; // true: unlock  false: lock

    public SymptomsCatalog() {
    }

    public String getSymptomId() {
        return symptomId;
    }

    public void setSymptomId(String symptomId) {
        this.symptomId = symptomId;
    }

    public String getSymptomName() {
        return symptomName;
    }

    public void setSymptomName(String symptomName) {
        this.symptomName = symptomName;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }
}


