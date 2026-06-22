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
import com.quan.diabetes.entity.TreatmentPlan;
import com.quan.diabetes.service.TreatmentPlanService;
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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.http.ResponseEntity;
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
    private final TreatmentPlanService treatmentPlanService;

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
                             UserService userService,
                             TreatmentPlanService treatmentPlanService) {
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
        this.treatmentPlanService = treatmentPlanService;
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

        TreatmentPlan latestTreatmentPlan = null;
        if (latestExam != null) {
            latestTreatmentPlan = treatmentPlanService.findByClinicalExamId(latestExam.getClinicalExamId()).orElse(null);
        }

        model.addAttribute("latestExam", latestExam);
        model.addAttribute("latestTreatmentPlan", latestTreatmentPlan);
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
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn phải đăng nhập trước.");
            return "redirect:/login";
        }

        if (fullName == null || fullName.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Yêu cầu nhập họ và tên.");
            return "redirect:/patient/profile";
        }

        // Force the patient's phone number to remain their account phone number
        String normalizedPhone = currentUser.getPhoneNumber();
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
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật hồ sơ thành công.");
        } else {
            patientService.create(patient);
            redirectAttributes.addFlashAttribute("successMessage", "Tạo hồ sơ thành công.");
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
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn phải đăng nhập trước.");
            return "redirect:/login";
        }

        String userId = currentUser.getUserId();

        if (!patientService.existsById(userId)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng tạo hồ sơ của bạn trước.");
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
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật lịch sinh hoạt thành công.");
        } else {
            patientRoutineService.create(routine);
            redirectAttributes.addFlashAttribute("successMessage", "Tạo lịch sinh hoạt thành công.");
        }

        return "redirect:/patient/profile";
    }

    @PostMapping("/patient/profile/routine/delete")
    public String deleteRoutine(HttpSession session,
                                RedirectAttributes redirectAttributes) {
        Patient patient = getCurrentPatient(session);

        if (patient == null || patient.getUserId() == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy hồ sơ bệnh nhân.");
            return "redirect:/patient/profile";
        }

        if (patientRoutineService.existsById(patient.getUserId())) {
            patientRoutineService.deleteById(patient.getUserId());
            redirectAttributes.addFlashAttribute("successMessage", "Xóa lịch sinh hoạt thành công.");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Lịch sinh hoạt không tồn tại.");
        }

        return "redirect:/patient/profile";
    }

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

    @GetMapping("/patient/progress")
    public String progress(
            @RequestParam(name = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(name = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "2") int size,
            Model model, HttpSession session) {
        Patient patient = addCommonData(model, session, "progress");

        List<ClinicalExamination> allExams = findExaminationsByPatient(patient);
        if (startDate != null) {
            allExams = allExams.stream()
                    .filter(exam -> exam.getExamDate() != null && !exam.getExamDate().isBefore(startDate.atStartOfDay()))
                    .collect(Collectors.toList());
        }
        if (endDate != null) {
            allExams = allExams.stream()
                    .filter(exam -> exam.getExamDate() != null && !exam.getExamDate().isAfter(endDate.atTime(23, 59, 59)))
                    .collect(Collectors.toList());
        }

        List<LabOrder> allLabOrders = findLabOrdersByPatient(patient);
        List<LabResult> allLabResults = findLabResultsByPatient(patient);
        List<PrescriptionDetail> allPrescriptionDetails = findPrescriptionDetailsByPatient(patient);

        Set<String> filteredExamIds = allExams.stream()
                .map(ClinicalExamination::getClinicalExamId)
                .collect(Collectors.toSet());

        List<LabOrder> filteredLabOrders = allLabOrders.stream()
                .filter(order -> order.getClinicalExamination() != null && filteredExamIds.contains(order.getClinicalExamination().getClinicalExamId()))
                .collect(Collectors.toList());

        Set<String> filteredOrderIds = filteredLabOrders.stream()
                .map(LabOrder::getLabOrderId)
                .collect(Collectors.toSet());
        List<LabResult> filteredLabResults = allLabResults.stream()
                .filter(result -> result.getLabOrder() != null && filteredOrderIds.contains(result.getLabOrder().getLabOrderId()))
                .collect(Collectors.toList());

        Set<String> prescriptionIds = prescriptionService.findAll()
                .stream()
                .filter(prescription -> prescription.getClinicalExamination() != null)
                .filter(prescription -> filteredExamIds.contains(prescription.getClinicalExamination().getClinicalExamId()))
                .map(Prescription::getPrescriptionId)
                .collect(Collectors.toSet());

        List<PrescriptionDetail> filteredPrescriptionDetails = allPrescriptionDetails.stream()
                .filter(detail -> detail.getPrescription() != null && prescriptionIds.contains(detail.getPrescription().getPrescriptionId()))
                .collect(Collectors.toList());

        int totalItems = allExams.size();
        int totalPages = (int) Math.ceil((double) totalItems / size);

        int start = Math.min(page * size, totalItems);
        int end = Math.min((page + 1) * size, totalItems);
        List<ClinicalExamination> pagedExams = (start < end) ? allExams.subList(start, end) : List.of();

        Map<String, TreatmentPlan> plansMap = groupTreatmentPlansByExam(pagedExams);

        model.addAttribute("examinations", pagedExams);
        model.addAttribute("labOrders", filteredLabOrders);
        model.addAttribute("labResults", filteredLabResults);
        model.addAttribute("prescriptionDetails", filteredPrescriptionDetails);
        model.addAttribute("labOrdersByExam", groupLabOrdersByExam(pagedExams, filteredLabOrders));
        model.addAttribute("labResultsByOrder", groupLabResultsByOrder(filteredLabOrders, filteredLabResults));
        model.addAttribute("prescriptionDetailsByExam", groupPrescriptionDetailsByExam(pagedExams, filteredPrescriptionDetails));
        model.addAttribute("treatmentPlansByExam", plansMap);
        model.addAttribute("abnormalResultCount", countAbnormalResults(filteredLabResults));
        model.addAttribute("completedOrderCount", countStatus(filteredLabOrders, "completed"));
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalItems", totalItems);
        model.addAttribute("pageSize", size);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

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

        ClinicalExamination latestExam = examinations.isEmpty() ? null : examinations.get(0);
        TreatmentPlan latestTreatmentPlan = null;
        if (latestExam != null) {
            latestTreatmentPlan = treatmentPlanService.findByClinicalExamId(latestExam.getClinicalExamId()).orElse(null);
        }

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
        model.addAttribute("latestTreatmentPlan", latestTreatmentPlan);

        return "patient/risk";
    }

    @GetMapping("/patient/history")
    public String history(
            @RequestParam(name = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(name = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "5") int size,
            Model model, HttpSession session) {
        Patient patient = addCommonData(model, session, "history");

        List<ClinicalExamination> allExams = findExaminationsByPatient(patient);
        if (startDate != null) {
            allExams = allExams.stream()
                    .filter(exam -> exam.getExamDate() != null && !exam.getExamDate().isBefore(startDate.atStartOfDay()))
                    .collect(Collectors.toList());
        }
        if (endDate != null) {
            allExams = allExams.stream()
                    .filter(exam -> exam.getExamDate() != null && !exam.getExamDate().isAfter(endDate.atTime(23, 59, 59)))
                    .collect(Collectors.toList());
        }

        int totalItems = allExams.size();
        int totalPages = (int) Math.ceil((double) totalItems / size);

        int start = Math.min(page * size, totalItems);
        int end = Math.min((page + 1) * size, totalItems);
        List<ClinicalExamination> pagedExams = (start < end) ? allExams.subList(start, end) : List.of();

        List<LabOrder> allLabOrders = findLabOrdersByPatient(patient);
        List<LabResult> allLabResults = findLabResultsByPatient(patient);
        List<PrescriptionDetail> allPrescriptionDetails = findPrescriptionDetailsByPatient(patient);

        Map<String, TreatmentPlan> plansMap = groupTreatmentPlansByExam(pagedExams);

        model.addAttribute("examinations", pagedExams);
        model.addAttribute("labOrdersByExam", groupLabOrdersByExam(pagedExams, allLabOrders));
        model.addAttribute("labResultsByOrder", groupLabResultsByOrder(allLabOrders, allLabResults));
        model.addAttribute("prescriptionDetailsByExam", groupPrescriptionDetailsByExam(pagedExams, allPrescriptionDetails));
        model.addAttribute("treatmentPlansByExam", plansMap);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalItems", totalItems);
        model.addAttribute("pageSize", size);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        return "patient/history";
    }

    @GetMapping("/patient/history/detail")
    public String historyDetail(
            @RequestParam("examId") String examId,
            Model model, HttpSession session) {
        Patient patient = addCommonData(model, session, "history");
        if (patient == null) {
            return "redirect:/login";
        }

        ClinicalExamination exam = clinicalExaminationService.findById(examId).orElse(null);
        if (exam == null || exam.getPatient() == null || !patient.getUserId().equals(exam.getPatient().getUserId())) {
            return "redirect:/patient/history";
        }

        List<LabOrder> allLabOrders = findLabOrdersByPatient(patient);
        List<LabResult> allLabResults = findLabResultsByPatient(patient);
        List<PrescriptionDetail> allPrescriptionDetails = findPrescriptionDetailsByPatient(patient);

        List<ClinicalExamination> examList = List.of(exam);
        
        TreatmentPlan plan = treatmentPlanService.findByClinicalExamId(examId).orElse(null);

        model.addAttribute("exam", exam);
        model.addAttribute("labOrders", groupLabOrdersByExam(examList, allLabOrders).get(examId));
        model.addAttribute("labResultsByOrder", groupLabResultsByOrder(allLabOrders, allLabResults));
        model.addAttribute("prescriptionDetails", groupPrescriptionDetailsByExam(examList, allPrescriptionDetails).get(examId));
        model.addAttribute("treatmentPlan", plan);

        return "patient/history-detail";
    }


    @GetMapping("/patient/chat")
    public String chat(Model model, HttpSession session) {
        Patient patient = addCommonData(model, session, "chat");

        List<AIConversation> conversations = findConversationsByPatient(patient);
        
        if (conversations.isEmpty() && patient != null) {
            AIConversation newConv = new AIConversation();
            newConv.setAiConversationId(UUID.randomUUID().toString());
            newConv.setPatient(patient);
            newConv.setTopic("Hỗ trợ chăm sóc tiểu đường");
            newConv.setCreatedAt(LocalDateTime.now());
            aiConversationService.create(newConv);
            conversations = List.of(newConv);
        }

        List<AIMessage> messages = conversations.isEmpty()
                ? List.of()
                : findMessagesByConversation(conversations.get(0));

        model.addAttribute("conversations", conversations);
        model.addAttribute("messages", messages);

        return "patient/chat";
    }

    @PostMapping("/patient/chat/send")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> sendMessage(
            @RequestParam("message") String message,
            HttpSession session) {
        
        Map<String, Object> response = new HashMap<>();
        User currentUser = getCurrentUser(session);
        Patient patient = getCurrentPatient(session);

        if (currentUser == null || patient == null) {
            response.put("success", false);
            response.put("error", "Không tìm thấy phiên làm việc của bệnh nhân.");
            return ResponseEntity.ok(response);
        }

        if (message == null || message.trim().isEmpty()) {
            response.put("success", false);
            response.put("error", "Nội dung tin nhắn không được để trống.");
            return ResponseEntity.ok(response);
        }

        // Find or create conversation
        List<AIConversation> conversations = findConversationsByPatient(patient);
        AIConversation conversation;
        if (conversations.isEmpty()) {
            conversation = new AIConversation();
            conversation.setAiConversationId(UUID.randomUUID().toString());
            conversation.setPatient(patient);
            conversation.setTopic("Hỗ trợ chăm sóc tiểu đường");
            conversation.setCreatedAt(LocalDateTime.now());
            aiConversationService.create(conversation);
        } else {
            conversation = conversations.get(0);
        }

        // Save User Message
        AIMessage userMsg = new AIMessage();
        userMsg.setAiConversation(conversation);
        userMsg.setContent(message.trim());
        userMsg.setSender("Patient");
        userMsg.setTime(LocalDateTime.now());
        aiMessageService.create(userMsg);

        // Generate Reply
        String replyText = generateAiReply(patient, message.trim());

        // Save AI Message
        AIMessage aiMsg = new AIMessage();
        aiMsg.setAiConversation(conversation);
        aiMsg.setContent(replyText);
        aiMsg.setSender("AI");
        aiMsg.setTime(LocalDateTime.now());
        aiMessageService.create(aiMsg);

        response.put("success", true);
        response.put("reply", replyText);
        return ResponseEntity.ok(response);
    }

    private String generateAiReply(Patient patient, String userText) {
        String msg = userText.toLowerCase();
        
        List<LabResult> labResults = findLabResultsByPatient(patient);
        
        // 0. Check treatment plan query
        if (msg.contains("phác đồ") || msg.contains("phac do") || msg.contains("kế hoạch điều trị") 
                || msg.contains("ke hoach dieu tri") || msg.contains("mục tiêu điều trị") 
                || msg.contains("muc tieu dieu tri") || msg.contains("chế độ ăn") || msg.contains("che do an") 
                || msg.contains("tập luyện") || msg.contains("tap luyen") || msg.contains("theo dõi đường huyết") 
                || msg.contains("theo doi duong huyet")) {
            
            List<ClinicalExamination> examinations = findExaminationsByPatient(patient);
            ClinicalExamination latestExam = examinations.isEmpty() ? null : examinations.get(0);
            TreatmentPlan latestTreatmentPlan = null;
            if (latestExam != null) {
                latestTreatmentPlan = treatmentPlanService.findByClinicalExamId(latestExam.getClinicalExamId()).orElse(null);
            }
            
            if (latestTreatmentPlan == null) {
                return "Hiện tại tôi chưa thấy phác đồ điều trị chi tiết nào được bác sĩ thiết lập cho bạn. Hãy tuân thủ hướng dẫn và trao đổi thêm với bác sĩ điều trị trong lần khám tới nhé.";
            }
            
            StringBuilder sb = new StringBuilder();
            sb.append("Dưới đây là Phác đồ điều trị gần nhất của bạn (ngày khám ")
              .append(latestExam.getExamDate() != null ? latestExam.getExamDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "")
              .append("):\n\n");
            
            sb.append("- **Mục tiêu điều trị**: ").append(latestTreatmentPlan.getTreatmentGoal() != null ? latestTreatmentPlan.getTreatmentGoal() : "Chưa cập nhật").append("\n");
            sb.append("- **Chế độ ăn uống**: ").append(latestTreatmentPlan.getDietPlan() != null ? latestTreatmentPlan.getDietPlan() : "Chưa cập nhật").append("\n");
            sb.append("- **Chế độ tập luyện**: ").append(latestTreatmentPlan.getExercisePlan() != null ? latestTreatmentPlan.getExercisePlan() : "Chưa cập nhật").append("\n");
            sb.append("- **Theo dõi đường huyết**: ").append(latestTreatmentPlan.getGlucoseMonitoringPlan() != null ? latestTreatmentPlan.getGlucoseMonitoringPlan() : "Chưa cập nhật").append("\n");
            
            return sb.toString();
        }

        // 1. Check indicators query
        if (msg.contains("chỉ số") || msg.contains("chiso") || msg.contains("hba1c") 
                || msg.contains("glucose") || msg.contains("đường huyết") || msg.contains("duong huyet") 
                || msg.contains("bmi") || msg.contains("cân nặng") || msg.contains("can nang") 
                || msg.contains("chiều cao") || msg.contains("chieu cao") || msg.contains("sức khỏe") 
                || msg.contains("suc khoe") || msg.contains("nguy cơ") || msg.contains("nguy co") 
                || msg.contains("risk") || msg.contains("indicator")) {
            
            String bmi = calculateBmi(patient);
            String bmiStatus = evaluateBmiStatus(patient);
            String hba1cVal = getLatestResultValue(labResults, "hba1c", "N/A");
            String hba1cStatus = evaluateHbA1cStatus(labResults);
            String glucoseVal = getLatestResultValue(labResults, "glucose", "N/A");
            String glucoseStatus = evaluateGlucoseStatus(labResults);
            int riskScore = calculateRiskScore(labResults, patient);
            String riskLevel = getRiskLevel(riskScore);
            String riskDesc = getRiskDescription(riskScore);
            String riskAdvice = getRiskAdvice(riskScore);

            StringBuilder sb = new StringBuilder();
            sb.append("Dưới đây là phân tích các chỉ số sức khỏe gần nhất của bạn:\n\n");
            sb.append("1. Chỉ số thể hình (BMI): ").append(bmi).append(" (Phân loại: ").append(bmiStatus).append(")\n");
            sb.append("2. Chỉ số HbA1c: ").append(hba1cVal);
            if (!"N/A".equals(hba1cVal)) sb.append("%");
            sb.append(" (Trạng thái: ").append(hba1cStatus).append(")\n");
            sb.append("3. Chỉ số Đường huyết đói (Glucose): ").append(glucoseVal);
            if (!"N/A".equals(glucoseVal)) sb.append(" mg/dL");
            sb.append(" (Trạng thái: ").append(glucoseStatus).append(")\n");
            sb.append("4. Đánh giá nguy cơ biến chứng tiểu đường: ").append(riskLevel).append(" (Điểm nguy cơ: ").append(riskScore).append("/100)\n\n");
            sb.append("Chi tiết đánh giá: ").append(riskDesc).append("\n\n");
            sb.append("Lời khuyên y tế dành cho bạn: ").append(riskAdvice);
            return sb.toString();
        }
        
        // 2. Check medication query
        if (msg.contains("thuốc") || msg.contains("thuoc") || msg.contains("uống thuốc") 
                || msg.contains("uong thuoc") || msg.contains("medication") || msg.contains("medicine") 
                || msg.contains("lịch uống") || msg.contains("lich uong") || msg.contains("đơn thuốc") 
                || msg.contains("don thuoc") || msg.contains("prescription")) {
            
            List<MedicationReminderView> reminders = buildTodayMedicationReminders(patient);
            if (reminders.isEmpty()) {
                return "Hiện tại tôi chưa thấy lịch uống thuốc hay đơn thuốc nào được ghi nhận trên hệ thống của bạn. Hãy tuân thủ hướng dẫn và trao đổi thêm với bác sĩ điều trị nếu có bất kỳ đơn thuốc mới nào nhé.";
            }
            
            StringBuilder sb = new StringBuilder();
            sb.append("Lịch uống thuốc hôm nay của bạn:\n\n");
            
            Set<String> uniqueReminders = new LinkedHashSet<>();
            for (MedicationReminderView reminder : reminders) {
                String desc = "- " + reminder.getMedicationName() + " (" + reminder.getDosage() + ") uống lúc " 
                        + reminder.getMedicationTime().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")) 
                        + " (" + reminder.getTimingName() + ")";
                if (reminder.getInstruction() != null && !reminder.getInstruction().isEmpty()) {
                    desc += " - Lưu ý: " + reminder.getInstruction();
                }
                if (reminder.getMedicationPlan() != null && !reminder.getMedicationPlan().isEmpty()) {
                    desc += " (Kế hoạch: " + reminder.getMedicationPlan() + ")";
                }
                uniqueReminders.add(desc);
            }
            
            for (String r : uniqueReminders) {
                sb.append(r).append("\n");
            }
            
            sb.append("\nBạn nên đặt báo thức hoặc bật thông báo trên trình duyệt để không quên giờ uống thuốc nhé!");
            return sb.toString();
        }
        
        // 3. Check routine query
        if (msg.contains("thói quen") || msg.contains("routine") || msg.contains("sinh hoạt") 
                || msg.contains("giờ giấc") || msg.contains("gio giac") || msg.contains("ăn uống") 
                || msg.contains("an uong") || msg.contains("ngủ") || msg.contains("ngu") 
                || msg.contains("bữa ăn") || msg.contains("bua an")) {
            
            PatientRoutine routine = findRoutineByPatient(patient);
            if (routine == null) {
                return "Hiện tại bạn chưa cập nhật lịch sinh hoạt hàng ngày trên hệ thống. Bạn vui lòng vào mục 'Hồ sơ cá nhân' để cập nhật giờ giấc sinh hoạt nhé.";
            }
            
            StringBuilder sb = new StringBuilder();
            sb.append("Lịch sinh hoạt hàng ngày hiện tại của bạn:\n");
            sb.append("- Thức dậy: ").append(routine.getWakeUpTime()).append("\n");
            sb.append("- Ăn sáng: ").append(routine.getBreakfastTime()).append("\n");
            sb.append("- Ăn trưa: ").append(routine.getLunchTime()).append("\n");
            sb.append("- Ăn tối: ").append(routine.getDinnerTime()).append("\n");
            sb.append("- Đi ngủ: ").append(routine.getSleepTime()).append("\n\n");
            sb.append("Việc duy trì giờ ăn và ngủ đều đặn rất quan trọng để giúp cơ thể ổn định lượng đường huyết. Hãy cố gắng tuân thủ khung giờ này nhé!");
            return sb.toString();
        }
        
        // 4. Default Greeting / General Care
        StringBuilder sb = new StringBuilder();
        sb.append("Xin chào! Tôi là Trợ lý AI Chăm sóc Tiểu đường của bạn.\n\n");
        sb.append("Tôi có thể giúp bạn kiểm tra thông tin sức khỏe cá nhân bất cứ lúc nào. Bạn có thể hỏi tôi các câu hỏi như:\n");
        sb.append("- \"Chỉ số sức khỏe của tôi thế nào?\" để kiểm tra BMI, HbA1c, Glucose và đánh giá mức độ nguy cơ tiểu đường.\n");
        sb.append("- \"Lịch uống thuốc hôm nay của tôi\" để xem các loại thuốc cần uống và thời gian uống.\n");
        sb.append("- \"Lịch sinh hoạt của tôi\" để xem lại các khung giờ ăn ngủ đã thiết lập.\n");
        sb.append("- \"Phác đồ điều trị của tôi như thế nào?\" để xem chi tiết chế độ dinh dưỡng, luyện tập và mục tiêu điều trị của bác sĩ.\n\n");
        sb.append("Ngoài ra, hãy luôn nhớ các nguyên tắc tự chăm sóc tiểu đường cơ bản:\n");
        sb.append("- Hạn chế tinh bột tinh chế, đồ ngọt, nước có ga.\n");
        sb.append("- Ăn nhiều rau xanh, chất xơ và bổ sung protein vừa phải.\n");
        sb.append("- Duy trì vận động tối thiểu 30 phút mỗi ngày.\n");
        sb.append("- Uống đủ nước và tránh thức khuya.\n\n");
        sb.append("Hôm nay bạn cần tôi hỗ trợ thông tin gì không?");
        return sb.toString();
    }

    @PostMapping("/patient/profile/change-password")
    public String changePassword(
            @RequestParam("currentPassword") String currentPassword,
            @RequestParam("newPassword") String newPassword,
            @RequestParam("confirmPassword") String confirmPassword,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        User currentUser = getCurrentUser(session);
        
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn phải đăng nhập trước.");
            return "redirect:/login";
        }

        // Restrict changing password to patients only via this route
        if (!"PAT".equalsIgnoreCase(currentUser.getRole().getRoleId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Từ chối truy cập. Chỉ bệnh nhân mới được thay đổi mật khẩu ở đây.");
            return "redirect:/patient/profile";
        }

        if (currentPassword == null || currentPassword.trim().isEmpty()
                || newPassword == null || newPassword.trim().isEmpty()
                || confirmPassword == null || confirmPassword.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Tất cả các trường mật khẩu đều bắt buộc.");
            return "redirect:/patient/profile";
        }

        // Verify old password (stored in plain text in the project)
        if (!currentUser.getPasswordHash().equals(currentPassword.trim())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Mật khẩu hiện tại không đúng.");
            return "redirect:/patient/profile";
        }

        if (newPassword.trim().length() < 6) {
            redirectAttributes.addFlashAttribute("errorMessage", "Mật khẩu mới phải có ít nhất 6 ký tự.");
            return "redirect:/patient/profile";
        }

        if (!newPassword.trim().equals(confirmPassword.trim())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Mật khẩu mới và xác nhận mật khẩu không khớp.");
            return "redirect:/patient/profile";
        }

        // Save new password
        currentUser.setPasswordHash(newPassword.trim());
        userService.update(currentUser.getUserId(), currentUser);
        session.setAttribute("loggedInUser", currentUser);

        redirectAttributes.addFlashAttribute("successMessage", "Thay đổi mật khẩu thành công.");
        return "redirect:/patient/profile";
    }


    private Patient addCommonData(Model model, HttpSession session, String activeMenu) {
        Patient patient = getCurrentPatient(session);

        model.addAttribute("activeMenu", activeMenu);
        model.addAttribute("patient", patient);
        model.addAttribute("patientCode", patient != null ? patient.getUserId() : "");
        model.addAttribute("pageRole", "Cổng thông tin bệnh nhân");

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

    private Map<String, TreatmentPlan> groupTreatmentPlansByExam(List<ClinicalExamination> examinations) {
        Map<String, TreatmentPlan> result = new LinkedHashMap<>();

        for (ClinicalExamination examination : examinations) {
            String examId = examination.getClinicalExamId();
            TreatmentPlan plan = treatmentPlanService.findByClinicalExamId(examId).orElse(null);
            result.put(examId, plan);
        }

        return result;
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

    private String evaluateGlucoseStatus(List<LabResult> results) {
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

    private String evaluateBmiStatus(Patient patient) {
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
            return "Nguy cơ cao";
        }

        if (riskScore >= 40) {
            return "Nguy cơ trung bình";
        }

        return "Nguy cơ thấp";
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
            return "Các chỉ số hiện tại của bạn cho thấy nguy cơ sức khỏe liên quan đến tiểu đường cao. Vui lòng tuân thủ phác đồ điều trị của bác sĩ và theo dõi sát sao lượng đường huyết.";
        }

        if (riskScore >= 40) {
            return "Các chỉ số hiện tại của bạn cho thấy nguy cơ ở mức trung bình. Hãy tiếp tục theo dõi HbA1c, đường huyết đói, BMI và tuân thủ hướng dẫn y tế.";
        }

        return "Các chỉ số hiện tại của bạn cho thấy nguy cơ thấp dựa trên dữ liệu hiện có. Tiếp tục duy trì thói quen lành mạnh và tái khám định kỳ.";
    }

    private String getRiskAdvice(int riskScore) {
        if (riskScore >= 70) {
            return "Hành động khuyến nghị: liên hệ với bác sĩ khi các triệu chứng xấu đi, tuân thủ thuốc được kê đơn, giảm thực phẩm chứa nhiều đường, và đi khám đúng hẹn.";
        }

        if (riskScore >= 40) {
            return "Hành động khuyến nghị: duy trì chế độ ăn cân bằng, tập thể dục đều đặn, theo dõi lượng đường huyết, và đi tái khám định kỳ.";
        }

        return "Hành động khuyến nghị: tiếp tục ăn uống lành mạnh, hoạt động thể chất đều đặn, xét nghiệm định kỳ và duy trì thói quen phòng ngừa tiểu đường.";
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
                : "Thuốc";

        String dosage = detail.getDosage() != null
                ? detail.getDosage()
                : "";

        String instruction = detail.getMedication() != null
                ? detail.getMedication().getUsageInstruction()
                : "";

        String medicationPlan = detail.getMedicationPlan();

        boolean isDueNow = !now.isBefore(reminderTime) && now.isBefore(medicationTime);
        boolean isPast = now.isAfter(medicationTime);

        String title = "Nhắc nhở dùng thuốc";
        String message = "Uống " + medicationName + " - " + dosage + " lúc "
                + medicationTime.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"));

        return new MedicationReminderView(
                title,
                message,
                medicationName,
                dosage,
                timingName,
                instruction,
                medicationPlan,
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
        private final String medicationPlan;
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
                                      String medicationPlan,
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
            this.medicationPlan = medicationPlan;
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

        public String getMedicationPlan() {
            return medicationPlan;
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