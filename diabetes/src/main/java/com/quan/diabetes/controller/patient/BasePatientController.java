package com.quan.diabetes.controller.patient;

import com.quan.diabetes.entity.*;

import com.quan.diabetes.dto.patient.MedicationReminderView;
import com.quan.diabetes.service.ai.AIConversationService;
import com.quan.diabetes.service.ai.AIMessageService;
import com.quan.diabetes.service.ai.AIReminderService;
import com.quan.diabetes.service.exam.ClinicalExaminationService;
import com.quan.diabetes.service.exam.TreatmentPlanService;
import com.quan.diabetes.service.lab.LabOrderService;
import com.quan.diabetes.service.lab.LabResultService;
import com.quan.diabetes.service.medication.PrescriptionDetailService;
import com.quan.diabetes.service.medication.PrescriptionService;
import com.quan.diabetes.service.medication.PrescriptionTimingService;
import com.quan.diabetes.service.user.PatientRoutineService;
import com.quan.diabetes.service.user.PatientService;
import com.quan.diabetes.util.ReminderTimeCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import jakarta.servlet.http.HttpSession;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public abstract class BasePatientController {

    @Autowired
    protected PatientService patientService;
    @Autowired
    protected PatientRoutineService patientRoutineService;
    @Autowired
    protected ClinicalExaminationService clinicalExaminationService;
    @Autowired
    protected LabOrderService labOrderService;
    @Autowired
    protected LabResultService labResultService;
    @Autowired
    protected PrescriptionService prescriptionService;
    @Autowired
    protected PrescriptionDetailService prescriptionDetailService;
    @Autowired
    protected PrescriptionTimingService prescriptionTimingService;
    @Autowired
    protected AIReminderService aiReminderService;
    @Autowired
    protected AIConversationService aiConversationService;
    @Autowired
    protected AIMessageService aiMessageService;
    @Autowired
    protected TreatmentPlanService treatmentPlanService;

    protected Patient addCommonData(Model model, HttpSession session, String activeMenu) {
        Patient patient = getCurrentPatient(session);

        model.addAttribute("activeMenu", activeMenu);
        model.addAttribute("patient", patient);
        model.addAttribute("patientCode", patient != null ? patient.getUserId() : "");
        model.addAttribute("pageRole", "Cổng thông tin bệnh nhân");

        boolean hasUnreadNotifications = false;
        if (patient != null) {
            hasUnreadNotifications = findRemindersByPatient(patient).stream()
                    .anyMatch(r -> r.getIsRead() == null || !r.getIsRead());
        }
        model.addAttribute("hasUnreadNotifications", hasUnreadNotifications);

        return patient;
    }

    protected User getCurrentUser(HttpSession session) {
        Object loggedInUser = session.getAttribute("loggedInUser");

        if (loggedInUser instanceof User user) {
            return user;
        }

        return null;
    }

    protected Patient getCurrentPatient(HttpSession session) {
        Object userProfile = session.getAttribute("userProfile");

        if (userProfile instanceof Patient patient) {
            return patient;
        }

        User currentUser = getCurrentUser(session);

        if (currentUser != null) {
            return patientService.findById(currentUser.getUserId()).orElse(null);
        }

        return null;
    }

    protected PatientRoutine findRoutineByPatient(Patient patient) {
        if (patient == null || patient.getUserId() == null) {
            return null;
        }

        return patientRoutineService.findById(patient.getUserId()).orElse(null);
    }

    protected List<ClinicalExamination> findExaminationsByPatient(Patient patient) {
        if (patient == null || patient.getUserId() == null) {
            return List.of();
        }

        return clinicalExaminationService.findAll()
                .stream()
                .filter(exam -> exam.getPatient() != null)
                .filter(exam -> patient.getUserId().equals(exam.getPatient().getUserId()))
                .sorted(Comparator.comparing(
                        ClinicalExamination::getExamDate,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());
    }

    protected Map<String, TreatmentPlan> groupTreatmentPlansByExam(List<ClinicalExamination> examinations) {
        Map<String, TreatmentPlan> result = new LinkedHashMap<>();

        for (ClinicalExamination examination : examinations) {
            String examId = examination.getClinicalExamId();
            TreatmentPlan plan = treatmentPlanService.findByClinicalExamId(examId).orElse(null);
            result.put(examId, plan);
        }

        return result;
    }

    protected List<LabOrder> findLabOrdersByPatient(Patient patient) {
        Set<String> examIds = findExaminationsByPatient(patient)
                .stream()
                .map(ClinicalExamination::getClinicalExamId)
                .collect(Collectors.toSet());

        if (examIds.isEmpty()) {
            return List.of();
        }

        return labOrderService.findAll()
                .stream()
                .filter(order -> order.getClinicalExamination() != null)
                .filter(order -> examIds.contains(order.getClinicalExamination().getClinicalExamId()))
                .collect(Collectors.toList());
    }

    protected List<LabResult> findLabResultsByPatient(Patient patient) {
        Set<String> orderIds = findLabOrdersByPatient(patient)
                .stream()
                .map(LabOrder::getLabOrderId)
                .collect(Collectors.toSet());

        if (orderIds.isEmpty()) {
            return List.of();
        }

        return labResultService.findAll()
                .stream()
                .filter(result -> result.getLabOrder() != null)
                .filter(result -> orderIds.contains(result.getLabOrder().getLabOrderId()))
                .collect(Collectors.toList());
    }

    protected List<PrescriptionDetail> findPrescriptionDetailsByPatient(Patient patient) {
        Set<String> examIds = findExaminationsByPatient(patient)
                .stream()
                .map(ClinicalExamination::getClinicalExamId)
                .collect(Collectors.toSet());

        if (examIds.isEmpty()) {
            return List.of();
        }

        Set<String> prescriptionIds = prescriptionService.findAll()
                .stream()
                .filter(prescription -> prescription.getClinicalExamination() != null)
                .filter(prescription -> examIds.contains(prescription.getClinicalExamination().getClinicalExamId()))
                .map(Prescription::getPrescriptionId)
                .collect(Collectors.toSet());

        if (prescriptionIds.isEmpty()) {
            return List.of();
        }

        return prescriptionDetailService.findAll()
                .stream()
                .filter(detail -> detail.getPrescription() != null)
                .filter(detail -> prescriptionIds.contains(detail.getPrescription().getPrescriptionId()))
                .collect(Collectors.toList());
    }

    protected Map<String, List<LabOrder>> groupLabOrdersByExam(List<ClinicalExamination> examinations,
            List<LabOrder> labOrders) {
        Map<String, List<LabOrder>> result = new LinkedHashMap<>();

        for (ClinicalExamination examination : examinations) {
            String examId = examination.getClinicalExamId();

            List<LabOrder> orders = labOrders.stream()
                    .filter(order -> order.getClinicalExamination() != null)
                    .filter(order -> examId.equals(order.getClinicalExamination().getClinicalExamId()))
                    .collect(Collectors.toList());

            result.put(examId, orders);
        }

        return result;
    }

    protected Map<String, List<LabResult>> groupLabResultsByOrder(List<LabOrder> labOrders,
            List<LabResult> labResults) {
        Map<String, List<LabResult>> result = new LinkedHashMap<>();

        for (LabOrder labOrder : labOrders) {
            String orderId = labOrder.getLabOrderId();

            List<LabResult> results = labResults.stream()
                    .filter(labResult -> labResult.getLabOrder() != null)
                    .filter(labResult -> orderId.equals(labResult.getLabOrder().getLabOrderId()))
                    .collect(Collectors.toList());

            result.put(orderId, results);
        }

        return result;
    }

    protected Map<String, List<PrescriptionDetail>> groupPrescriptionDetailsByExam(
            List<ClinicalExamination> examinations,
            List<PrescriptionDetail> details) {
        Map<String, List<PrescriptionDetail>> result = new LinkedHashMap<>();

        for (ClinicalExamination examination : examinations) {
            String examId = examination.getClinicalExamId();

            List<PrescriptionDetail> examDetails = details.stream()
                    .filter(detail -> detail.getPrescription() != null)
                    .filter(detail -> detail.getPrescription().getClinicalExamination() != null)
                    .filter(detail -> examId
                            .equals(detail.getPrescription().getClinicalExamination().getClinicalExamId()))
                    .collect(Collectors.toList());

            result.put(examId, examDetails);
        }

        return result;
    }

    protected long countAbnormalResults(List<LabResult> labResults) {
        return labResults.stream()
                .map(LabResult::getFlag)
                .filter(Objects::nonNull)
                .map(String::toLowerCase)
                .filter(flag -> flag.contains("high")
                        || flag.contains("low")
                        || flag.contains("abnormal")
                        || flag.contains("cao")
                        || flag.contains("thấp"))
                .count();
    }

    protected long countStatus(List<LabOrder> labOrders, String keyword) {
        return labOrders.stream()
                .map(LabOrder::getStatus)
                .filter(Objects::nonNull)
                .map(String::toLowerCase)
                .filter(status -> status.contains(keyword.toLowerCase()))
                .count();
    }

    protected List<AIReminder> findRemindersByPatient(Patient patient) {
        if (patient == null || patient.getUserId() == null) {
            return List.of();
        }

        return aiReminderService.getListByIdAndScheduledTimeLessThanEqual(patient.getUserId(), LocalDateTime.now());
    }

    protected List<AIConversation> findConversationsByPatient(Patient patient) {
        if (patient == null || patient.getUserId() == null) {
            return List.of();
        }

        return aiConversationService.findAll()
                .stream()
                .filter(conversation -> conversation.getPatient() != null)
                .filter(conversation -> patient.getUserId().equals(conversation.getPatient().getUserId()))
                .sorted(Comparator.comparing(
                        AIConversation::getCreatedAt,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());
    }

    protected List<AIMessage> findMessagesByConversation(AIConversation conversation) {
        if (conversation == null || conversation.getAiConversationId() == null) {
            return List.of();
        }

        return aiMessageService.findAll()
                .stream()
                .filter(message -> message.getAiConversation() != null)
                .filter(message -> conversation.getAiConversationId()
                        .equals(message.getAiConversation().getAiConversationId()))
                .sorted(Comparator.comparing(
                        AIMessage::getTime,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());
    }

    protected String getLatestResultValue(List<LabResult> results, String keyword, String fallback) {
        return results.stream()
                .filter(result -> result.getLabTest() != null)
                .filter(result -> result.getLabTest().getTestName() != null)
                .filter(result -> result.getLabTest().getTestName().toLowerCase().contains(keyword.toLowerCase()))
                .map(LabResult::getResultValue)
                .filter(Objects::nonNull)
                .map(value -> value.stripTrailingZeros().toPlainString())
                .findFirst()
                .orElse(fallback);
    }

    protected String calculateBmi(Patient patient) {
        if (patient == null
                || patient.getHeight() == null
                || patient.getWeight() == null
                || patient.getHeight() == 0) {
            return "N/A";
        }

        double heightMeter = patient.getHeight() / 100.0;
        double bmi = patient.getWeight().doubleValue() / (heightMeter * heightMeter);

        return String.format(Locale.US, "%.1f", bmi);
    }

    protected int calculateRiskScore(List<LabResult> results, Patient patient) {
        int score = 30;

        for (LabResult result : results) {
            String testName = result.getLabTest() != null && result.getLabTest().getTestName() != null
                    ? result.getLabTest().getTestName().toLowerCase()
                    : "";

            BigDecimal value = result.getResultValue();

            if (value == null) {
                continue;
            }

            if (testName.contains("hba1c") && value.compareTo(new BigDecimal("7.0")) >= 0) {
                score += 25;
            }

            if (testName.contains("glucose") && value.compareTo(new BigDecimal("126")) >= 0) {
                score += 20;
            }

            String flag = result.getFlag() == null ? "" : result.getFlag().toLowerCase();

            if (flag.contains("high") || flag.contains("cao") || flag.contains("abnormal")) {
                score += 10;
            }
        }

        if (patient != null && patient.getPermanentMedicalHistory() != null
                && patient.getPermanentMedicalHistory().toLowerCase().contains("diabetes")) {
            score += 10;
        }

        return Math.min(score, 100);
    }

    protected String clean(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }

    public String formatDateTime(java.time.LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }

        return dateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    protected String evaluateHbA1cStatus(List<LabResult> results) {
        BigDecimal value = findLatestResultNumber(results, "hba1c");

        if (value == null) {
            return "Không có dữ liệu";
        }

        if (value.compareTo(new BigDecimal("6.5")) >= 0) {
            return "Ngưỡng tiểu đường";
        }

        if (value.compareTo(new BigDecimal("5.7")) >= 0) {
            return "Ngưỡng tiền tiểu đường";
        }

        return "Ngưỡng bình thường";
    }

    protected String evaluateGlucoseStatus(List<LabResult> results) {
        BigDecimal value = findLatestResultNumber(results, "glucose");

        if (value == null) {
            return "Không có dữ liệu";
        }

        if (value.compareTo(new BigDecimal("126")) >= 0) {
            return "Cao";
        }

        if (value.compareTo(new BigDecimal("100")) >= 0) {
            return "Cận ranh giới";
        }

        return "Bình thường";
    }

    protected String evaluateBmiStatus(Patient patient) {
        if (patient == null
                || patient.getHeight() == null
                || patient.getWeight() == null
                || patient.getHeight() == 0) {
            return "Không có dữ liệu";
        }

        double heightMeter = patient.getHeight() / 100.0;
        double bmi = patient.getWeight().doubleValue() / (heightMeter * heightMeter);

        if (bmi >= 30) {
            return "Béo phì";
        }

        if (bmi >= 25) {
            return "Thừa cân";
        }

        if (bmi >= 18.5) {
            return "Bình thường";
        }

        return "Thiếu cân";
    }

    protected BigDecimal findLatestResultNumber(List<LabResult> results, String keyword) {
        return results.stream()
                .filter(result -> result.getLabTest() != null)
                .filter(result -> result.getLabTest().getTestName() != null)
                .filter(result -> result.getLabTest().getTestName().toLowerCase().contains(keyword.toLowerCase()))
                .map(LabResult::getResultValue)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    protected String getRiskLevel(int riskScore) {
        if (riskScore >= 70) {
            return "Nguy cơ cao";
        }

        if (riskScore >= 40) {
            return "Nguy cơ trung bình";
        }

        return "Nguy cơ thấp";
    }

    protected String getRiskBadgeClass(int riskScore) {
        if (riskScore >= 70) {
            return "badge-danger";
        }

        if (riskScore >= 40) {
            return "badge-warning";
        }

        return "badge-success";
    }

    protected List<LabResult> findAbnormalResults(List<LabResult> labResults) {
        return labResults.stream()
                .filter(result -> result.getFlag() != null)
                .filter(result -> {
                    String flag = result.getFlag().toLowerCase();

                    return flag.contains("high")
                            || flag.contains("low")
                            || flag.contains("abnormal")
                            || flag.contains("cao")
                            || flag.contains("thấp");
                })
                .collect(Collectors.toList());
    }

    protected String getRiskDescription(int riskScore) {
        if (riskScore >= 70) {
            return "Các chỉ số hiện tại của bạn cho thấy nguy cơ sức khỏe liên quan đến tiểu đường cao. Vui lòng tuân thủ phác đồ điều trị của bác sĩ và theo dõi sát sao lượng đường huyết.";
        }

        if (riskScore >= 40) {
            return "Các chỉ số hiện tại của bạn cho thấy nguy cơ ở mức trung bình. Hãy tiếp tục theo dõi HbA1c, đường huyết đói, BMI và tuân thủ hướng dẫn y tế.";
        }

        return "Các chỉ số hiện tại của bạn cho thấy nguy cơ thấp dựa trên dữ liệu hiện có. Tiếp tục duy trì thói quen lành mạnh và tái khám định kỳ.";
    }

    protected String getRiskAdvice(int riskScore) {
        if (riskScore >= 70) {
            return "Hành động khuyến nghị: liên hệ với bác sĩ khi các triệu chứng xấu đi, tuân thủ thuốc được kê đơn, giảm thực phẩm chứa nhiều đường, và đi khám đúng hẹn.";
        }

        if (riskScore >= 40) {
            return "Hành động khuyến nghị: duy trì chế độ ăn cân bằng, tập thể dục đều đặn, theo dõi lượng đường huyết, và đi tái khám định kỳ.";
        }

        return "Hành động khuyến nghị: tiếp tục ăn uống lành mạnh, hoạt động thể chất đều đặn, xét nghiệm định kỳ và duy trì thói quen phòng ngừa tiểu đường.";
    }

    protected List<MedicationReminderView> buildTodayMedicationReminders(Patient patient) {
        if (patient == null || patient.getUserId() == null) {
            return List.of();
        }

        List<PrescriptionDetail> prescriptionDetails = findPrescriptionDetailsByPatient(patient);
        if (prescriptionDetails.isEmpty()) {
            return List.of();
        }

        LocalDateTime now = LocalDateTime.now();

        // Lấy tất cả nhắc nhở hoạt động của bệnh nhân trong ngày hôm nay (lockStatus =
        // false)
        List<AIReminder> todayReminders = aiReminderService.getPatientRemindersToday(patient.getUserId());
        List<MedicationReminderView> reminders = new ArrayList<>();

        for (AIReminder reminder : todayReminders) {
            if (reminder.getTiming() == null) {
                continue;
            }
            int timingId = reminder.getTiming().getTimingID();

            // Tìm chi tiết đơn thuốc khớp với khung giờ (timingId) của nhắc nhở
            PrescriptionDetail matchingDetail = null;
            for (PrescriptionDetail detail : prescriptionDetails) {
                if (detail.getPrescriptionTimings() != null) {
                    boolean matches = detail.getPrescriptionTimings().stream()
                            .anyMatch(pt -> pt.getTiming() != null && pt.getTiming().getTimingID() == timingId);
                    if (matches) {
                        matchingDetail = detail;
                        break;
                    }
                }
            }

            if (matchingDetail == null) {
                continue;
            }

            boolean isSent = reminder.getIsSent() != null && reminder.getIsSent();
            boolean dueNow = false;
            boolean past = false;

            if (isSent) {
                LocalDateTime sentTime = reminder.getScheduledTime();
                LocalDateTime tenMinsAfter = sentTime.plusMinutes(10);
                if (now.isAfter(tenMinsAfter)) {
                    past = true;
                } else {
                    dueNow = true;
                }
            } else {
                // Chưa gửi thì trạng thái là sắp diễn ra (dueNow = false, past = false)
                dueNow = false;
                past = false;
            }

            reminders.add(createMedicationReminderView(
                    matchingDetail,
                    reminder.getTiming().getTimingName(),
                    reminder.getScheduledTime().plusMinutes(10),
                    reminder.getScheduledTime(),
                    dueNow,
                    past));
        }

        return reminders.stream()
                .sorted(Comparator.comparing(MedicationReminderView::getReminderTime))
                .collect(Collectors.toList());
    }

    protected MedicationReminderView createMedicationReminderView(PrescriptionDetail detail,
            String timingName,
            LocalDateTime endTime,
            LocalDateTime reminderTime,
            boolean isDueNow,
            boolean isPast) {
        String medicationName = detail.getMedication() != null
                ? detail.getMedication().getMedicationName()
                : "Thuốc";

        String dosage = detail.getDosage() != null
                ? detail.getDosage()
                : "";

        String instruction = detail.getMedication() != null
                ? detail.getMedication().getUsageInstruction()
                : "";

        String medicationPlan = detail.getMedicationPlan();

        String title = "Nhắc nhở dùng thuốc";
        String message = "Uống " + medicationName + " - " + dosage + " lúc "
                + endTime.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"));

        return new MedicationReminderView(
                title,
                message,
                medicationName,
                dosage,
                timingName,
                instruction,
                medicationPlan,
                endTime,
                reminderTime,
                isDueNow,
                isPast);
    }
}
