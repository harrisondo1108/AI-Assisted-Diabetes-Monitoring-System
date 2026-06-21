package com.quan.diabetes.dto;

import java.util.List;

/**
 * Form backing object to bind clinical examination form fields from Thymeleaf views.
 */
public class ClinicalExamForm {

    private String medicalHistory;
    private String diagnosisNote;
    private String nextAppointment; // Receives yyyy-MM-dd date string
    private List<String> symptomIds;
    private List<String> labTestIds;
    private String prescriptionJson; // Receives serialized JSON string of medications

    // Treatment plan details
    private String treatmentGoal;
    private String dietPlan;
    private String exercisePlan;
    private String glucoseMonitoringPlan;
    private String medicationPlan;

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

    public List<String> getLabTestIds() {
        return labTestIds;
    }

    public void setLabTestIds(List<String> labTestIds) {
        this.labTestIds = labTestIds;
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
    private String labResultsJson;

    public String getSymptomCommentsJson() {
        return symptomCommentsJson;
    }

    public void setSymptomCommentsJson(String symptomCommentsJson) {
        this.symptomCommentsJson = symptomCommentsJson;
    }

    public String getLabResultsJson() {
        return labResultsJson;
    }

    public void setLabResultsJson(String labResultsJson) {
        this.labResultsJson = labResultsJson;
    }

    @Override
    public String toString() {
        return "ClinicalExamForm{" +
                "medicalHistory='" + medicalHistory + '\'' +
                ", diagnosisNote='" + diagnosisNote + '\'' +
                ", nextAppointment='" + nextAppointment + '\'' +
                ", symptomIds=" + symptomIds +
                ", labTestIds=" + labTestIds +
                ", prescriptionJson='" + prescriptionJson + '\'' +
                ", treatmentGoal='" + treatmentGoal + '\'' +
                ", dietPlan='" + dietPlan + '\'' +
                ", exercisePlan='" + exercisePlan + '\'' +
                ", glucoseMonitoringPlan='" + glucoseMonitoringPlan + '\'' +
                ", medicationPlan='" + medicationPlan + '\'' +
                '}';
    }
}
