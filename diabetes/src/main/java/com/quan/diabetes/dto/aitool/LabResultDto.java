package com.quan.diabetes.dto.aitool;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record LabResultDto(
        @com.fasterxml.jackson.annotation.JsonProperty("Tên xét nghiệm") String testName,
        @com.fasterxml.jackson.annotation.JsonProperty("Kết quả") BigDecimal resultValue,
        @com.fasterxml.jackson.annotation.JsonProperty("Khoảng tham chiếu") String referenceRange,
        @com.fasterxml.jackson.annotation.JsonProperty("Đơn vị") String unit,
        @com.fasterxml.jackson.annotation.JsonProperty("Đánh giá") String flag,
        @com.fasterxml.jackson.annotation.JsonProperty("Ngày xét nghiệm") LocalDateTime examDate
) {}
