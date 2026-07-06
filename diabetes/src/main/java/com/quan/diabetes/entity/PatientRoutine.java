package com.quan.diabetes.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import jakarta.persistence.Transient;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PostLoad;
import org.springframework.data.domain.Persistable;

import java.time.LocalTime;

@Entity
@Table(name = "PatientRoutine")
public class PatientRoutine implements Persistable<String> {

    @Id
    @Column(name = "UserID", length = 50)
    private String userId;

    @Transient
    private boolean isNew = true;

    @Override
    public String getId() {
        return this.userId;
    }

    @Override
    public boolean isNew() {
        return this.isNew;
    }

    @PostPersist
    @PostLoad
    protected void markNotNew() {
        this.isNew = false;
    }

    /*
        Không dùng @MapsId ở đây để tránh lỗi:
        null identifier (com.quan.diabetes.entity.PatientRoutine)

        UserID vừa là PK của PatientRoutine, vừa là FK sang Patient.
        Mapping patient chỉ dùng để đọc, không dùng để insert/update UserID.
    */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserID", referencedColumnName = "UserID", insertable = false, updatable = false)
    private Patient patient;

    @Column(name = "BreakfastTime")
    private LocalTime breakfastTime = LocalTime.of(7, 0);

    @Column(name = "LunchTime")
    private LocalTime lunchTime = LocalTime.of(12, 0);

    @Column(name = "DinnerTime")
    private LocalTime dinnerTime = LocalTime.of(18, 0);

    @Column(name = "WakeUpTime")
    private LocalTime wakeUpTime = LocalTime.of(6, 0);

    @Column(name = "SleepTime")
    private LocalTime sleepTime = LocalTime.of(22, 0);

    public PatientRoutine() {
    }

    public PatientRoutine(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;

        if (patient != null) {
            this.userId = patient.getUserId();
        }
    }

    public LocalTime getBreakfastTime() {
        return breakfastTime;
    }

    public void setBreakfastTime(LocalTime breakfastTime) {
        this.breakfastTime = breakfastTime;
    }

    public LocalTime getLunchTime() {
        return lunchTime;
    }

    public void setLunchTime(LocalTime lunchTime) {
        this.lunchTime = lunchTime;
    }

    public LocalTime getDinnerTime() {
        return dinnerTime;
    }

    public void setDinnerTime(LocalTime dinnerTime) {
        this.dinnerTime = dinnerTime;
    }

    public LocalTime getWakeUpTime() {
        return wakeUpTime;
    }

    public void setWakeUpTime(LocalTime wakeUpTime) {
        this.wakeUpTime = wakeUpTime;
    }

    public LocalTime getSleepTime() {
        return sleepTime;
    }

    public void setSleepTime(LocalTime sleepTime) {
        this.sleepTime = sleepTime;
    }
}