package com.quan.diabetes.dto.doctor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class DoctorProfileForm {

    @NotBlank(message = "Họ và tên bắt buộc phải điền.")
    @Size(min = 2, max = 60, message = "Họ và tên phải có độ dài từ 2 đến 60 ký tự.")
    @Pattern(regexp = "^[A-Za-zÀ-ỹ\\s]+$", message = "Họ và tên chỉ được chứa chữ cái và khoảng trắng.")
    private String fullName;

    private String dob;

    @NotNull(message = "Vui lòng chọn giới tính.")
    private Boolean gender;

    @Size(max = 60, message = "Chuyên khoa không được vượt quá 60 ký tự.")
    private String specialty;

    @Size(max = 200, message = "Địa chỉ không được vượt quá 200 ký tự.")
    private String address;

    private Integer roomId;

    @Pattern(regexp = "^$|^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$", message = "Email không đúng định dạng.")
    @Size(max = 100, message = "Email không được vượt quá 100 ký tự.")
    private String email;

    public DoctorProfileForm() {
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public Boolean getGender() {
        return gender;
    }

    public void setGender(Boolean gender) {
        this.gender = gender;
    }

    public String getSpecialty() {
        return specialty;
    }

    public void setSpecialty(String specialty) {
        this.specialty = specialty;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Integer getRoomId() {
        return roomId;
    }

    public void setRoomId(Integer roomId) {
        this.roomId = roomId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
