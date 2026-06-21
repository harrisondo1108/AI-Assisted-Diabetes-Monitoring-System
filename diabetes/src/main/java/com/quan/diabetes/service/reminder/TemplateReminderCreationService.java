package com.quan.diabetes.service.reminder;

import com.quan.diabetes.dto.PrescriptionReminderDto;
import com.quan.diabetes.entity.TreatmentPlan;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class TemplateReminderCreationService {

    public String generateGroupReminder(
            String patientName,
            String timeSlot,
            List<PrescriptionReminderDto> medicines
    ) {
        StringBuilder content = new StringBuilder();

        // Thay đổi lời chào thân thiện hơn
        content.append("Thân chào bệnh nhân ")
                .append(valueOrDefault(patientName, "nhé"))
                .append(",\n\n");

        // Hành văn nhẹ nhàng hơn cho khung giờ
        if (hasText(timeSlot)) {
            content.append("Đã đến khung giờ dùng thuốc của bạn (")
                    .append(timeSlot.trim())
                    .append(") rồi ạ. Bạn lưu ý sử dụng các thuốc sau nhé:\n");
        } else {
            content.append("Đã đến giờ sử dụng thuốc của bạn rồi ạ. Bạn lưu ý dùng các thuốc sau nhé:\n");
        }

        if (medicines == null || medicines.isEmpty()) {
            content.append("- Hiện tại chưa có thông tin thuốc cần nhắc trong khung giờ này.\n");
        } else {
            for (PrescriptionReminderDto medicine : medicines) {
                content.append(formatMedicineLine(medicine)).append("\n");
            }

            String treatmentPlan = formatTreatmentPlan(medicines);
            if (hasText(treatmentPlan)) {
                // Thay đổi tiêu đề kế hoạch phối hợp nghe bớt khô khan
                content.append("\nMột vài lưu ý nhỏ cho ngày hôm nay để việc điều trị hiệu quả hơn:\n");
                content.append(treatmentPlan);
            }
        }

        // Lời chúc ấm áp hơn
        content.append("\nChúc bạn một ngày nhiều niềm vui và luôn khỏe mạnh!");
        return content.toString();
    }

    private String formatMedicineLine(PrescriptionReminderDto medicine) {
        if (medicine == null) {
            return "- (Thông tin thuốc chưa rõ)";
        }

        StringBuilder line = new StringBuilder();
        line.append("📍 "); // Thêm emoji để giao diện tin nhắn trực quan, bớt khô khan
        line.append(valueOrDefault(medicine.getMedicationName(), "Thuốc chưa rõ tên"));

        appendSegment(line, medicine.getDosage());
        appendSegment(line, combineFormAndRoute(medicine));
        appendSegment(line, medicine.getUsageInstruction());
        appendSegment(line, medicine.getMedicationPlan());
        return line.toString();
    }

    private String combineFormAndRoute(PrescriptionReminderDto medicine) {
        String form = medicine.getForm();
        String route = medicine.getAdministrationRoute();

        if (hasText(form) && hasText(route)) {
            return form.trim() + ", " + route.trim();
        }
        if (hasText(form)) {
            return form.trim();
        }
        if (hasText(route)) {
            return route.trim();
        }
        return "";
    }

    private String formatTreatmentPlan(List<PrescriptionReminderDto> medicines) {
        Set<String> planLines = new LinkedHashSet<>();

        for (PrescriptionReminderDto medicine : medicines) {
            if (medicine == null || medicine.getTreatmentPlan() == null) {
                continue;
            }

            TreatmentPlan plan = medicine.getTreatmentPlan();
            // Đổi nhãn (Label) cho gần gũi hơn với đời sống hàng ngày
            addPlanLine(planLines, "🥗 Chế độ dinh dưỡng", plan.getDietPlan());
            addPlanLine(planLines, "🏃 Chế độ tập luyện", plan.getExercisePlan());
            addPlanLine(planLines, "🩸 Theo dõi đường huyết", plan.getGlucoseMonitoringPlan());
        }

        StringBuilder content = new StringBuilder();
        for (String line : planLines) {
            content.append(line).append("\n");
        }

        return content.toString();
    }

    private void addPlanLine(Set<String> planLines, String label, String value) {
        if (hasText(value)) {
            planLines.add(label + ": " + value.trim());
        }
    }

    private void appendSegment(StringBuilder line, String value) {
        if (hasText(value)) {
            line.append(" | ").append(value.trim()); // Thay dấu "-" bằng "|" nhìn phân tách thông tin thuốc clear hơn
        }
    }

    private String valueOrDefault(String value, String defaultValue) {
        return hasText(value) ? value.trim() : defaultValue;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}