package com.quan.diabetes.controller.patient;

import com.quan.diabetes.entity.*;
import com.quan.diabetes.dto.MedicationReminderView;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;

@Controller
public class PatientNotificationController extends BasePatientController {

    @GetMapping("/patient/notifications")
    public String notifications(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "5") int size,
            Model model, HttpSession session) {
        Patient patient = addCommonData(model, session, "notifications");

        List<AIReminder> allAiReminders = findRemindersByPatient(patient);
        List<MedicationReminderView> medicationReminders = buildTodayMedicationReminders(patient);

        int totalItems = allAiReminders.size();
        int totalPages = (int) Math.ceil((double) totalItems / size);

        int start = Math.min(page * size, totalItems);
        int end = Math.min((page + 1) * size, totalItems);
        List<AIReminder> pagedAiReminders = (start < end) ? allAiReminders.subList(start, end) : List.of();

        long dueMedicationReminderCount = medicationReminders.stream()
                .filter(MedicationReminderView::isDueNow)
                .count();

        long upcomingMedicationReminderCount = medicationReminders.stream()
                .filter(reminder -> !reminder.isPast())
                .count();

        model.addAttribute("aiReminders", pagedAiReminders);
        model.addAttribute("medicationReminders", medicationReminders);
        model.addAttribute("dueMedicationReminderCount", dueMedicationReminderCount);
        model.addAttribute("upcomingMedicationReminderCount", upcomingMedicationReminderCount);
        model.addAttribute("currentTime", LocalDateTime.now());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalItems", totalItems);
        model.addAttribute("pageSize", size);

        return "patient/notifications";
    }
}
