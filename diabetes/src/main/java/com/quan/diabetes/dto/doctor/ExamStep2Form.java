package com.quan.diabetes.dto.doctor;

import jakarta.validation.constraints.NotBlank;

/**
 * Form data for Tab 2: Lab Tests & Pregnancy flag
 */
public class ExamStep2Form {

    /** true if the patient is currently pregnant (female patients only) */
    private Boolean isPregnant;

    /** When true, triggers "order all lab tests" action */
    private Boolean orderLabs;

    @NotBlank(message = "Ghi chú chẩn đoán lâm sàng không được để trống và không được chứa mỗi space")
    private String diagnosisNote;

    // ---- Getters & Setters ----

    public Boolean getIsPregnant() {
        return isPregnant;
    }

    public void setIsPregnant(Boolean isPregnant) {
        this.isPregnant = isPregnant;
    }

    public Boolean getOrderLabs() {
        return orderLabs;
    }

    public void setOrderLabs(Boolean orderLabs) {
        this.orderLabs = orderLabs;
    }

    public String getDiagnosisNote() {
        return diagnosisNote;
    }

    public void setDiagnosisNote(String diagnosisNote) {
        this.diagnosisNote = diagnosisNote;
    }
}
