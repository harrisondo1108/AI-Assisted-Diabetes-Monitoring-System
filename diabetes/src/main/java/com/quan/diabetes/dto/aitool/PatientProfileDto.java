package com.quan.diabetes.dto.aitool;

import java.math.BigDecimal;

public record PatientProfileDto(
        @com.fasterxml.jackson.annotation.JsonProperty("Họ và tên") String fullName,
        @com.fasterxml.jackson.annotation.JsonProperty("Giới tính") String gender,
        @com.fasterxml.jackson.annotation.JsonProperty("Chiều cao (cm)") Integer height,
        @com.fasterxml.jackson.annotation.JsonProperty("Cân nặng (kg)") BigDecimal weight,
        @com.fasterxml.jackson.annotation.JsonProperty("Nhóm máu") String bloodgroup,
        @com.fasterxml.jackson.annotation.JsonProperty("Tiền sử bệnh lý") String permanentMedicalHistory,
        @com.fasterxml.jackson.annotation.JsonProperty("Ghi chú dị ứng") String allergyNotes
) {}
