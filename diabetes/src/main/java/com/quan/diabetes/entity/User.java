package com.quan.diabetes.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "Account")
public class User {

    public static final String STATUS_ACTIVE = "Active";
    public static final String STATUS_LOCKED = "Clocked";

    @Id
    @Column(name = "UserID", length = 50)
    private String userId;

    @Column(name = "PhoneNumber", nullable = false, unique = true, length = 50, columnDefinition = "NVARCHAR(50)")
    private String phoneNumber;

    @Column(name = "PasswordHash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "Status", nullable = false, length = 20, columnDefinition = "NVARCHAR(20) CHECK (Status IN ('Active', 'Clocked'))")
    private String status = STATUS_ACTIVE;

    @ManyToOne
    @JoinColumn(name = "RoleID")
    private Role role;

    @OneToOne(mappedBy = "user")
    private Profile profile;

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    public User() {
    }

    public User(String userId, String phoneNumber, String passwordHash, Role role) {
        this.userId = userId;
        this.phoneNumber = phoneNumber;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId='" + userId + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", status='" + status + '\'' +
                ", role=" + (role != null ? role.getRoleId() : null) +
                '}';
    }
}