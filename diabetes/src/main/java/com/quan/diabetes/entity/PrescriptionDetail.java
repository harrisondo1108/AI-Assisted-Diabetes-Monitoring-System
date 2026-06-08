package com.quan.diabetes.entity;

import jakarta.persistence.*;
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
}


