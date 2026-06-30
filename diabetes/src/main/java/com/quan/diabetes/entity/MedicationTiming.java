package com.quan.diabetes.entity;

import jakarta.persistence.*;
import java.util.List;
@Entity
@Table(name = "MedicationTiming")
public class MedicationTiming {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TimingID")
    private Integer timingID;

    @Column(name = "TimingName", nullable = false, length = 100)
    private String timingName; // sau ăn sáng, trước ăn trưa, trước khi ngủ,....

//    @OneToMany(mappedBy = "timing")
//    private List<PrescriptionTiming> prescriptionTimings;

    public MedicationTiming() {
    }

    public MedicationTiming(String timingName) {
        this.timingName = timingName;
    }

    public Integer getTimingID() {
        return timingID;
    }

    public void setTimingID(Integer timingID) {
        this.timingID = timingID;
    }

    public String getTimingName() {
        return timingName;
    }

    public void setTimingName(String timingName) {
        this.timingName = timingName;
    }

}
