package com.quan.diabetes.dto.user;

import java.math.BigDecimal;
import java.time.LocalDate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object used by admin UI to display and edit user information.
 * It aggregates fields from User, Patient and Profile entities.
 */
public class UserManagementDTO {
    private String userId;

    @NotBlank(message = "Số điện thoại đăng nhập không được để trống")
    @Pattern(regexp = "^(0[35789])[0-9]{8}$", message = "Số điện thoại phải là số di động Việt Nam hợp lệ (10 chữ số, bắt đầu bằng 03, 05, 07, 08, 09)")
    private String accountPhone;

    private String status;
    private String role;
    private String password; // plain password for creation; not persisted in DB

    @NotBlank(message = "Họ và tên không được để trống")
    @Size(max = 60, message = "Họ và tên không được vượt quá 60 ký tự")
    @Pattern(regexp = "^[\\p{L}\\s]+$", message = "Họ và tên chỉ được chứa chữ cái và khoảng trắng")
    private String fullName;

    @Size(max = 200, message = "Địa chỉ không được vượt quá 200 ký tự")
    private String address;


    @NotNull(message = "Vui lòng chọn ngày sinh")
    @Past(message = "Ngày sinh phải ở trong quá khứ")
    private LocalDate dob;

    @NotNull(message = "Vui lòng chọn giới tính")
    private Boolean gender; // true = male, false = female (or vice versa depending on app)

    // Patient-specific fields
    private Integer height;
    private BigDecimal weight;

    private String bloodgroup;

    private String permanentMedicalHistory;
    private String allergyNotes;
    private String supervisorName;
    private String supervisorPhone;
    @Pattern(regexp = "^$|^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$", message = "Email không đúng định dạng")
    @Size(max = 100, message = "Email không được vượt quá 100 ký tự")
    private String email;
    private String roomName;

    @Size(max = 60, message = "Chuyên khoa không được vượt quá 60 ký tự")
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

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public LocalDate getDob() { return dob; }
    public void setDob(LocalDate dob) { this.dob = dob; }

    public Boolean getGender() { return gender; }
    public void setGender(Boolean gender) { this.gender = gender; }

    public Integer getHeight() { return height; }
    public void setHeight(Integer height) { this.height = height; }

    public BigDecimal getWeight() { return weight; }
    public void setWeight(BigDecimal weight) { this.weight = weight; }

    public String getBloodgroup() { return bloodgroup; }
    public void setBloodgroup(String bloodgroup) { this.bloodgroup = bloodgroup; }

    public String getPermanentMedicalHistory() { return permanentMedicalHistory; }
    public void setPermanentMedicalHistory(String permanentMedicalHistory) {
        this.permanentMedicalHistory = permanentMedicalHistory;
    }

    public String getAllergyNotes() { return allergyNotes; }
    public void setAllergyNotes(String allergyNotes) { this.allergyNotes = allergyNotes; }

    public String getSupervisorName() { return supervisorName; }
    public void setSupervisorName(String supervisorName) { this.supervisorName = supervisorName; }

    public String getSupervisorPhone() { return supervisorPhone; }
    public void setSupervisorPhone(String supervisorPhone) { this.supervisorPhone = supervisorPhone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

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
                ", email='" + email + '\'' +
                ", roomName='" + roomName + '\'' +
                ", specialty='" + specialty + '\'' +
                '}';
    }
}