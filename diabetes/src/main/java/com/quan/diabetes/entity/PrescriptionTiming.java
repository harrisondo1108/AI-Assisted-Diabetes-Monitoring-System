package com.quan.diabetes.entity;

import jakarta.persistence.*;

@Entity
@Table(
        name = "PrescriptionTiming",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"PrescriptionDetailID", "TimingID"}
                )
        }
)
public class PrescriptionTiming {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PrescriptionTimingID")
    private Long prescriptionTimingID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "PrescriptionDetailID",
            nullable = false
    )
    private PrescriptionDetail prescriptionDetail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "TimingID",
            nullable = false
    )
    private MedicationTiming timing;

    public PrescriptionTiming() {
    }

    public Long getPrescriptionTimingID() {
        return prescriptionTimingID;
    }

    public void setPrescriptionTimingID(Long prescriptionTimingID) {
        this.prescriptionTimingID = prescriptionTimingID;
    }

    public PrescriptionDetail getPrescriptionDetail() {
        return prescriptionDetail;
    }

    public void setPrescriptionDetail(PrescriptionDetail prescriptionDetail) {
        this.prescriptionDetail = prescriptionDetail;
    }

    public MedicationTiming getTiming() {
        return timing;
    }

    public void setTiming(MedicationTiming timing) {
        this.timing = timing;
    }
}
