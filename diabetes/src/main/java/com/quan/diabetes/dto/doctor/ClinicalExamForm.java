package com.quan.diabetes.dto.doctor;

import java.util.List;
import java.util.Map;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

/**
 * Form backing object to bind clinical examination form fields from Thymeleaf
 * views.
 */
public class ClinicalExamForm {

    @NotBlank(message = "Lý do khám không được để trống")
    private String medicalHistory;
    @NotBlank(message = "Ghi chú chẩn đoán lâm sàng không được để trống")
    private String diagnosisNote;
    private String nextAppointment;
    private List<String> symptomIds;
    private String prescriptionJson;
    private Map<String, String> symptomComments;

    // Treatment plan details
    @Pattern(regexp = "^$|.*\\S.*", message = "Mục tiêu điều trị không được chỉ chứa khoảng trắng")
    private String treatmentGoal;
    @Pattern(regexp = "^$|.*\\S.*", message = "Chế độ dinh dưỡng không được chỉ chứa khoảng trắng")
    private String dietPlan;
    @Pattern(regexp = "^$|.*\\S.*", message = "Chế độ tập luyện không được chỉ chứa khoảng trắng")
    private String exercisePlan;
    @Pattern(regexp = "^$|.*\\S.*", message = "Kế hoạch theo dõi đường huyết không được chỉ chứa khoảng trắng")
    private String glucoseMonitoringPlan;
    @Pattern(regexp = "^$|.*\\S.*", message = "Kế hoạch dùng thuốc không được chỉ chứa khoảng trắng")
    private String medicationPlan;
    private Boolean isPregnant;

    // Getters and Setters
    public String getMedicalHistory() {
        return medicalHistory;
    }

    public void setMedicalHistory(String medicalHistory) {
        this.medicalHistory = medicalHistory;
    }

    public String getDiagnosisNote() {
        return diagnosisNote;
    }

    public void setDiagnosisNote(String diagnosisNote) {
        this.diagnosisNote = diagnosisNote;
    }

    public String getNextAppointment() {
        return nextAppointment;
    }

    public void setNextAppointment(String nextAppointment) {
        this.nextAppointment = nextAppointment;
    }

    public List<String> getSymptomIds() {
        return symptomIds;
    }

    public void setSymptomIds(List<String> symptomIds) {
        this.symptomIds = symptomIds;
    }

    public String getPrescriptionJson() {
        return prescriptionJson;
    }

    public void setPrescriptionJson(String prescriptionJson) {
        this.prescriptionJson = prescriptionJson;
    }

    public String getTreatmentGoal() {
        return treatmentGoal;
    }

    public void setTreatmentGoal(String treatmentGoal) {
        this.treatmentGoal = treatmentGoal;
    }

    public String getDietPlan() {
        return dietPlan;
    }

    public void setDietPlan(String dietPlan) {
        this.dietPlan = dietPlan;
    }

    public String getExercisePlan() {
        return exercisePlan;
    }

    public void setExercisePlan(String exercisePlan) {
        this.exercisePlan = exercisePlan;
    }

    public String getGlucoseMonitoringPlan() {
        return glucoseMonitoringPlan;
    }

    public void setGlucoseMonitoringPlan(String glucoseMonitoringPlan) {
        this.glucoseMonitoringPlan = glucoseMonitoringPlan;
    }

    public String getMedicationPlan() {
        return medicationPlan;
    }

    public void setMedicationPlan(String medicationPlan) {
        this.medicationPlan = medicationPlan;
    }

    private String symptomCommentsJson;

    public String getSymptomCommentsJson() {
        return symptomCommentsJson;
    }

    public void setSymptomCommentsJson(String symptomCommentsJson) {
        this.symptomCommentsJson = symptomCommentsJson;
    }

    public Boolean getIsPregnant() {
        return isPregnant;
    }

    public void setIsPregnant(Boolean isPregnant) {
        this.isPregnant = isPregnant;
    }

    public Map<String, String> getSymptomComments() {
        return symptomComments;
    }

    public void setSymptomComments(Map<String, String> symptomComments) {
        this.symptomComments = symptomComments;
    }

    @Override
    public String toString() {
        return "ClinicalExamForm{" +
                "medicalHistory='" + medicalHistory + '\'' +
                ", diagnosisNote='" + diagnosisNote + '\'' +
                ", nextAppointment='" + nextAppointment + '\'' +
                ", symptomIds=" + symptomIds +
                ", prescriptionJson='" + prescriptionJson + '\'' +
                ", treatmentGoal='" + treatmentGoal + '\'' +
                ", dietPlan='" + dietPlan + '\'' +
                ", exercisePlan='" + exercisePlan + '\'' +
                ", glucoseMonitoringPlan='" + glucoseMonitoringPlan + '\'' +
                ", medicationPlan='" + medicationPlan + '\'' +
                '}';
    }
}
