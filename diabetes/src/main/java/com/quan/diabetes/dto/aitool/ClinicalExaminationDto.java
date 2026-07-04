package com.quan.diabetes.dto.aitool;

import com.quan.diabetes.dto.reminder.PrescriptionReminderDto;
import java.time.LocalDateTime;
import java.util.List;

public record ClinicalExaminationDto(
        @com.fasterxml.jackson.annotation.JsonProperty("Ngày khám") LocalDateTime examDate,
        @com.fasterxml.jackson.annotation.JsonProperty("Bác sĩ khám") String doctorName,
        @com.fasterxml.jackson.annotation.JsonProperty("Chẩn đoán") String diagnosisNote,
        @com.fasterxml.jackson.annotation.JsonProperty("Lịch hẹn tái khám") LocalDateTime nextAppointment,
        @com.fasterxml.jackson.annotation.JsonProperty("Các triệu chứng") List<String> symptoms,
        @com.fasterxml.jackson.annotation.JsonProperty("Danh sách kết quả xét nghiệm") List<LabResultDto> labResults,
        @com.fasterxml.jackson.annotation.JsonProperty("Danh sách đơn thuốc") List<PrescriptionReminderDto> prescriptions,
        @com.fasterxml.jackson.annotation.JsonProperty("Kế hoạch điều trị") TreatmentPlanDto treatmentPlan,
        @com.fasterxml.jackson.annotation.JsonProperty("Trạng thái") String status,
        @com.fasterxml.jackson.annotation.JsonProperty("Lý do hủy (nếu có)") String cancelReason
) {}
