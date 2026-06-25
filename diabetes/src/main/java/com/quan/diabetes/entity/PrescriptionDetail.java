package com.quan.diabetes.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


@Entity
@Table(name = "PrescriptionDetail")
public class PrescriptionDetail {

    @Id
    @Column(name = "PrescriptionDetailID", length = 50)
    private String prescriptionDetailId;

    @ManyToOne
    @JoinColumn(name = "PrescriptionID")
    private Prescription prescription;

    @ManyToOne
    @JoinColumn(name = "MedicationID")
    private Medication medication;

    @Column(name = "Dosage", length = 50)
    private String dosage;

    @OneToMany(
            mappedBy = "prescriptionDetail",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<PrescriptionTiming> prescriptionTimings;

    @Column(name = "TotalQuantity")
    private int totalQuantity;

    @Column(name = "DurationDays")
    private int durationDays;

    @Column(name = "StartDate")
    private LocalDate startDate;

    @Column(name = "EndDate")
    private LocalDate endDate;

    @Column(name = "MedicationPlan", columnDefinition = "NVARCHAR(MAX)")
    private String medicationPlan; // kế hoạch dùng thuốc, có thể là "Dùng liên tục", "Dùng khi cần thiết", "Dùng theo chu kỳ",....

    public PrescriptionDetail() {
    }
    public void addTiming(PrescriptionTiming timing){
        prescriptionTimings.add(timing);
        timing.setPrescriptionDetail(this);
    }

    public void removeTiming(PrescriptionTiming timing){
        prescriptionTimings.remove(timing);
        timing.setPrescriptionDetail(null);
    }
    public String getPrescriptionDetailId() {
        return prescriptionDetailId;
    }

    public void setPrescriptionDetailId(String prescriptionDetailId) {
        this.prescriptionDetailId = prescriptionDetailId;
    }

    public List<PrescriptionTiming> getPrescriptionTimings() {
        return prescriptionTimings;
    }

    public void setPrescriptionTimings(List<PrescriptionTiming> prescriptionTimings) {
        this.prescriptionTimings = prescriptionTimings;
    }

    public Prescription getPrescription() {
        return prescription;
    }

    public void setPrescription(Prescription prescription) {
        this.prescription = prescription;
    }

    public Medication getMedication() {
        return medication;
    }

    public void setMedication(Medication medication) {
        this.medication = medication;
    }

    public String getDosage() {
        return dosage;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    public int getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(int totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public int getDurationDays() {
        return durationDays;
    }

    public void setDurationDays(int durationDays) {
        this.durationDays = durationDays;
    }

    public String getMedicationPlan() {
        return medicationPlan;
    }

    public void setMedicationPlan(String medicationPlan) {
        this.medicationPlan = medicationPlan;
    }

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
}


