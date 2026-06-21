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

        content.append("Chào bệnh nhân ")
                .append(valueOrDefault(patientName, ""))
                .append(",\n\n");

        content.append("Đến giờ sử dụng thuốc");
        if (hasText(timeSlot)) {
            content.append(" ").append(timeSlot.trim());
        }
        content.append(":\n");

        if (medicines == null || medicines.isEmpty()) {
            content.append("- Hiện chưa có thông tin thuốc cần nhắc trong khung giờ này.\n");
        } else {
            for (PrescriptionReminderDto medicine : medicines) {
                content.append(formatMedicineLine(medicine)).append("\n");
            }

            String treatmentPlan = formatTreatmentPlan(medicines);
            if (hasText(treatmentPlan)) {
                content.append("\nKế hoạch phối hợp hôm nay:\n");
                content.append(treatmentPlan);
            }
        }

        content.append("\nChúc bạn nhiều sức khỏe.");
        return content.toString();
    }

    private String formatMedicineLine(PrescriptionReminderDto medicine) {
        if (medicine == null) {
            return "- Thuốc chưa rõ thông tin.";
        }

        StringBuilder line = new StringBuilder();
        line.append("- ");
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
            addPlanLine(planLines, "Chế độ dinh dưỡng", plan.getDietPlan());
            addPlanLine(planLines, "Chế độ tập luyện", plan.getExercisePlan());
            addPlanLine(planLines, "Theo dõi đường huyết", plan.getGlucoseMonitoringPlan());
        }

        StringBuilder content = new StringBuilder();
        for (String line : planLines) {
            content.append("- ").append(line).append("\n");
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
            line.append(" - ").append(value.trim());
        }
    }

    private String valueOrDefault(String value, String defaultValue) {
        return hasText(value) ? value.trim() : defaultValue;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
