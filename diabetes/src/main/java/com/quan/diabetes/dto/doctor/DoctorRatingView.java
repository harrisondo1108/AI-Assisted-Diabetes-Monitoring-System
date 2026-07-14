package com.quan.diabetes.dto.doctor;

public class DoctorRatingView {
    private String doctorId;
    private String fullName;
    private String specialty;
    private String roomName;
    private String imageUrl;
    private Double averageRating;
    private Long ratingCount;

    public DoctorRatingView() {
    }

    public DoctorRatingView(String doctorId, String fullName, String specialty, String roomName, String imageUrl, Double averageRating, Long ratingCount) {
        this.doctorId = doctorId;
        this.fullName = fullName;
        this.specialty = specialty;
        this.roomName = roomName;
        this.imageUrl = imageUrl;
        this.averageRating = averageRating;
        this.ratingCount = ratingCount;
    }

    public String getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(String doctorId) {
        this.doctorId = doctorId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getSpecialty() {
        return specialty;
    }

    public void setSpecialty(String specialty) {
        this.specialty = specialty;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }

    public Long getRatingCount() {
        return ratingCount;
    }

    public void setRatingCount(Long ratingCount) {
        this.ratingCount = ratingCount;
    }
}
