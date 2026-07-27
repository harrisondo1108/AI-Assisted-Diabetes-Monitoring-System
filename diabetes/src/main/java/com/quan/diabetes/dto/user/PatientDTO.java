package com.quan.diabetes.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class PatientDTO {

    @NotBlank(message = "Vai trò không được để trống")
    private String roleId;

    @NotBlank(message = "Họ và tên không được để trống")
    @Size(max = 60, message = "Họ và tên không được vượt quá 60 ký tự")
    @Pattern(regexp = "^[\\p{L}\\s]+$", message = "Họ và tên chỉ được chứa chữ cái và khoảng trắng")
    private String fullName;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^[0-9]{10,11}$", message = "Số điện thoại phải từ 10-11 chữ số")
    private String phoneNumber;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$]).{8,}$",
        message = "Mật khẩu phải chứa ít nhất 8 ký tự, bao gồm chữ hoa, chữ thường, ít nhất một chữ số và ký tự đặc biệt (!@#$)."
    )
    private String password;

    @Pattern(regexp = "^$|^[A-Za-z0-9+_.-]+@(.+)$", message = "Địa chỉ email không hợp lệ.")
    @Size(max = 100, message = "Email không được vượt quá 100 ký tự.")
    private String email;

    private String dob;

    private String gender;

    private String bloodGroup;

    @Pattern(regexp = "^$|^([1-9]|[1-9][0-9]|[1-2][0-9][0-9])$", message = "Chiều cao không hợp lệ (phải từ 1 đến 299 cm)")
    private String height;

    @Pattern(regexp = "^$|^([1-9]|[1-9][0-9]|[1-9][0-9][0-9])(\\.[0-9]+)?$", message = "Cân nặng không hợp lệ (phải từ 1 đến 999.9 kg)")
    private String weight;

    @Size(max = 200, message = "Địa chỉ không được vượt quá 200 ký tự")
    private String address;

    private String medicalHistory;

    private String allergyNotes;

    // Getters and Setters
    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getBloodGroup() {
        return bloodGroup;
    }

    public void setBloodGroup(String bloodGroup) {
        this.bloodGroup = bloodGroup;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getMedicalHistory() {
        return medicalHistory;
    }

    public void setMedicalHistory(String medicalHistory) {
        this.medicalHistory = medicalHistory;
    }

    public String getAllergyNotes() {
        return allergyNotes;
    }

    public void setAllergyNotes(String allergyNotes) {
        this.allergyNotes = allergyNotes;
    }
}
