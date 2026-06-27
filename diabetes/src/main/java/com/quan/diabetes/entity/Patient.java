package com.quan.diabetes.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "Patient")
public class Patient {

    @Id
    @Column(name = "UserID", length = 50)
    private String userId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "UserID")
    private User user;

    @Column(name = "FullName", nullable = false, length = 60, columnDefinition = "NVARCHAR(60)")
    private String fullName;

    @Column(name = "PhoneNumber", length = 15, unique = true, columnDefinition = "NVARCHAR(15)")
    private String phoneNumber;

    @Column(name = "Address", length = 200, columnDefinition = "NVARCHAR(200)")
    private String address;

    @Column(name = "Dob")
    private LocalDate dob;

    @Column(name = "Gender")
    private Boolean gender;
    // true -> male, false -> female

    @Column(name = "Height")
    private Integer height;

    @Column(name = "Weight", precision = 5, scale = 2)
    private BigDecimal weight;

    @Column(name = "Bloodgroup", length = 3)
    private String bloodgroup;

    @Column(name = "PermanentMedicalHistory", columnDefinition = "NVARCHAR(MAX)")
    private String permanentMedicalHistory;

    @Column(name = "AllergyNotes", columnDefinition = "NVARCHAR(MAX)")
    private String allergyNotes;

    @Column(name = "SupervisorName", length = 90, columnDefinition = "NVARCHAR(90)")
    private String supervisorName;

    @Column(name = "SupervisorPhone", length = 15, columnDefinition = "NVARCHAR(15)")
    private String supervisorPhone;

    @Column(name = "ImageURL", length = 255, columnDefinition = "NVARCHAR(255)")
    private String imageUrl;

    public Patient() {
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public LocalDate getDob() {
        return dob;
    }

    public void setDob(LocalDate dob) {
        this.dob = dob;
    }

    public Boolean getGender() {
        return gender;
    }

    public void setGender(Boolean gender) {
        this.gender = gender;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public BigDecimal getWeight() {
        return weight;
    }

    public void setWeight(BigDecimal weight) {
        this.weight = weight;
    }

    public String getBloodgroup() {
        return bloodgroup;
    }

    public void setBloodgroup(String bloodgroup) {
        this.bloodgroup = bloodgroup;
    }

    public String getPermanentMedicalHistory() {
        return permanentMedicalHistory;
    }

    public void setPermanentMedicalHistory(String permanentMedicalHistory) {
        this.permanentMedicalHistory = permanentMedicalHistory;
    }

    public String getAllergyNotes() {
        return allergyNotes;
    }

    public void setAllergyNotes(String allergyNotes) {
        this.allergyNotes = allergyNotes;
    }

    public String getSupervisorName() {
        return supervisorName;
    }

    public void setSupervisorName(String supervisorName) {
        this.supervisorName = supervisorName;
    }

    public String getSupervisorPhone() {
        return supervisorPhone;
    }

    public void setSupervisorPhone(String supervisorPhone) {
        this.supervisorPhone = supervisorPhone;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}



