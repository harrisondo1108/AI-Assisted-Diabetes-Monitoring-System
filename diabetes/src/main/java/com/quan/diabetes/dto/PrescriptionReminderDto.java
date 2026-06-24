package com.quan.diabetes.dto;

import com.quan.diabetes.entity.TreatmentPlan;

import java.time.LocalDate;

/**
 * DTO used to describe medication reminders for a clinical examination (session).
 */
public class PrescriptionReminderDto {
    private String patientId;
    private String clinicalExamId;
    private String medicationName;
    private String dosage;
    private LocalDate startDate;
    private LocalDate endDate;
    private String form;
    private String administrationRoute;
    private String usageInstruction;
    private String timingName;
    private String medicationPlan;
    private TreatmentPlan treatmentPlan;

    // Constructor used by JPQL new expression


    public PrescriptionReminderDto(String patientId, String clinicalExamId, String medicationName, String dosage, LocalDate startDate, LocalDate endDate, String form, String administrationRoute, String usageInstruction, String timingName, String medicationPlan, TreatmentPlan treatmentPlan) {
        this.patientId = patientId;
        this.clinicalExamId = clinicalExamId;
        this.medicationName = medicationName;
        this.dosage = dosage;
        this.startDate = startDate;
        this.endDate = endDate;
        this.form = form;
        this.administrationRoute = administrationRoute;
        this.usageInstruction = usageInstruction;
        this.timingName = timingName;
        this.medicationPlan = medicationPlan;
        this.treatmentPlan = treatmentPlan;
    }

    // Getters and setters
    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public String getClinicalExamId() { return clinicalExamId; }
    public void setClinicalExamId(String clinicalExamId) { this.clinicalExamId = clinicalExamId; }

    public String getMedicationName() { return medicationName; }
    public void setMedicationName(String medicationName) { this.medicationName = medicationName; }

    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }

    public String getForm() { return form; }
    public void setForm(String form) { this.form = form; }

    public String getAdministrationRoute() { return administrationRoute; }
    public void setAdministrationRoute(String administrationRoute) { this.administrationRoute = administrationRoute; }

    public String getUsageInstruction() { return usageInstruction; }
    public void setUsageInstruction(String usageInstruction) { this.usageInstruction = usageInstruction; }

    public String getTimingName() { return timingName; }
    public void setTimingName(String timingName) { this.timingName = timingName; }

    public String getMedicationPlan() { return medicationPlan; }
    public void setMedicationPlan(String medicationPlan) { this.medicationPlan = medicationPlan; }

    public TreatmentPlan getTreatmentPlan() { return treatmentPlan; }
    public void setTreatmentPlan(TreatmentPlan treatmentPlan) { this.treatmentPlan = treatmentPlan; }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    @Override
    public String toString() {
        return "PrescriptionReminderDto{" +
                "patientId='" + patientId + '\'' +
                ", clinicalExamId='" + clinicalExamId + '\'' +
                ", medicationName='" + medicationName + '\'' +
                ", dosage='" + dosage + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", form='" + form + '\'' +
                ", administrationRoute='" + administrationRoute + '\'' +
                ", usageInstruction='" + usageInstruction + '\'' +
                ", timingName='" + timingName + '\'' +
                ", medicationPlan='" + medicationPlan + '\'' +
                ", treatmentPlan=" + treatmentPlan +
                '}';
    }
}


