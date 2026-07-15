package com.quan.diabetes.dto.doctor;

import java.util.List;
import java.util.Map;
import jakarta.validation.constraints.NotBlank;

/**
 * Form data for Tab 1: Medical History & Symptoms
 */
public class ExamStep1Form {

    @NotBlank(message = "Lý do khám không được để trống và không được chứa mỗi space")
    private String medicalHistory;

    private List<String> symptomIds;

    /** Map symptomId -> note text */
    private Map<String, String> symptomComments;

    // ---- Getters & Setters ----

    public String getMedicalHistory() {
        return medicalHistory;
    }

    public void setMedicalHistory(String medicalHistory) {
        this.medicalHistory = medicalHistory;
    }

    public List<String> getSymptomIds() {
        return symptomIds;
    }

    public void setSymptomIds(List<String> symptomIds) {
        this.symptomIds = symptomIds;
    }

    public Map<String, String> getSymptomComments() {
        return symptomComments;
    }

    public void setSymptomComments(Map<String, String> symptomComments) {
        this.symptomComments = symptomComments;
    }
}
