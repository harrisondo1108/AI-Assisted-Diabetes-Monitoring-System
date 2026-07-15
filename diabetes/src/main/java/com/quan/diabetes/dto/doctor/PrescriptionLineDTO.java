package com.quan.diabetes.dto.doctor;

import java.util.List;

public class PrescriptionLineDTO {
    private String medId;
    private String name;
    private String concentration;
    private String form;
    private String dosage;
    private Double dosagePerDose;
    private Integer duration;
    private Integer quantity;
    private List<String> timing;
    private String timingText;
    private String medicationPlan;
    private String startDate;
    private String endDate;

    public String getMedId() { return medId; }
    public void setMedId(String medId) { this.medId = medId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getConcentration() { return concentration; }
    public void setConcentration(String concentration) { this.concentration = concentration; }
    public String getForm() { return form; }
    public void setForm(String form) { this.form = form; }
    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }
    public Double getDosagePerDose() { return dosagePerDose; }
    public void setDosagePerDose(Double dosagePerDose) { this.dosagePerDose = dosagePerDose; }
    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public List<String> getTiming() { return timing; }
    public void setTiming(List<String> timing) { this.timing = timing; }
    public String getTimingText() { return timingText; }
    public void setTimingText(String timingText) { this.timingText = timingText; }
    public String getMedicationPlan() { return medicationPlan; }
    public void setMedicationPlan(String medicationPlan) { this.medicationPlan = medicationPlan; }
    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }
}
