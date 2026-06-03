package com.quan.diabetes.entity;

import jakarta.persistence.*;

import java.time.LocalTime;

@Entity
@Table(name = "PatientRoutine")
public class PatientRoutine {

    @Id
    @Column(name = "UserID", length = 50)
    private String userId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "UserID")
    private Patient patient;

    @Column(name = "BreakfastTime")
    private LocalTime breakfastTime = LocalTime.of(7, 0);;

    @Column(name = "LunchTime")
    private LocalTime lunchTime = LocalTime.of(11, 0);;

    @Column(name = "DinnerTime")
    private LocalTime dinnerTime = LocalTime.of(18, 0);;

    @Column(name = "WakeUpTime")
    private LocalTime wakeUpTime = LocalTime.of(6, 0);;

    @Column(name = "SleepTime")
    private LocalTime sleepTime = LocalTime.of(22, 0);;

    public PatientRoutine() {}

    // Getters and Setters


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

    @Override
    public String toString() {
        return "PatientRoutine{" +
                "userId='" + userId + '\'' +
                ", patient=" + patient +
                ", breakfastTime=" + breakfastTime +
                ", lunchTime=" + lunchTime +
                ", dinnerTime=" + dinnerTime +
                ", wakeUpTime=" + wakeUpTime +
                ", sleepTime=" + sleepTime +
                '}';
    }
}