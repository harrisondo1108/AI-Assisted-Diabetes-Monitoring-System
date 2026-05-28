package com.quan.diabetes.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Data Transfer Object used by admin UI to display and edit user information.
 * It aggregates fields from User, Patient and Profile entities.
 */
public class UserManagementDTO {
    private String userId;
    private String accountPhone;
    private String status;
    private String role;
    private String password; // plain password for creation; not persisted in DB

    // Patient / Profile common fields
    private String fullName;
    private String phoneNumber;
    private String address;
    private LocalDate dob;
    private Boolean gender; // true = male, false = female (or vice versa depending on app)

    // Patient‑specific fields
    private Integer height;
    private BigDecimal weight;
    private String bloodgroup;
    private String permanentMedicalHistory;
    private String allergyNotes;
    private String supervisorName;
    private String supervisorPhone;

    // Profile‑specific fields
    private String roomName;
    private String specialty;

    // Getters and Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getAccountPhone() { return accountPhone; }
    public void setAccountPhone(String accountPhone) { this.accountPhone = accountPhone; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public LocalDate getDob() { return dob; }
    public void setDob(LocalDate dob) { this.dob = dob; }
    public Boolean getGender() { return gender; }
    public void setGender(Boolean gender) { this.gender = gender; }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public BigDecimal getWeight() { return weight; }
    public void setWeight(BigDecimal weight) { this.weight = weight; }
    public String getBloodgroup() { return bloodgroup; }
    public void setBloodgroup(String bloodgroup) { this.bloodgroup = bloodgroup; }
    public String getPermanentMedicalHistory() { return permanentMedicalHistory; }
    public void setPermanentMedicalHistory(String permanentMedicalHistory) { this.permanentMedicalHistory = permanentMedicalHistory; }
    public String getAllergyNotes() { return allergyNotes; }
    public void setAllergyNotes(String allergyNotes) { this.allergyNotes = allergyNotes; }
    public String getSupervisorName() { return supervisorName; }
    public void setSupervisorName(String supervisorName) { this.supervisorName = supervisorName; }
    public String getSupervisorPhone() { return supervisorPhone; }
    public void setSupervisorPhone(String supervisorPhone) { this.supervisorPhone = supervisorPhone; }
    public String getRoomName() { return roomName; }
    public void setRoomName(String roomName) { this.roomName = roomName; }
    public String getSpecialty() { return specialty; }
    public void setSpecialty(String specialty) { this.specialty = specialty; }

    @Override
    public String toString() {
        return "UserManagementDTO{" +
                "userId='" + userId + '\'' +
                ", accountPhone='" + accountPhone + '\'' +
                ", status='" + status + '\'' +
                ", role='" + role + '\'' +
                ", password='" + password + '\'' +
                ", fullName='" + fullName + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", address='" + address + '\'' +
                ", dob=" + dob +
                ", gender=" + gender +
                ", height=" + height +
                ", weight=" + weight +
                ", bloodgroup='" + bloodgroup + '\'' +
                ", permanentMedicalHistory='" + permanentMedicalHistory + '\'' +
                ", allergyNotes='" + allergyNotes + '\'' +
                ", supervisorName='" + supervisorName + '\'' +
                ", supervisorPhone='" + supervisorPhone + '\'' +
                ", roomName='" + roomName + '\'' +
                ", specialty='" + specialty + '\'' +
                '}';
    }
}
