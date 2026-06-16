package com.quan.diabetes.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PostLoad;
import org.springframework.data.domain.Persistable;
import java.time.LocalDate;

@Entity
@Table(name = "Profile")
public class Profile implements Persistable<String> {

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

    @OneToOne
    @MapsId
    @JoinColumn(name = "UserID")
    private User user;

    @Column(name = "FullName", nullable = false, length = 60, columnDefinition = "NVARCHAR(60)")
    private String fullName;

    @Column(name = "PhoneNumber", length = 15, unique = true)
    private String phoneNumber;

    @Column(name = "Address", length = 200, columnDefinition = "NVARCHAR(200)")
    private String address;

    @Column(name = "Dob")
    private LocalDate dob;

    @Column(name = "Gender")
    private Boolean gender;

    @ManyToOne
    @JoinColumn(name = "RoomID")
    private Room room;

    @Column(name = "Specialty", length = 60, columnDefinition = "NVARCHAR(60)")
    private String specialty;

    public Profile() {
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

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public String getSpecialty() {
        return specialty;
    }

    public void setSpecialty(String specialty) {
        this.specialty = specialty;
    }
}


