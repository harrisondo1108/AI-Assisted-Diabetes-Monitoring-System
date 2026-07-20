package com.quan.diabetes.controller.patient;

import com.quan.diabetes.entity.*;
import com.quan.diabetes.dto.patient.MedicationReminderView;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class PatientNotificationController extends BasePatientController {

    @GetMapping("/patient/notifications")
    public String notifications(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "5") int size,
            Model model, HttpSession session) {
        Patient patient = addCommonData(model, session, "notifications");

        List<Reminder> allAiReminders = findRemindersByPatient(patient);
        List<MedicationReminderView> medicationReminders = buildTodayMedicationReminders(patient);

        int totalItems = allAiReminders.size();
        int totalPages = (int) Math.ceil((double) totalItems / size);

        int start = Math.min(page * size, totalItems);
        int end = Math.min((page + 1) * size, totalItems);
        List<Reminder> pagedAiReminders = (start < end) ? allAiReminders.subList(start, end) : List.of();

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

    @GetMapping("/patient/notifications/history")
    public String notificationsHistory(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "50") int size,
            Model model, HttpSession session) {
        Patient patient = addCommonData(model, session, "notifications");

        List<Reminder> allAiReminders = findRemindersByPatient(patient);

        // Filter and Group
        List<Reminder> todayNotifications = new ArrayList<>();
        List<Reminder> yesterdayNotifications = new ArrayList<>();
        List<Reminder> olderNotifications = new ArrayList<>();

        LocalDate todayDate = LocalDate.now();
        LocalDate yesterdayDate = todayDate.minusDays(1);

        for (Reminder reminder : allAiReminders) {
            if (reminder.getScheduledTime() != null) {
                LocalDate scheduledDate = reminder.getScheduledTime().toLocalDate();
                if (scheduledDate.isEqual(todayDate)) {
                    todayNotifications.add(reminder);
                } else if (scheduledDate.isEqual(yesterdayDate)) {
                    yesterdayNotifications.add(reminder);
                } else {
                    olderNotifications.add(reminder);
                }
            } else {
                olderNotifications.add(reminder);
            }
        }

        model.addAttribute("todayNotifications", todayNotifications);
        model.addAttribute("yesterdayNotifications", yesterdayNotifications);
        model.addAttribute("olderNotifications", olderNotifications);

        return "patient/notifications-history";
    }

    @PostMapping("/patient/notifications/mark-all-read")
    @ResponseBody
    public Map<String, Object> markAllRead(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        Patient patient = getCurrentPatient(session);
        if (patient != null) {
            List<Reminder> allAiReminders = findRemindersByPatient(patient);
            for (Reminder reminder : allAiReminders) {
                if (reminder.getIsRead() == null || !reminder.getIsRead()) {
                    reminder.setIsRead(true);
                    reminderService.update(reminder.getReminderId(), reminder);
                }
            }
            response.put("success", true);
            response.put("message", "Đã đánh dấu tất cả là đã đọc");
        } else {
            response.put("success", false);
            response.put("message", "Không tìm thấy thông tin bệnh nhân");
        }
        return response;
    }

    @GetMapping("/patient/notifications/{id}")
    public String notificationDetail(
            @PathVariable("id") Long id,
            Model model, HttpSession session) {
        Patient patient = addCommonData(model, session, "notifications");
        if (patient == null) {
            return "redirect:/login";
        }

        Reminder reminder = reminderService.findById(id).orElse(null);
        if (reminder == null || reminder.getPatient() == null
                || !patient.getUserId().equals(reminder.getPatient().getUserId())) {
            return "redirect:/patient/notifications/history";
        }

        // Update isRead = true
        if (reminder.getIsRead() == null || !reminder.getIsRead()) {
            reminder.setIsRead(true);
            reminderService.update(reminder.getReminderId(), reminder);
        }

        // Subtitle formatted date/time
        String formattedDateTime = "";
        if (reminder.getScheduledTime() != null) {
            LocalDateTime scheduled = reminder.getScheduledTime();
            LocalDate date = scheduled.toLocalDate();
            LocalDate today = LocalDate.now();
            LocalDate yesterday = today.minusDays(1);

            String ampm = scheduled.getHour() >= 12 ? "PM" : "AM";
            int hour = scheduled.getHour() > 12 ? scheduled.getHour() - 12
                    : (scheduled.getHour() == 0 ? 12 : scheduled.getHour());
            String timeStr = String.format("%02d:%02d", hour, scheduled.getMinute());
            if (date.isEqual(today)) {
                formattedDateTime = "Hôm nay, " + timeStr + " " + ampm;
            } else if (date.isEqual(yesterday)) {
                formattedDateTime = "Hôm qua, " + timeStr + " " + ampm;
            } else {
                formattedDateTime = date.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")) + ", "
                        + timeStr + " " + ampm;
            }
        }

        // Default values
        String medicationName = "Chưa rõ";
        String dosage = "Theo chỉ định";
        String timingName = "Theo hướng dẫn";
        String instruction = "Uống theo hướng dẫn của bác sĩ.";
        String doctorNote = "Không có";
        String doctorName = "Bác sĩ điều trị";
        String doctorSpecialty = "Khoa Nội tiết";
        String examId = null;
        ClinicalExamination exam = null;

        // 1. Prefer the exam linked directly to the reminder
        if (reminder.getClinicalExamination() != null) {
            exam = reminder.getClinicalExamination();
        }
        // 2. Fallback to the latest exam for the patient
        else if (patient != null) {
            exam = clinicalExaminationService.findAll().stream()
                    .filter(e -> e.getPatient() != null && e.getPatient().getUserId().equals(patient.getUserId()))
                    .sorted(Comparator.comparing(
                            ClinicalExamination::getExamDate,
                            Comparator.nullsLast(Comparator.reverseOrder())))
                    .findFirst()
                    .orElse(null);
        }

        if (exam != null) {
            examId = exam.getClinicalExamId();
            if (exam.getDoctor() != null) {
                User doctorUser = exam.getDoctor();
                if (doctorUser.getProfile() != null) {
                    doctorName = "BS. " + doctorUser.getProfile().getFullName();
                    if (doctorUser.getProfile().getSpecialty() != null
                            && !doctorUser.getProfile().getSpecialty().trim().isEmpty()) {
                        doctorSpecialty = doctorUser.getProfile().getSpecialty();
                    }
                } else {
                    doctorName = "BS. " + doctorUser.getUserId();
                }
            }

            // Find prescription details for this clinical exam
            final ClinicalExamination finalExam = exam;
            List<PrescriptionDetail> details = prescriptionDetailService.findAll().stream()
                    .filter(d -> d.getPrescription() != null && d.getPrescription().getClinicalExamination() != null)
                    .filter(d -> d.getPrescription().getClinicalExamination().getClinicalExamId()
                            .equals(finalExam.getClinicalExamId()))
                    .collect(Collectors.toList());

            List<PrescriptionDetail> matchingDetails = new ArrayList<>();

            // Strategy A: Match by Timing
            if (reminder.getTiming() != null && !details.isEmpty()) {
                timingName = reminder.getTiming().getTimingName();
                for (PrescriptionDetail d : details) {
                    if (d.getPrescriptionTimings() != null) {
                        boolean matchesTiming = d.getPrescriptionTimings().stream()
                                .anyMatch(pt -> pt.getTiming() != null
                                        && pt.getTiming().getTimingID() == reminder.getTiming().getTimingID());
                        if (matchesTiming) {
                            matchingDetails.add(d);
                        }
                    }
                }
            }

            // Strategy B: Match by parsing Medication Name from the Message
            if (matchingDetails.isEmpty() && !details.isEmpty() && reminder.getMessage() != null) {
                String msgLower = reminder.getMessage().toLowerCase();
                for (PrescriptionDetail d : details) {
                    if (d.getMedication() != null && d.getMedication().getMedicationName() != null) {
                        String medName = d.getMedication().getMedicationName().toLowerCase();
                        if (msgLower.contains(medName)) {
                            matchingDetails.add(d);
                        }
                    }
                }
            }

            // Apply the matched details to a list
            List<Map<String, String>> medList = new ArrayList<>();
            if (!matchingDetails.isEmpty()) {
                for (PrescriptionDetail d : matchingDetails) {
                    Map<String, String> med = new HashMap<>();
                    med.put("medicationName", d.getMedication() != null && d.getMedication().getMedicationName() != null ? d.getMedication().getMedicationName() : "Chưa rõ");
                    med.put("dosage", d.getDosage() != null ? d.getDosage() : "Theo chỉ định");
                    
                    String dTiming = timingName;
                    if (d.getPrescriptionTimings() != null && !d.getPrescriptionTimings().isEmpty()) {
                         dTiming = d.getPrescriptionTimings().stream()
                                 .map(pt -> pt.getTiming() != null ? pt.getTiming().getTimingName() : "")
                                 .filter(t -> !t.isEmpty())
                                 .collect(Collectors.joining(" & "));
                    }
                    med.put("timingName", dTiming != null && !dTiming.isEmpty() ? dTiming : "Theo hướng dẫn");
                    
                    med.put("instruction", d.getMedication() != null && d.getMedication().getUsageInstruction() != null && !d.getMedication().getUsageInstruction().trim().isEmpty() ? d.getMedication().getUsageInstruction() : "Uống theo hướng dẫn của bác sĩ.");
                    med.put("doctorNote", d.getMedicationPlan() != null && !d.getMedicationPlan().trim().isEmpty() ? d.getMedicationPlan() : "Không có");
                    medList.add(med);
                }
            }

            // Fallback for mock notifications
            if (medList.isEmpty() && reminder.getTitle() != null) {
                String title = reminder.getTitle();
                String message = reminder.getMessage() != null ? reminder.getMessage() : "";

                if (title.contains("Metformin") || message.contains("Metformin")) {
                    Map<String, String> med = new HashMap<>();
                    med.put("medicationName", "Metformin 500mg");
                    med.put("dosage", "01 Viên");
                    med.put("timingName", "Mỗi buổi sáng");
                    med.put("instruction", "Uống 1 viên ngay sau bữa ăn sáng để duy trì mức đường huyết ổn định. Việc dùng thuốc sau khi ăn giúp giảm thiểu các tác dụng phụ lên đường tiêu hóa.");
                    med.put("doctorNote", "Vui lòng uống đúng giờ và không bỏ bữa. Nếu có dấu hiệu bất thường như chóng mặt, vã mồ hôi lạnh hoặc mệt mỏi cực độ, hãy liên hệ bác sĩ ngay lập tức. Đây là một phần quan trọng trong phác đồ điều trị tiểu đường Type 2 của bạn.");
                    medList.add(med);
                } else if (title.contains("Gliclazide") || message.contains("Gliclazide")) {
                    Map<String, String> med = new HashMap<>();
                    med.put("medicationName", "Gliclazide 30mg");
                    med.put("dosage", "01 Viên");
                    med.put("timingName", "Mỗi buổi sáng");
                    med.put("instruction", "Uống 1 viên trước bữa ăn sáng để ổn định đường huyết.");
                    med.put("doctorNote", "Duy trì uống đều đặn trước ăn sáng 30 phút. Theo dõi đường huyết mao mạch định kỳ và báo cáo lại trong lần tái khám tới.");
                    medList.add(med);
                } else if (title.contains("Nhắc nhở lịch sử dụng thuốc") || title.contains("Nhắc nhở") || message.contains("thuốc")) {
                    Map<String, String> med = new HashMap<>();
                    med.put("medicationName", "Metformin 500mg");
                    med.put("dosage", "01 Viên");
                    med.put("timingName", message.contains("sáng") ? "Mỗi buổi sáng" : (message.contains("tối") ? "Mỗi buổi tối" : "Sau bữa ăn"));
                    med.put("instruction", "Uống 1 viên ngay sau bữa ăn để duy trì mức đường huyết ổn định. Việc dùng thuốc sau khi ăn giúp giảm thiểu các tác dụng phụ lên đường tiêu hóa.");
                    med.put("doctorNote", "Vui lòng uống đúng giờ và không bỏ bữa. Nếu có dấu hiệu bất thường, hãy liên hệ bác sĩ ngay lập tức. Việc tuân thủ điều trị là rất quan trọng.");
                    medList.add(med);
                }
            }
            
            // Set first item as default for single variables to prevent thymeleaf errors
            if (!medList.isEmpty()) {
                Map<String, String> firstMed = medList.get(0);
                medicationName = firstMed.get("medicationName");
                dosage = firstMed.get("dosage");
                timingName = firstMed.get("timingName");
                instruction = firstMed.get("instruction");
                doctorNote = medList.stream().map(m -> m.get("doctorNote")).filter(n -> !n.equals("Không có")).collect(Collectors.joining(". "));
                if(doctorNote.isEmpty()) doctorNote = "Không có";
            }
            
            model.addAttribute("medicationList", medList);
        }

        boolean isAppointment = false;
        String apptDayOfWeek = "Thứ Tư";
        String apptDateStr = "25 tháng 10, 2023";
        String apptLocation = "Phòng khám 204, Tầng 2, Tòa nhà A - Bệnh viện Đa khoa Tâm Anh";

        if (reminder.getTitle() != null && (reminder.getTitle().contains("Lịch")
                || reminder.getTitle().contains("Tái khám")
                || reminder.getTitle().contains("Hẹn")
                || reminder.getTitle().contains("Xác nhận"))) {
            isAppointment = true;

            if (exam != null) {
                LocalDateTime appt = exam.getNextAppointment();
                if (appt != null) {
                    apptDayOfWeek = getVietnameseDayOfWeek(appt.getDayOfWeek());
                    apptDateStr = appt.getDayOfMonth() + " tháng " + appt.getMonthValue() + ", " + appt.getYear();
                } else {
                    LocalDateTime fallbackAppt = exam.getExamDate() != null ? exam.getExamDate().plusDays(7)
                            : LocalDateTime.now().plusDays(7);
                    apptDayOfWeek = getVietnameseDayOfWeek(fallbackAppt.getDayOfWeek());
                    apptDateStr = fallbackAppt.getDayOfMonth() + " tháng " + fallbackAppt.getMonthValue() + ", "
                            + fallbackAppt.getYear();
                }
            } else {
                if (reminder.getTitle().contains("Lê Văn Anh") || reminder.getMessage().contains("BS. Lê Văn Anh")) {
                    apptDayOfWeek = "Thứ Tư";
                    apptDateStr = "25 tháng 10, 2023";
                } else {
                    LocalDateTime nextWeek = LocalDateTime.now().plusDays(7);
                    apptDayOfWeek = getVietnameseDayOfWeek(nextWeek.getDayOfWeek());
                    apptDateStr = nextWeek.getDayOfMonth() + " tháng " + nextWeek.getMonthValue() + ", "
                            + nextWeek.getYear();
                }
            }
        }

        model.addAttribute("reminder", reminder);
        model.addAttribute("medicationName", medicationName);
        model.addAttribute("dosage", dosage);
        model.addAttribute("timingName", timingName);
        model.addAttribute("instruction", instruction);
        model.addAttribute("doctorNote", doctorNote);
        model.addAttribute("doctorName", doctorName);
        model.addAttribute("doctorSpecialty", doctorSpecialty);
        model.addAttribute("formattedDateTime", formattedDateTime);
        model.addAttribute("examId", examId);
        model.addAttribute("isAppointment", isAppointment);
        model.addAttribute("apptDayOfWeek", apptDayOfWeek);
        model.addAttribute("apptDateStr", apptDateStr);
        model.addAttribute("apptLocation", apptLocation);

        return "patient/notifications-detail";
    }

    @GetMapping("/patient/prescription/detail")
    public String prescriptionDetail(
            @RequestParam("examId") String examId,
            Model model, HttpSession session) {
        Patient patient = addCommonData(model, session, "notifications");
        if (patient == null) {
            return "redirect:/login";
        }

        List<Map<String, Object>> prescriptionList = new ArrayList<>();

        if ("mock-exam-1".equals(examId)) {
            prescriptionList.add(createMockPrescriptionItem(
                    "Metformin", "BIGUANIDE CLASS", "500mg", "2 lần/ngày", "Sau ăn sáng & tối",
                    "Uống với một ly nước đầy. Báo cáo tình trạng buồn nôn kéo dài cho BS. Hà."));
            prescriptionList.add(createMockPrescriptionItem(
                    "Gliclazide", "SULFONYLUREA", "30mg", "1 lần/ngày", "30 phút trước ăn sáng",
                    "Nuốt nguyên viên. Không bẻ hoặc nhai. Theo dõi các dấu hiệu hạ đường huyết."));
            prescriptionList.add(createMockPrescriptionItem(
                    "Ramipril", "ACE INHIBITOR", "5mg", "1 lần/ngày", "Trước khi đi ngủ",
                    "Để kiểm soát huyết áp. Duy trì thời gian uống đều đặn mỗi tối."));
            prescriptionList.add(createMockPrescriptionItem(
                    "Atorvastatin", "STATIN", "20mg", "1 lần/ngày", "Buổi tối",
                    "Khuyến nghị chế độ ăn ít chất béo. Tránh uống nước ép bưởi chùm trong thời gian điều trị."));
        } else {
            ClinicalExamination exam = clinicalExaminationService.findById(examId).orElse(null);
            if (exam == null || exam.getPatient() == null
                    || !patient.getUserId().equals(exam.getPatient().getUserId())) {
                return "redirect:/patient/notifications/history";
            }

            List<PrescriptionDetail> details = prescriptionDetailService.findAll().stream()
                    .filter(d -> d.getPrescription() != null && d.getPrescription().getClinicalExamination() != null)
                    .filter(d -> d.getPrescription().getClinicalExamination().getClinicalExamId().equals(examId))
                    .collect(Collectors.toList());

            if (details.isEmpty()) {
                prescriptionList.add(createMockPrescriptionItem(
                        "Metformin", "BIGUANIDE CLASS", "500mg", "2 lần/ngày", "Sau ăn sáng & tối",
                        "Uống với một ly nước đầy. Báo cáo tình trạng buồn nôn kéo dài cho BS. Hà."));
                prescriptionList.add(createMockPrescriptionItem(
                        "Gliclazide", "SULFONYLUREA", "30mg", "1 lần/ngày", "30 phút trước ăn sáng",
                        "Nuốt nguyên viên. Không bẻ hoặc nhai. Theo dõi các dấu hiệu hạ đường huyết."));
                prescriptionList.add(createMockPrescriptionItem(
                        "Ramipril", "ACE INHIBITOR", "5mg", "1 lần/ngày", "Trước khi đi ngủ",
                        "Để kiểm soát huyết áp. Duy trì thời gian uống đều đặn mỗi tối."));
                prescriptionList.add(createMockPrescriptionItem(
                        "Atorvastatin", "STATIN", "20mg", "1 lần/ngày", "Buổi tối",
                        "Khuyến nghị chế độ ăn ít chất béo. Tránh uống nước ép bưởi chùm trong thời gian điều trị."));
            } else {
                for (PrescriptionDetail d : details) {
                    String name = d.getMedication() != null ? d.getMedication().getMedicationName() : "Thuốc";
                    String subclass = determineSubclass(name);
                    String dosage = d.getDosage() != null ? d.getDosage() : "Theo chỉ định";
                    int freqVal = d.getPrescriptionTimings() != null ? d.getPrescriptionTimings().size() : 1;
                    String frequency = freqVal + " lần/ngày";

                    String timing = "Theo hướng dẫn";
                    if (d.getPrescriptionTimings() != null && !d.getPrescriptionTimings().isEmpty()) {
                        List<String> timingNames = d.getPrescriptionTimings().stream()
                                .map(pt -> pt.getTiming() != null ? pt.getTiming().getTimingName() : "")
                                .filter(t -> !t.isEmpty())
                                .collect(Collectors.toList());
                        if (!timingNames.isEmpty()) {
                            timing = String.join(" & ", timingNames);
                        }
                    }

                    String notes = d.getMedication() != null && d.getMedication().getUsageInstruction() != null
                            ? d.getMedication().getUsageInstruction()
                            : "Uống thuốc theo hướng dẫn.";

                    prescriptionList.add(createMockPrescriptionItem(name, subclass, dosage, frequency, timing, notes));
                }
            }
        }

        model.addAttribute("prescriptionDetails", prescriptionList);
        model.addAttribute("activeItemsCount", prescriptionList.size());
        model.addAttribute("examId", examId);

        return "patient/prescription-detail";
    }

    private Map<String, Object> createMockPrescriptionItem(
            String name, String subclass, String dosage, String frequency, String timing, String notes) {
        Map<String, Object> item = new HashMap<>();
        item.put("name", name);
        item.put("subclass", subclass);
        item.put("dosage", dosage);
        item.put("frequency", frequency);
        item.put("timing", timing);
        item.put("notes", notes);
        return item;
    }

    private String determineSubclass(String name) {
        if (name == null)
            return "MEDICATION CLASS";
        String lower = name.toLowerCase();
        if (lower.contains("metformin"))
            return "BIGUANIDE CLASS";
        if (lower.contains("gliclazide"))
            return "SULFONYLUREA";
        if (lower.contains("ramipril"))
            return "ACE INHIBITOR";
        if (lower.contains("atorvastatin"))
            return "STATIN";
        return "MEDICATION CLASS";
    }

    private String getVietnameseDayOfWeek(java.time.DayOfWeek dayOfWeek) {
        return switch (dayOfWeek) {
            case MONDAY -> "Thứ Hai";
            case TUESDAY -> "Thứ Ba";
            case WEDNESDAY -> "Thứ Tư";
            case THURSDAY -> "Thứ Năm";
            case FRIDAY -> "Thứ Sáu";
            case SATURDAY -> "Thứ Bảy";
            case SUNDAY -> "Chủ Nhật";
        };
    }
}
