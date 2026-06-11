package com.quan.diabetes.controller.patient;

import com.quan.diabetes.entity.AIConversation;
import com.quan.diabetes.entity.AIMessage;
import com.quan.diabetes.entity.AIReminder;
import com.quan.diabetes.entity.ClinicalExamination;
import com.quan.diabetes.entity.LabOrder;
import com.quan.diabetes.entity.LabResult;
import com.quan.diabetes.entity.Patient;
import com.quan.diabetes.entity.PatientRoutine;
import com.quan.diabetes.entity.Prescription;
import com.quan.diabetes.entity.PrescriptionDetail;
import com.quan.diabetes.entity.User;
import com.quan.diabetes.service.AIConversationService;
import com.quan.diabetes.service.AIMessageService;
import com.quan.diabetes.service.AIReminderService;
import com.quan.diabetes.service.ClinicalExaminationService;
import com.quan.diabetes.service.LabOrderService;
import com.quan.diabetes.service.LabResultService;
import com.quan.diabetes.service.PatientRoutineService;
import com.quan.diabetes.service.PatientService;
import com.quan.diabetes.service.PrescriptionDetailService;
import com.quan.diabetes.service.PrescriptionService;
import com.quan.diabetes.service.UserService;
import com.quan.diabetes.entity.PrescriptionTiming;
import com.quan.diabetes.service.PrescriptionTimingService;
import jakarta.servlet.http.HttpSession;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class PatientController {

    private final PatientService patientService;
    private final PatientRoutineService patientRoutineService;
    private final ClinicalExaminationService clinicalExaminationService;
    private final LabOrderService labOrderService;
    private final LabResultService labResultService;
    private final PrescriptionService prescriptionService;
    private final PrescriptionDetailService prescriptionDetailService;
    private final PrescriptionTimingService prescriptionTimingService;
    private final AIReminderService aiReminderService;
    private final AIConversationService aiConversationService;
    private final AIMessageService aiMessageService;
    private final UserService userService;

    public PatientController(PatientService patientService,
                             PatientRoutineService patientRoutineService,
                             ClinicalExaminationService clinicalExaminationService,
                             LabOrderService labOrderService,
                             LabResultService labResultService,
                             PrescriptionService prescriptionService,
                             PrescriptionDetailService prescriptionDetailService,
                             PrescriptionTimingService prescriptionTimingService,
                             AIReminderService aiReminderService,
                             AIConversationService aiConversationService,
                             AIMessageService aiMessageService,
                             UserService userService) {
        this.patientService = patientService;
        this.patientRoutineService = patientRoutineService;
        this.clinicalExaminationService = clinicalExaminationService;
        this.labOrderService = labOrderService;
        this.labResultService = labResultService;
        this.prescriptionService = prescriptionService;
        this.prescriptionDetailService = prescriptionDetailService;
        this.prescriptionTimingService = prescriptionTimingService;
        this.aiReminderService = aiReminderService;
        this.aiConversationService = aiConversationService;
        this.aiMessageService = aiMessageService;
        this.userService = userService;
    }

    @GetMapping({"/patient", "/patient/dashboard"})
    public String dashboard(Model model, HttpSession session) {
        Patient patient = addCommonData(model, session, "dashboard");

        List<ClinicalExamination> examinations = findExaminationsByPatient(patient);
        List<LabOrder> labOrders = findLabOrdersByPatient(patient);
        List<LabResult> labResults = findLabResultsByPatient(patient);
        List<AIReminder> aiReminders = findRemindersByPatient(patient);
        List<MedicationReminderView> medicationReminders = buildTodayMedicationReminders(patient);

        ClinicalExamination latestExam = examinations.isEmpty() ? null : examinations.get(0);

        model.addAttribute("latestExam", latestExam);
        model.addAttribute("examinationCount", examinations.size());
        model.addAttribute("labOrderCount", labOrders.size());
        model.addAttribute("completedOrderCount", countStatus(labOrders, "completed"));
        model.addAttribute("abnormalResultCount", countAbnormalResults(labResults));

        model.addAttribute("latestHbA1c", getLatestResultValue(labResults, "hba1c", "N/A"));
        model.addAttribute("latestGlucose", getLatestResultValue(labResults, "glucose", "N/A"));
        model.addAttribute("latestHbA1cStatus", evaluateHbA1cStatus(labResults));
        model.addAttribute("latestGlucoseStatus", evaluateGlucoseStatus(labResults));

        model.addAttribute("bmi", calculateBmi(patient));
        model.addAttribute("bmiStatus", evaluateBmiStatus(patient));

        int riskScore = calculateRiskScore(labResults, patient);
        model.addAttribute("riskScore", riskScore);
        model.addAttribute("riskLevel", getRiskLevel(riskScore));
        model.addAttribute("riskBadgeClass", getRiskBadgeClass(riskScore));

        model.addAttribute("recentMedicationReminders", medicationReminders.stream()
                .filter(reminder -> !reminder.isPast())
                .limit(4)
                .collect(Collectors.toList()));

        model.addAttribute("dueMedicationReminderCount", medicationReminders.stream()
                .filter(MedicationReminderView::isDueNow)
                .count());

        model.addAttribute("todayMedicationReminderCount", medicationReminders.size());

        model.addAttribute("recentAiReminders", aiReminders.stream()
                .limit(3)
                .collect(Collectors.toList()));

        model.addAttribute("recentLabResults", labResults.stream().limit(5).collect(Collectors.toList()));
        model.addAttribute("recentLabOrders", labOrders.stream().limit(4).collect(Collectors.toList()));

        return "patient/dashboard";
    }

    @GetMapping("/patient/profile")
    public String profile(Model model, HttpSession session) {
        User currentUser = getCurrentUser(session);
        Patient patient = getCurrentPatient(session);

        PatientRoutine routine = null;

        if (patient != null && patient.getUserId() != null) {
            routine = patientRoutineService.findById(patient.getUserId()).orElse(null);
        }

        model.addAttribute("activeMenu", "profile");
        model.addAttribute("patient", patient);
        model.addAttribute("routine", routine);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("patientCode", patient != null ? patient.getUserId() : "");
        model.addAttribute("pageRole", "Patient Portal");
        model.addAttribute("isCreateMode", patient == null);

        return "patient/profile";
    }

    @PostMapping("/patient/profile/save")
    public String saveProfile(@RequestParam("fullName") String fullName,
                              @RequestParam("phoneNumber") String phoneNumber,
                              @RequestParam(value = "address", required = false) String address,
                              @RequestParam(value = "dob", required = false)
                              @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dob,
                              @RequestParam(value = "gender", required = false) Boolean gender,
                              @RequestParam(value = "height", required = false) Integer height,
                              @RequestParam(value = "weight", required = false) BigDecimal weight,
                              @RequestParam(value = "bloodgroup", required = false) String bloodgroup,
                              @RequestParam(value = "permanentMedicalHistory", required = false) String permanentMedicalHistory,
                              @RequestParam(value = "allergyNotes", required = false) String allergyNotes,
                              @RequestParam(value = "supervisorName", required = false) String supervisorName,
                              @RequestParam(value = "supervisorPhone", required = false) String supervisorPhone,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {

        User currentUser = getCurrentUser(session);

        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "You must login first.");
            return "redirect:/login";
        }

        if (fullName == null || fullName.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Full name is required.");
            return "redirect:/patient/profile";
        }

        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Phone number is required.");
            return "redirect:/patient/profile";
        }

        String normalizedPhone = phoneNumber.trim();

        var existingUserByPhone = userService.findByPhoneNumber(normalizedPhone);

        if (existingUserByPhone.isPresent()
                && !existingUserByPhone.get().getUserId().equals(currentUser.getUserId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Phone number already exists.");
            return "redirect:/patient/profile";
        }

        String userId = currentUser.getUserId();

        Patient patient = patientService.findById(userId).orElse(new Patient());

        patient.setUserId(userId);
        patient.setUser(currentUser);
        patient.setFullName(fullName.trim());
        patient.setPhoneNumber(normalizedPhone);
        patient.setAddress(clean(address));
        patient.setDob(dob);
        patient.setGender(gender);
        patient.setHeight(height);
        patient.setWeight(weight);
        patient.setBloodgroup(clean(bloodgroup));
        patient.setPermanentMedicalHistory(clean(permanentMedicalHistory));
        patient.setAllergyNotes(clean(allergyNotes));
        patient.setSupervisorName(clean(supervisorName));
        patient.setSupervisorPhone(clean(supervisorPhone));

        if (patientService.existsById(userId)) {
            patientService.update(userId, patient);
            redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully.");
        } else {
            patientService.create(patient);
            redirectAttributes.addFlashAttribute("successMessage", "Profile created successfully.");
        }

        currentUser.setPhoneNumber(normalizedPhone);
        userService.update(currentUser.getUserId(), currentUser);

        session.setAttribute("loggedInUser", currentUser);
        session.setAttribute("userProfile", patient);

        return "redirect:/patient/profile";
    }

    @PostMapping("/patient/profile/routine/save")
    public String saveRoutine(@RequestParam(value = "breakfastTime", required = false)
                              @DateTimeFormat(pattern = "HH:mm") LocalTime breakfastTime,
                              @RequestParam(value = "lunchTime", required = false)
                              @DateTimeFormat(pattern = "HH:mm") LocalTime lunchTime,
                              @RequestParam(value = "dinnerTime", required = false)
                              @DateTimeFormat(pattern = "HH:mm") LocalTime dinnerTime,
                              @RequestParam(value = "wakeUpTime", required = false)
                              @DateTimeFormat(pattern = "HH:mm") LocalTime wakeUpTime,
                              @RequestParam(value = "sleepTime", required = false)
                              @DateTimeFormat(pattern = "HH:mm") LocalTime sleepTime,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {

        User currentUser = getCurrentUser(session);

        if (currentUser == null || currentUser.getUserId() == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "You must login first.");
            return "redirect:/login";
        }

        String userId = currentUser.getUserId();

        if (!patientService.existsById(userId)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please create your profile first.");
            return "redirect:/patient/profile";
        }

        PatientRoutine routine = patientRoutineService
                .findById(userId)
                .orElse(new PatientRoutine());

        routine.setUserId(userId);
        routine.setWakeUpTime(wakeUpTime != null ? wakeUpTime : LocalTime.of(6, 0));
        routine.setBreakfastTime(breakfastTime != null ? breakfastTime : LocalTime.of(7, 0));
        routine.setLunchTime(lunchTime != null ? lunchTime : LocalTime.of(12, 0));
        routine.setDinnerTime(dinnerTime != null ? dinnerTime : LocalTime.of(18, 0));
        routine.setSleepTime(sleepTime != null ? sleepTime : LocalTime.of(22, 0));

        if (patientRoutineService.existsById(userId)) {
            patientRoutineService.update(userId, routine);
            redirectAttributes.addFlashAttribute("successMessage", "Routine updated successfully.");
        } else {
            patientRoutineService.create(routine);
            redirectAttributes.addFlashAttribute("successMessage", "Routine created successfully.");
        }

        return "redirect:/patient/profile";
    }

    @PostMapping("/patient/profile/routine/delete")
    public String deleteRoutine(HttpSession session,
                                RedirectAttributes redirectAttributes) {
        Patient patient = getCurrentPatient(session);

        if (patient == null || patient.getUserId() == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Patient profile not found.");
            return "redirect:/patient/profile";
        }

        if (patientRoutineService.existsById(patient.getUserId())) {
            patientRoutineService.deleteById(patient.getUserId());
            redirectAttributes.addFlashAttribute("successMessage", "Routine deleted successfully.");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Routine does not exist.");
        }

        return "redirect:/patient/profile";
    }

    @GetMapping("/patient/notifications")
    public String notifications(Model model, HttpSession session) {
        Patient patient = addCommonData(model, session, "notifications");

        List<AIReminder> aiReminders = findRemindersByPatient(patient);
        List<MedicationReminderView> medicationReminders = buildTodayMedicationReminders(patient);

        long dueMedicationReminderCount = medicationReminders.stream()
                .filter(MedicationReminderView::isDueNow)
                .count();

        long upcomingMedicationReminderCount = medicationReminders.stream()
                .filter(reminder -> !reminder.isPast())
                .count();

        model.addAttribute("aiReminders", aiReminders);
        model.addAttribute("medicationReminders", medicationReminders);
        model.addAttribute("dueMedicationReminderCount", dueMedicationReminderCount);
        model.addAttribute("upcomingMedicationReminderCount", upcomingMedicationReminderCount);
        model.addAttribute("currentTime", LocalDateTime.now());

        return "patient/notifications";
    }

    @GetMapping("/patient/progress")
    public String progress(Model model, HttpSession session) {
        Patient patient = addCommonData(model, session, "progress");

        List<ClinicalExamination> examinations = findExaminationsByPatient(patient);
        List<LabOrder> labOrders = findLabOrdersByPatient(patient);
        List<LabResult> labResults = findLabResultsByPatient(patient);
        List<PrescriptionDetail> prescriptionDetails = findPrescriptionDetailsByPatient(patient);

        model.addAttribute("examinations", examinations);
        model.addAttribute("labOrders", labOrders);
        model.addAttribute("labResults", labResults);
        model.addAttribute("prescriptionDetails", prescriptionDetails);
        model.addAttribute("labOrdersByExam", groupLabOrdersByExam(examinations, labOrders));
        model.addAttribute("labResultsByOrder", groupLabResultsByOrder(labOrders, labResults));
        model.addAttribute("prescriptionDetailsByExam", groupPrescriptionDetailsByExam(examinations, prescriptionDetails));
        model.addAttribute("abnormalResultCount", countAbnormalResults(labResults));
        model.addAttribute("completedOrderCount", countStatus(labOrders, "completed"));

        return "patient/progress";
    }

    @GetMapping("/patient/tests")
    public String tests() {
        return "redirect:/patient/progress";
    }

    @GetMapping("/patient/results")
    public String results() {
        return "redirect:/patient/progress";
    }

    @GetMapping("/patient/risk")
    public String risk(Model model, HttpSession session) {
        Patient patient = addCommonData(model, session, "risk");

        List<ClinicalExamination> examinations = findExaminationsByPatient(patient);
        List<LabResult> labResults = findLabResultsByPatient(patient);
        List<LabResult> abnormalResults = findAbnormalResults(labResults);

        int riskScore = calculateRiskScore(labResults, patient);

        model.addAttribute("riskScore", riskScore);
        model.addAttribute("riskLevel", getRiskLevel(riskScore));
        model.addAttribute("riskBadgeClass", getRiskBadgeClass(riskScore));
        model.addAttribute("riskDescription", getRiskDescription(riskScore));
        model.addAttribute("riskAdvice", getRiskAdvice(riskScore));

        model.addAttribute("latestHbA1c", getLatestResultValue(labResults, "hba1c", "N/A"));
        model.addAttribute("latestGlucose", getLatestResultValue(labResults, "glucose", "N/A"));
        model.addAttribute("latestHbA1cStatus", evaluateHbA1cStatus(labResults));
        model.addAttribute("latestGlucoseStatus", evaluateGlucoseStatus(labResults));

        model.addAttribute("bmi", calculateBmi(patient));
        model.addAttribute("bmiStatus", evaluateBmiStatus(patient));

        model.addAttribute("examinationCount", examinations.size());
        model.addAttribute("abnormalResultCount", abnormalResults.size());
        model.addAttribute("abnormalResults", abnormalResults);
        model.addAttribute("recentLabResults", labResults.stream().limit(6).collect(Collectors.toList()));

        return "patient/risk";
    }

    @GetMapping("/patient/history")
    public String history(Model model, HttpSession session) {
        Patient patient = addCommonData(model, session, "history");

        model.addAttribute("examinations", findExaminationsByPatient(patient));

        return "patient/history";
    }

    @GetMapping("/patient/chat")
    public String chat(Model model, HttpSession session) {
        Patient patient = addCommonData(model, session, "chat");

        List<AIConversation> conversations = findConversationsByPatient(patient);
        List<AIMessage> messages = conversations.isEmpty()
                ? List.of()
                : findMessagesByConversation(conversations.get(0));

        model.addAttribute("conversations", conversations);
        model.addAttribute("messages", messages);

        return "patient/chat";
    }

    private Patient addCommonData(Model model, HttpSession session, String activeMenu) {
        Patient patient = getCurrentPatient(session);

        model.addAttribute("activeMenu", activeMenu);
        model.addAttribute("patient", patient);
        model.addAttribute("patientCode", patient != null ? patient.getUserId() : "");
        model.addAttribute("pageRole", "Patient Portal");

        return patient;
    }

    private User getCurrentUser(HttpSession session) {
        Object loggedInUser = session.getAttribute("loggedInUser");

        if (loggedInUser instanceof User user) {
            return user;
        }

        return null;
    }

    private Patient getCurrentPatient(HttpSession session) {
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

    private PatientRoutine findRoutineByPatient(Patient patient) {
        if (patient == null || patient.getUserId() == null) {
            return null;
        }

        return patientRoutineService.findById(patient.getUserId()).orElse(null);
    }

    private List<ClinicalExamination> findExaminationsByPatient(Patient patient) {
        if (patient == null || patient.getUserId() == null) {
            return List.of();
        }

        return clinicalExaminationService.findAll()
                .stream()
                .filter(exam -> exam.getPatient() != null)
                .filter(exam -> patient.getUserId().equals(exam.getPatient().getUserId()))
                .sorted(Comparator.comparing(
                        ClinicalExamination::getExamDate,
                        Comparator.nullsLast(Comparator.reverseOrder())
                ))
                .collect(Collectors.toList());
    }

    private List<LabOrder> findLabOrdersByPatient(Patient patient) {
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

    private List<LabResult> findLabResultsByPatient(Patient patient) {
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

    private List<PrescriptionDetail> findPrescriptionDetailsByPatient(Patient patient) {
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

    private Map<String, List<LabOrder>> groupLabOrdersByExam(List<ClinicalExamination> examinations,
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

    private Map<String, List<LabResult>> groupLabResultsByOrder(List<LabOrder> labOrders,
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

    private Map<String, List<PrescriptionDetail>> groupPrescriptionDetailsByExam(List<ClinicalExamination> examinations,
                                                                                 List<PrescriptionDetail> details) {
        Map<String, List<PrescriptionDetail>> result = new LinkedHashMap<>();

        for (ClinicalExamination examination : examinations) {
            String examId = examination.getClinicalExamId();

            List<PrescriptionDetail> examDetails = details.stream()
                    .filter(detail -> detail.getPrescription() != null)
                    .filter(detail -> detail.getPrescription().getClinicalExamination() != null)
                    .filter(detail -> examId.equals(detail.getPrescription().getClinicalExamination().getClinicalExamId()))
                    .collect(Collectors.toList());

            result.put(examId, examDetails);
        }

        return result;
    }

    private long countAbnormalResults(List<LabResult> labResults) {
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

    private long countStatus(List<LabOrder> labOrders, String keyword) {
        return labOrders.stream()
                .map(LabOrder::getStatus)
                .filter(Objects::nonNull)
                .map(String::toLowerCase)
                .filter(status -> status.contains(keyword.toLowerCase()))
                .count();
    }

    private List<AIReminder> findRemindersByPatient(Patient patient) {
        if (patient == null || patient.getUserId() == null) {
            return List.of();
        }

        return aiReminderService.findAll()
                .stream()
                .filter(reminder -> reminder.getPatient() != null)
                .filter(reminder -> patient.getUserId().equals(reminder.getPatient().getUserId()))
                .sorted(Comparator.comparing(
                        AIReminder::getScheduledTime,
                        Comparator.nullsLast(Comparator.reverseOrder())
                ))
                .collect(Collectors.toList());
    }

    private List<AIConversation> findConversationsByPatient(Patient patient) {
        if (patient == null || patient.getUserId() == null) {
            return List.of();
        }

        return aiConversationService.findAll()
                .stream()
                .filter(conversation -> conversation.getPatient() != null)
                .filter(conversation -> patient.getUserId().equals(conversation.getPatient().getUserId()))
                .sorted(Comparator.comparing(
                        AIConversation::getCreatedAt,
                        Comparator.nullsLast(Comparator.reverseOrder())
                ))
                .collect(Collectors.toList());
    }

    private List<AIMessage> findMessagesByConversation(AIConversation conversation) {
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
                        Comparator.nullsLast(Comparator.naturalOrder())
                ))
                .collect(Collectors.toList());
    }

    private String getLatestResultValue(List<LabResult> results, String keyword, String fallback) {
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

    private String calculateBmi(Patient patient) {
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

    private int calculateRiskScore(List<LabResult> results, Patient patient) {
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

    private String clean(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }

    public String formatDateTime(java.time.LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }

        return dateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    private String evaluateHbA1cStatus(List<LabResult> results) {
        BigDecimal value = findLatestResultNumber(results, "hba1c");

        if (value == null) {
            return "No data";
        }

        if (value.compareTo(new BigDecimal("6.5")) >= 0) {
            return "Diabetes range";
        }

        if (value.compareTo(new BigDecimal("5.7")) >= 0) {
            return "Prediabetes range";
        }

        return "Normal range";
    }

    private String evaluateGlucoseStatus(List<LabResult> results) {
        BigDecimal value = findLatestResultNumber(results, "glucose");

        if (value == null) {
            return "No data";
        }

        if (value.compareTo(new BigDecimal("126")) >= 0) {
            return "High";
        }

        if (value.compareTo(new BigDecimal("100")) >= 0) {
            return "Borderline";
        }

        return "Normal";
    }

    private String evaluateBmiStatus(Patient patient) {
        if (patient == null
                || patient.getHeight() == null
                || patient.getWeight() == null
                || patient.getHeight() == 0) {
            return "No data";
        }

        double heightMeter = patient.getHeight() / 100.0;
        double bmi = patient.getWeight().doubleValue() / (heightMeter * heightMeter);

        if (bmi >= 30) {
            return "Obese";
        }

        if (bmi >= 25) {
            return "Overweight";
        }

        if (bmi >= 18.5) {
            return "Normal";
        }

        return "Underweight";
    }

    private BigDecimal findLatestResultNumber(List<LabResult> results, String keyword) {
        return results.stream()
                .filter(result -> result.getLabTest() != null)
                .filter(result -> result.getLabTest().getTestName() != null)
                .filter(result -> result.getLabTest().getTestName().toLowerCase().contains(keyword.toLowerCase()))
                .map(LabResult::getResultValue)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    private String getRiskLevel(int riskScore) {
        if (riskScore >= 70) {
            return "High Risk";
        }

        if (riskScore >= 40) {
            return "Medium Risk";
        }

        return "Low Risk";
    }

    private String getRiskBadgeClass(int riskScore) {
        if (riskScore >= 70) {
            return "badge-danger";
        }

        if (riskScore >= 40) {
            return "badge-warning";
        }

        return "badge-success";
    }

    private List<LabResult> findAbnormalResults(List<LabResult> labResults) {
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

    private String getRiskDescription(int riskScore) {
        if (riskScore >= 70) {
            return "Your current indicators show a high diabetes-related health risk. Please follow your doctor's treatment plan and monitor your glucose closely.";
        }

        if (riskScore >= 40) {
            return "Your current indicators show a moderate risk. Continue monitoring HbA1c, fasting glucose, BMI and follow medical guidance.";
        }

        return "Your current indicators show a low risk based on available data. Keep maintaining a healthy routine and regular follow-up.";
    }

    private String getRiskAdvice(int riskScore) {
        if (riskScore >= 70) {
            return "Recommended actions: contact your doctor when symptoms worsen, follow prescribed medication, reduce high-sugar foods, and attend your next appointment on time.";
        }

        if (riskScore >= 40) {
            return "Recommended actions: maintain a balanced diet, exercise regularly, monitor blood glucose, and keep follow-up appointments.";
        }

        return "Recommended actions: continue healthy eating, regular activity, periodic testing and routine diabetes prevention habits.";
    }

    private List<MedicationReminderView> buildTodayMedicationReminders(Patient patient) {
        if (patient == null || patient.getUserId() == null) {
            return List.of();
        }

        PatientRoutine routine = findRoutineByPatient(patient);
        List<PrescriptionDetail> prescriptionDetails = findPrescriptionDetailsByPatient(patient);

        if (prescriptionDetails.isEmpty()) {
            return List.of();
        }

        Set<String> prescriptionDetailIds = prescriptionDetails.stream()
                .map(PrescriptionDetail::getPrescriptionDetailId)
                .collect(Collectors.toSet());

        Map<String, PrescriptionDetail> detailMap = prescriptionDetails.stream()
                .collect(Collectors.toMap(
                        PrescriptionDetail::getPrescriptionDetailId,
                        detail -> detail,
                        (oldValue, newValue) -> oldValue
                ));

        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        List<MedicationReminderView> reminders = new ArrayList<>();

        List<PrescriptionTiming> prescriptionTimings = prescriptionTimingService.findAll()
                .stream()
                .filter(timing -> timing.getPrescriptionDetail() != null)
                .filter(timing -> timing.getTiming() != null)
                .filter(timing -> prescriptionDetailIds.contains(
                        timing.getPrescriptionDetail().getPrescriptionDetailId()
                ))
                .collect(Collectors.toList());

        for (PrescriptionTiming prescriptionTiming : prescriptionTimings) {
            PrescriptionDetail detail = detailMap.get(
                    prescriptionTiming.getPrescriptionDetail().getPrescriptionDetailId()
            );

            if (detail == null) {
                continue;
            }

            String timingName = prescriptionTiming.getTiming().getTimingName();
            LocalTime medicationTime = resolveMedicationTime(timingName, routine);

            if (medicationTime == null) {
                continue;
            }

            LocalDateTime medicationDateTime = LocalDateTime.of(today, medicationTime);

            reminders.add(createMedicationReminderView(
                    detail,
                    timingName,
                    medicationDateTime,
                    medicationDateTime.minusMinutes(15),
                    15,
                    now
            ));

            reminders.add(createMedicationReminderView(
                    detail,
                    timingName,
                    medicationDateTime,
                    medicationDateTime.minusMinutes(10),
                    10,
                    now
            ));
        }

        return reminders.stream()
                .sorted(Comparator.comparing(MedicationReminderView::getReminderTime))
                .collect(Collectors.toList());
    }

    private MedicationReminderView createMedicationReminderView(PrescriptionDetail detail,
                                                                String timingName,
                                                                LocalDateTime medicationTime,
                                                                LocalDateTime reminderTime,
                                                                int minutesBefore,
                                                                LocalDateTime now) {
        String medicationName = detail.getMedication() != null
                ? detail.getMedication().getMedicationName()
                : "Medication";

        String dosage = detail.getDosage() != null
                ? detail.getDosage()
                : "";

        String instruction = detail.getMedication() != null
                ? detail.getMedication().getUsageInstruction()
                : "";

        boolean isDueNow = !now.isBefore(reminderTime) && now.isBefore(medicationTime);
        boolean isPast = now.isAfter(medicationTime);

        String title = "Medication reminder";
        String message = "Take " + medicationName + " - " + dosage + " at "
                + medicationTime.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"));

        return new MedicationReminderView(
                title,
                message,
                medicationName,
                dosage,
                timingName,
                instruction,
                medicationTime,
                reminderTime,
                minutesBefore,
                isDueNow,
                isPast
        );
    }

    private LocalTime resolveMedicationTime(String timingName, PatientRoutine routine) {
        if (timingName == null || timingName.trim().isEmpty()) {
            return null;
        }

        String normalizedTiming = timingName.toLowerCase();

        LocalTime breakfastTime = routine != null && routine.getBreakfastTime() != null
                ? routine.getBreakfastTime()
                : LocalTime.of(7, 0);

        LocalTime lunchTime = routine != null && routine.getLunchTime() != null
                ? routine.getLunchTime()
                : LocalTime.of(12, 0);

        LocalTime dinnerTime = routine != null && routine.getDinnerTime() != null
                ? routine.getDinnerTime()
                : LocalTime.of(18, 30);

        LocalTime sleepTime = routine != null && routine.getSleepTime() != null
                ? routine.getSleepTime()
                : LocalTime.of(22, 30);

        LocalTime wakeUpTime = routine != null && routine.getWakeUpTime() != null
                ? routine.getWakeUpTime()
                : LocalTime.of(6, 0);

        if (normalizedTiming.contains("breakfast") || normalizedTiming.contains("sáng")) {
            return adjustByMealTiming(normalizedTiming, breakfastTime);
        }

        if (normalizedTiming.contains("lunch") || normalizedTiming.contains("trưa")) {
            return adjustByMealTiming(normalizedTiming, lunchTime);
        }

        if (normalizedTiming.contains("dinner") || normalizedTiming.contains("tối")) {
            return adjustByMealTiming(normalizedTiming, dinnerTime);
        }

        if (normalizedTiming.contains("sleep")
                || normalizedTiming.contains("bed")
                || normalizedTiming.contains("ngủ")) {
            return sleepTime;
        }

        if (normalizedTiming.contains("wake")
                || normalizedTiming.contains("morning")
                || normalizedTiming.contains("thức")) {
            return wakeUpTime;
        }

        return null;
    }

    private LocalTime adjustByMealTiming(String timingName, LocalTime mealTime) {
        if (timingName.contains("before") || timingName.contains("trước")) {
            return mealTime.minusMinutes(15);
        }

        if (timingName.contains("after") || timingName.contains("sau")) {
            return mealTime.plusMinutes(15);
        }

        return mealTime;
    }

    public static class MedicationReminderView {
        private final String title;
        private final String message;
        private final String medicationName;
        private final String dosage;
        private final String timingName;
        private final String instruction;
        private final LocalDateTime medicationTime;
        private final LocalDateTime reminderTime;
        private final int minutesBefore;
        private final boolean dueNow;
        private final boolean past;

        public MedicationReminderView(String title,
                                      String message,
                                      String medicationName,
                                      String dosage,
                                      String timingName,
                                      String instruction,
                                      LocalDateTime medicationTime,
                                      LocalDateTime reminderTime,
                                      int minutesBefore,
                                      boolean dueNow,
                                      boolean past) {
            this.title = title;
            this.message = message;
            this.medicationName = medicationName;
            this.dosage = dosage;
            this.timingName = timingName;
            this.instruction = instruction;
            this.medicationTime = medicationTime;
            this.reminderTime = reminderTime;
            this.minutesBefore = minutesBefore;
            this.dueNow = dueNow;
            this.past = past;
        }

        public String getTitle() {
            return title;
        }

        public String getMessage() {
            return message;
        }

        public String getMedicationName() {
            return medicationName;
        }

        public String getDosage() {
            return dosage;
        }

        public String getTimingName() {
            return timingName;
        }

        public String getInstruction() {
            return instruction;
        }

        public LocalDateTime getMedicationTime() {
            return medicationTime;
        }

        public LocalDateTime getReminderTime() {
            return reminderTime;
        }

        public int getMinutesBefore() {
            return minutesBefore;
        }

        public boolean isDueNow() {
            return dueNow;
        }

        public boolean isPast() {
            return past;
        }
    }
}