package com.quan.diabetes.dto.aitool;

import java.time.LocalDateTime;

public record TreatmentPlanDto(
        @com.fasterxml.jackson.annotation.JsonProperty("Mục tiêu điều trị") String treatmentGoal,
        @com.fasterxml.jackson.annotation.JsonProperty("Chế độ dinh dưỡng") String dietPlan,
        @com.fasterxml.jackson.annotation.JsonProperty("Chế độ tập luyện") String exercisePlan,
        @com.fasterxml.jackson.annotation.JsonProperty("Kế hoạch theo dõi đường huyết") String glucoseMonitoringPlan,
        @com.fasterxml.jackson.annotation.JsonProperty("Ngày lập phác đồ") LocalDateTime createdAt
) {}
