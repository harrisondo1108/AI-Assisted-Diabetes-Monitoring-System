package com.quan.diabetes.controller.doctor;

import com.quan.diabetes.dto.doctor.ExamStep1Form;
import com.quan.diabetes.dto.doctor.ExamStep2Form;
import com.quan.diabetes.dto.doctor.ExamStep3Form;
import com.quan.diabetes.dto.doctor.MedicationLineForm;
import com.quan.diabetes.dto.doctor.PrescriptionLineDTO;
import com.quan.diabetes.entity.*;
import com.quan.diabetes.repository.*;
import com.quan.diabetes.service.exam.ClinicalExaminationService;
import com.quan.diabetes.service.user.PatientRoutineService;
import com.quan.diabetes.service.user.PatientService;
import com.quan.diabetes.service.user.ProfileService;
import com.quan.diabetes.service.systemlog.SystemLogService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;
import com.quan.diabetes.util.ParseUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;

@Controller
@RequestMapping("/doctor")
public class DoctorExamineController {

    private final ClinicalExaminationService clinicalExaminationService;
    private final SymptomsCatalogRepository symptomsCatalogRepository;
    private final LabTestCatalogRepository labTestCatalogRepository;
    private final MedicationRepository medicationRepository;
    private final ClinicalExaminationRepository clinicalExaminationRepository;
    private final LabResultRepository labResultRepository;
    private final ExamSymptomRepository examSymptomRepository;
    private final MedicationTimingRepository medicationTimingRepository;
    private final PatientTypeRepository patientTypeRepository;
    private final IndicatorThresholdRepository indicatorThresholdRepository;
    private final PatientRoutineService patientRoutineService;
    private final PatientRepository patientRepository;

    @Autowired
    private SystemLogService systemLogService;

    public DoctorExamineController(
            ClinicalExaminationService clinicalExaminationService,
            SymptomsCatalogRepository symptomsCatalogRepository,
            LabTestCatalogRepository labTestCatalogRepository,
            MedicationRepository medicationRepository,
            ClinicalExaminationRepository clinicalExaminationRepository,
            LabResultRepository labResultRepository,
            ExamSymptomRepository examSymptomRepository,
            MedicationTimingRepository medicationTimingRepository,
            PatientTypeRepository patientTypeRepository,
            IndicatorThresholdRepository indicatorThresholdRepository,
            PatientRoutineService patientRoutineService,
            PatientRepository patientRepository) {
        this.clinicalExaminationService = clinicalExaminationService;
        this.symptomsCatalogRepository = symptomsCatalogRepository;
        this.labTestCatalogRepository = labTestCatalogRepository;
        this.medicationRepository = medicationRepository;
        this.clinicalExaminationRepository = clinicalExaminationRepository;
        this.labResultRepository = labResultRepository;
        this.examSymptomRepository = examSymptomRepository;
        this.medicationTimingRepository = medicationTimingRepository;
        this.patientTypeRepository = patientTypeRepository;
        this.indicatorThresholdRepository = indicatorThresholdRepository;
        this.patientRoutineService = patientRoutineService;
        this.patientRepository = patientRepository;
    }

    @GetMapping("/examine")
    public String redirectExamine(@RequestParam(value = "patientId", required = false) String patientId,
            @RequestParam(value = "warning", required = false) String warning,
            HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !"DOC".equalsIgnoreCase(loggedInUser.getRole().getRoleId())) {
            return "redirect:/login";
        }
        if (patientId != null) {
            clinicalExaminationService.startExamination(patientId, loggedInUser.getUserId());
            session.setAttribute("selectedPatientId", patientId);
            session.setAttribute("examineViewOnly", "false");
            session.setAttribute("examineEditMode", false);

            ClinicalExamination exam = clinicalExaminationRepository
                    .findFirstByPatient_UserIdAndDoctor_UserIdAndStatusIn(patientId, loggedInUser.getUserId(),
                            List.of("InProgress", "Pending"))
                    .orElse(null);
            if (exam != null) {
                return "redirect:/doctor/examine/" + exam.getClinicalExamId() + "/step1";
            }
        }
        return "redirect:/doctor/queue";
    }

    @PostMapping("/examine/{patientId}/start")
    public String startExam(@PathVariable("patientId") String patientId, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !"DOC".equalsIgnoreCase(loggedInUser.getRole().getRoleId())) {
            return "redirect:/login";
        }

        try {
            clinicalExaminationService.startExamination(patientId, loggedInUser.getUserId());
        } catch (Exception e) {
            systemLogService.saveLog(loggedInUser.getUserId(), "APPROVE_MEDICAL_RECORD", "MedicalRecord", null, "Bác sĩ tiếp nhận bệnh án thất bại: " + e.getMessage(), null, null, "FAILED");
            throw e;
        }
        session.setAttribute("selectedPatientId", patientId);
        session.setAttribute("examineViewOnly", "false");
        session.setAttribute("examineEditMode", false);

        // Find the InProgress exam to get its ID and redirect to step1
        ClinicalExamination exam = clinicalExaminationRepository
                .findFirstByPatient_UserIdAndDoctor_UserIdAndStatusIn(patientId, loggedInUser.getUserId(),
                        List.of("Pending", "InProgress"))
                .orElse(null);
        if (exam != null) {
            // Clear prescription session for fresh exam
            session.removeAttribute("prescriptionLines");
            session.removeAttribute("prescriptionExamId");
            return "redirect:/doctor/examine/" + exam.getClinicalExamId() + "/step1";
        }
        return "redirect:/doctor/examine";
    }

    @GetMapping("/examine/{patientId}/cancel")
    public String cancelExaminePage(@PathVariable("patientId") String patientId, Model model, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !"DOC".equalsIgnoreCase(loggedInUser.getRole().getRoleId())) {
            return "redirect:/login";
        }

        Patient patient = patientRepository.findById(patientId).orElse(null);
        if (patient == null) {
            return "redirect:/doctor/queue";
        }
        model.addAttribute("patient", patient);
        return "doctor/cancel-exam";
    }

    @PostMapping("/examine/{patientId}/cancel")
    public String cancelExamine(
            @PathVariable("patientId") String patientId,
            @RequestParam("cancelReason") String cancelReason,
            HttpSession session,
            Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !"DOC".equalsIgnoreCase(loggedInUser.getRole().getRoleId())) {
            return "redirect:/login";
        }

        if (cancelReason == null || cancelReason.trim().isEmpty()) {
            Patient patient = patientRepository.findById(patientId).orElse(null);
            model.addAttribute("patient", patient);
            model.addAttribute("error", "Lý do hủy không được để trống hoặc chỉ chứa khoảng trắng.");
            return "doctor/cancel-exam";
        }

        try {
            clinicalExaminationService.cancelExamination(patientId, reason, loggedInUser.getUserId());
        } catch (Exception e) {
            systemLogService.saveLog(loggedInUser.getUserId(), "REJECT_MEDICAL_RECORD", "MedicalRecord", null, "Bác sĩ từ chối/hủy bệnh án thất bại: " + e.getMessage(), null, null, "FAILED");
            throw e;
        }
        return "redirect:/doctor/queue?toast=cancelled";
    }

    @GetMapping("/examine/edit/{examId}")
    public String editExaminePage(
            @PathVariable("examId") String examId,
            HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !"DOC".equalsIgnoreCase(loggedInUser.getRole().getRoleId())) {
            return "redirect:/login";
        }

        ClinicalExamination exam = clinicalExaminationRepository.findById(examId)
                .orElse(null);
        if (exam == null)
            return "redirect:/doctor/queue";

        if (!exam.getDoctor().getUserId().equalsIgnoreCase(loggedInUser.getUserId())) {
            return "redirect:/doctor/queue";
        }

        if (exam.getExamDate() != null && !exam.getExamDate().toLocalDate().isEqual(java.time.LocalDate.now())) {
            return "redirect:/doctor/queue?toast=not_today";
        }

        // Clear prescription session cache so Tab 4 reloads from DB for this exam
        String sessionExamId = (String) session.getAttribute("prescriptionExamId");
        if (sessionExamId == null || !sessionExamId.equals(examId)) {
            session.removeAttribute("prescriptionLines");
            session.removeAttribute("prescriptionExamId");
        }

        session.setAttribute("examineEditMode", true);

        return "redirect:/doctor/examine/" + examId + "/step1";
    }

    private List<PrescriptionLineDTO> mapPrescriptionDetailsToDTO(List<PrescriptionDetail> details) {
        List<PrescriptionLineDTO> list = new ArrayList<>();
        if (details == null)
            return list;
        for (PrescriptionDetail p : details) {
            PrescriptionLineDTO dto = new PrescriptionLineDTO();
            dto.setMedId(p.getMedication().getMedicationId());
            dto.setName(p.getMedication().getMedicationName());
            dto.setConcentration(p.getMedication().getConcentration());
            dto.setForm(p.getMedication().getForm());
            dto.setDosage(p.getDosage());
            dto.setDosagePerDose(parseDosagePerDose(p.getDosage()));
            dto.setDuration(p.getDurationDays());
            dto.setQuantity(p.getTotalQuantity());

            List<String> timings = new ArrayList<>();
            if (p.getPrescriptionTimings() != null) {
                for (PrescriptionTiming pt : p.getPrescriptionTimings()) {
                    timings.add(pt.getTiming().getTimingName());
                }
            }
            dto.setTiming(timings);
            dto.setTimingText(String.join(", ", timings));
            dto.setMedicationPlan(p.getMedicationPlan());
            dto.setStartDate(p.getStartDate() != null ? p.getStartDate().toString() : "");
            dto.setEndDate(p.getEndDate() != null ? p.getEndDate().toString() : "");
            list.add(dto);
        }
        return list;
    }

    private Double parseDosagePerDose(String dosageStr) {
        if (dosageStr == null || dosageStr.equalsIgnoreCase("Auto"))
            return 1.0;
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("^([\\d.]+)").matcher(dosageStr);
        return m.find() ? Double.parseDouble(m.group(1)) : 1.0;
    }

    private String getMedicationUnit(String form) {
        if (form == null)
            return "viên";
        String f = form.toLowerCase();
        if (f.contains("viên") || f.contains("tablet") || f.contains("capsule"))
            return "viên";
        if (f.contains("gói") || f.contains("sachet"))
            return "gói";
        if (f.contains("chai") || f.contains("bottle") || f.contains("lọ"))
            return "chai";
        if (f.contains("ống") || f.contains("ampoule"))
            return "ống";
        if (f.contains("tuýp") || f.contains("tube"))
            return "tuýp";
        return "đơn vị";
    }

    private String formatLocalTime(java.time.LocalTime time) {
        if (time == null)
            return "N/A";
        return time.format(java.time.format.DateTimeFormatter.ofPattern("hh:mm a"));
    }

    // =========================================================================
    // NEW TAB-BASED STEP ENDPOINTS
    // =========================================================================

    /** Helper: load common model attributes for all step pages */
    private void populateStepCommonModel(String examId, HttpSession session, Model model, User loggedInUser) {
        ClinicalExamination exam = clinicalExaminationRepository.findById(examId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy ca khám: " + examId));
        Patient patient = exam.getPatient();
        model.addAttribute("exam", exam);
        model.addAttribute("patient", patient);
        model.addAttribute("routine", patientRoutineService.findById(patient.getUserId()).orElse(null));
        model.addAttribute("tomorrowDate", java.time.LocalDate.now().plusDays(1).toString());
    }

    /** Resolve targetTab param to redirect URL */
    private String redirectToTab(String examId, String targetTab) {
        int tab = 1;
        try {
            tab = Integer.parseInt(targetTab);
        } catch (Exception ignored) {
        }
        return switch (tab) {
            case 2 -> "redirect:/doctor/examine/" + examId + "/step2";
            case 3 -> "redirect:/doctor/examine/" + examId + "/step3";
            case 4 -> "redirect:/doctor/examine/" + examId + "/step4";
            default -> "redirect:/doctor/examine/" + examId + "/step1";
        };
    }

    // ----- STEP 1 -----

    @GetMapping("/examine/{examId}/step1")
    public String step1Page(@PathVariable String examId, HttpSession session, Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !"DOC".equalsIgnoreCase(loggedInUser.getRole().getRoleId()))
            return "redirect:/login";

        populateStepCommonModel(examId, session, model, loggedInUser);

        ClinicalExamination exam = clinicalExaminationRepository.findById(examId).orElseThrow();

        // Pre-populate form from DB
        ExamStep1Form form = new ExamStep1Form();
        form.setMedicalHistory(exam.getMedicalHistory());

        // Load saved symptoms
        List<ExamSymptom> saved = examSymptomRepository.findAll().stream()
                .filter(s -> s.getId().getClinicalExamId().equals(examId))
                .collect(Collectors.toList());
        form.setSymptomIds(saved.stream().map(s -> s.getSymptom().getSymptomId()).collect(Collectors.toList()));
        Map<String, String> comments = new HashMap<>();
        saved.forEach(s -> comments.put(s.getSymptom().getSymptomId(), s.getNote() != null ? s.getNote() : ""));
        form.setSymptomComments(comments);

        model.addAttribute("step1Form", form);
        model.addAttribute("symptomsCatalog", symptomsCatalogRepository.findAll());
        return "doctor/exam-step1";
    }

    @PostMapping("/examine/{examId}/step1/save")
    public String step1Save(@PathVariable String examId,
            @Valid @ModelAttribute("step1Form") ExamStep1Form form,
            org.springframework.validation.BindingResult result,
            @RequestParam(value = "targetTab", defaultValue = "1") String targetTab,
            HttpSession session, Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !"DOC".equalsIgnoreCase(loggedInUser.getRole().getRoleId()))
            return "redirect:/login";

        if (result.hasErrors()) {
            populateStepCommonModel(examId, session, model, loggedInUser);
            model.addAttribute("symptomsCatalog", symptomsCatalogRepository.findAll());
            return "doctor/exam-step1";
        }

        clinicalExaminationService.saveStep1(examId, form);
        return redirectToTab(examId, targetTab);
    }

    // ----- STEP 2 -----

    @GetMapping("/examine/{examId}/step2")
    public String step2Page(@PathVariable String examId, HttpSession session, Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !"DOC".equalsIgnoreCase(loggedInUser.getRole().getRoleId()))
            return "redirect:/login";

        populateStepCommonModel(examId, session, model, loggedInUser);

        ExamStep2Form form = new ExamStep2Form();
        Boolean isPregnant = (Boolean) session.getAttribute("isPregnant_" + examId);
        form.setIsPregnant(Boolean.TRUE.equals(isPregnant));

        ClinicalExamination exam = clinicalExaminationRepository.findById(examId).orElseThrow();
        form.setDiagnosisNote(exam.getDiagnosisNote());

        model.addAttribute("step2Form", form);

        // Load lab results
        List<LabResult> labResults = labResultRepository.findByLabOrder_ClinicalExamination_ClinicalExamId(examId);
        model.addAttribute("labResults", labResults);
        return "doctor/exam-step2";
    }

    @PostMapping("/examine/{examId}/step2/save")
    public String step2Save(@PathVariable String examId,
            @Valid @ModelAttribute("step2Form") ExamStep2Form form,
            org.springframework.validation.BindingResult result,
            @RequestParam(value = "targetTab", defaultValue = "2") String targetTab,
            @RequestParam(value = "orderLabs", required = false) String orderLabsParam,
            HttpSession session, Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !"DOC".equalsIgnoreCase(loggedInUser.getRole().getRoleId()))
            return "redirect:/login";

        if (result.hasErrors()) {
            populateStepCommonModel(examId, session, model, loggedInUser);
            List<LabResult> labResults = labResultRepository.findByLabOrder_ClinicalExamination_ClinicalExamId(examId);
            model.addAttribute("labResults", labResults);
            return "doctor/exam-step2";
        }

        // Store pregnancy in session
        session.setAttribute("isPregnant_" + examId, Boolean.TRUE.equals(form.getIsPregnant()));

        // Set orderLabs flag (button value="true" sent as request param)
        if ("true".equalsIgnoreCase(orderLabsParam)) {
            form.setOrderLabs(true);
        }

        // Resolve patient type for lab thresholds
        ClinicalExamination exam = clinicalExaminationRepository.findById(examId).orElseThrow();
        Patient patient = exam.getPatient();
        PatientType matchedType = null;
        if (Boolean.TRUE.equals(form.getIsPregnant()) && patient != null && Boolean.TRUE.equals(patient.getGender())) {
            matchedType = patientTypeRepository.findAll().stream()
                    .filter(t -> t.getTypeName().equalsIgnoreCase("Pregnant")).findFirst().orElse(null);
        } else if (patient != null && patient.getDob() != null) {
            int age = java.time.Period.between(patient.getDob(), java.time.LocalDate.now()).getYears();
            final int finalAge = age;
            matchedType = patientTypeRepository.findAll().stream()
                    .filter(t -> t.getMinAge() != null && t.getMaxAge() != null
                            && finalAge >= t.getMinAge() && finalAge <= t.getMaxAge())
                    .findFirst().orElse(null);
        }

        List<LabTestCatalog> testCatalog = labTestCatalogRepository.findAll().stream()
                .filter(l -> l.getStatus() == null || l.getStatus()).collect(Collectors.toList());

        clinicalExaminationService.saveStep2(examId, form, matchedType, testCatalog);

        // If order-labs was pressed, always stay on step 2 to show results
        if (Boolean.TRUE.equals(form.getOrderLabs())) {
            return "redirect:/doctor/examine/" + examId + "/step2";
        }
        return redirectToTab(examId, targetTab);
    }

    // ----- STEP 3 -----

    @GetMapping("/examine/{examId}/step3")
    public String step3Page(@PathVariable String examId, HttpSession session, Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !"DOC".equalsIgnoreCase(loggedInUser.getRole().getRoleId()))
            return "redirect:/login";

        populateStepCommonModel(examId, session, model, loggedInUser);

        ClinicalExamination exam = clinicalExaminationRepository.findById(examId).orElseThrow();
        ExamStep3Form form = new ExamStep3Form();
        if (exam.getNextAppointment() != null)
            form.setNextAppointment(exam.getNextAppointment().toLocalDate().toString());
        if (exam.getTreatmentPlan() != null) {
            form.setTreatmentGoal(exam.getTreatmentPlan().getTreatmentGoal());
            form.setDietPlan(exam.getTreatmentPlan().getDietPlan());
            form.setExercisePlan(exam.getTreatmentPlan().getExercisePlan());
            form.setGlucoseMonitoringPlan(exam.getTreatmentPlan().getGlucoseMonitoringPlan());
        }
        model.addAttribute("step3Form", form);
        return "doctor/exam-step3";
    }

    @PostMapping("/examine/{examId}/step3/save")
    public String step3Save(@PathVariable String examId,
            @Valid @ModelAttribute("step3Form") ExamStep3Form form,
            org.springframework.validation.BindingResult result,
            @RequestParam(value = "targetTab", defaultValue = "3") String targetTab,
            HttpSession session, Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !"DOC".equalsIgnoreCase(loggedInUser.getRole().getRoleId()))
            return "redirect:/login";

        if (result.hasErrors()) {
            populateStepCommonModel(examId, session, model, loggedInUser);
            return "doctor/exam-step3";
        }

        clinicalExaminationService.saveStep3(examId, form);
        return redirectToTab(examId, targetTab);
    }

    // ----- STEP 4 -----

    @GetMapping("/examine/{examId}/step4")
    public String step4Page(@PathVariable String examId,
            @RequestParam(value = "editIndex", required = false) Integer editIndex,
            HttpSession session, Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !"DOC".equalsIgnoreCase(loggedInUser.getRole().getRoleId()))
            return "redirect:/login";

        populateStepCommonModel(examId, session, model, loggedInUser);

        // Load prescription lines from session; if not loaded yet for this exam, load
        // from DB
        @SuppressWarnings("unchecked")
        List<PrescriptionLineDTO> lines = (List<PrescriptionLineDTO>) session.getAttribute("prescriptionLines");
        String sessionExamId = (String) session.getAttribute("prescriptionExamId");
        if (lines == null || sessionExamId == null || !sessionExamId.equals(examId)) {
            lines = clinicalExaminationService.getPrescriptionLines(examId);
            session.setAttribute("prescriptionLines", lines);
            session.setAttribute("prescriptionExamId", examId);
        }
        model.addAttribute("prescriptionLines", lines);
        model.addAttribute("medicationsCatalog", medicationRepository.findAll());
        model.addAttribute("medicationTimings", medicationTimingRepository.findAll());

        // Prepare inline add/edit form
        MedicationLineForm medForm;
        if (model.containsAttribute("medForm")) {
            medForm = (MedicationLineForm) model.getAttribute("medForm");
        } else {
            medForm = new MedicationLineForm();
            medForm.setEditIndex(-1);
            medForm.setDuration(30);
            medForm.setDosagePerDose(1.0);
            medForm.setStartDate(java.time.LocalDate.now().plusDays(1).toString());

            if (editIndex != null && editIndex >= 0 && editIndex < lines.size()) {
                PrescriptionLineDTO editingMed = lines.get(editIndex);
                medForm.setEditIndex(editIndex);
                medForm.setMedId(editingMed.getMedId());
                medForm.setDosagePerDose(editingMed.getDosagePerDose());
                medForm.setDuration(editingMed.getDuration());
                medForm.setStartDate(editingMed.getStartDate());
                medForm.setEndDate(editingMed.getEndDate());
                medForm.setQuantity(editingMed.getQuantity());
                medForm.setTiming(editingMed.getTiming());
                medForm.setMedicationPlan(editingMed.getMedicationPlan());
            }
            model.addAttribute("medForm", medForm);
        }

        PrescriptionLineDTO editingMed = null;
        if (medForm.getEditIndex() != null && medForm.getEditIndex() >= 0 && medForm.getEditIndex() < lines.size()) {
            editingMed = lines.get(medForm.getEditIndex());
        }
        model.addAttribute("editingMed", editingMed);

        return "doctor/exam-step4";
    }

    /** Navigate between tabs from step4 (no form data to save) */
    @PostMapping("/examine/{examId}/step4/navigate")
    public String step4Navigate(@PathVariable String examId,
            @RequestParam(value = "targetTab", defaultValue = "4") String targetTab,
            HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !"DOC".equalsIgnoreCase(loggedInUser.getRole().getRoleId()))
            return "redirect:/login";
        return redirectToTab(examId, targetTab);
    }

    /** Add or update a single medication line in session */
    @PostMapping("/examine/{examId}/step4/add")
    public String step4AddMedication(@PathVariable String examId,
            @Valid @ModelAttribute("medForm") MedicationLineForm form,
            org.springframework.validation.BindingResult result,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes,
            HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !"DOC".equalsIgnoreCase(loggedInUser.getRole().getRoleId()))
            return "redirect:/login";

        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.medForm", result);
            redirectAttributes.addFlashAttribute("medForm", form);
            return "redirect:/doctor/examine/" + examId + "/step4";
        }

        if (form.getMedId() == null || form.getMedId().trim().isEmpty())
            return "redirect:/doctor/examine/" + examId + "/step4";

        Medication med = medicationRepository.findById(form.getMedId()).orElse(null);
        if (med == null)
            return "redirect:/doctor/examine/" + examId + "/step4";

        // Calculate quantity and end date
        int duration = form.getDuration() != null ? form.getDuration() : 30;
        double dosePerTime = form.getDosagePerDose() != null ? form.getDosagePerDose() : 1.0;
        int timingCount = (form.getTiming() != null && !form.getTiming().isEmpty()) ? form.getTiming().size() : 1;
        int quantity = (int) Math.ceil(dosePerTime * timingCount * duration);

        String startDate = form.getStartDate();
        String endDate = "";
        if (startDate != null && !startDate.isEmpty()) {
            endDate = java.time.LocalDate.parse(startDate).plusDays(duration - 1).toString();
        }

        // Build DTO
        PrescriptionLineDTO dto = new PrescriptionLineDTO();
        dto.setMedId(med.getMedicationId());
        dto.setName(med.getMedicationName());
        dto.setConcentration(med.getConcentration());
        dto.setForm(med.getForm());
        dto.setDuration(duration);
        dto.setDosagePerDose(dosePerTime);
        dto.setQuantity(quantity);
        dto.setStartDate(startDate);
        dto.setEndDate(endDate);
        dto.setTiming(form.getTiming() != null ? form.getTiming() : List.of());
        dto.setTimingText(String.join(", ", dto.getTiming()));
        dto.setMedicationPlan(form.getMedicationPlan());
        // Compute display dosage string
        String dosageDisplay = String.format(java.util.Locale.US, "%.0f", dosePerTime) + " viên/lần";
        dto.setDosage(dosageDisplay);

        @SuppressWarnings("unchecked")
        List<PrescriptionLineDTO> lines = (List<PrescriptionLineDTO>) session.getAttribute("prescriptionLines");
        if (lines == null)
            lines = new ArrayList<>();

        Integer editIdx = form.getEditIndex();
        if (editIdx != null && editIdx >= 0 && editIdx < lines.size()) {
            lines.set(editIdx, dto);
        } else {
            lines.add(dto);
        }
        session.setAttribute("prescriptionLines", lines);
        session.setAttribute("prescriptionExamId", examId);
        return "redirect:/doctor/examine/" + examId + "/step4";
    }

    /** Delete a medication line from session */
    @PostMapping("/examine/{examId}/step4/delete")
    public String step4DeleteMedication(@PathVariable String examId,
            @RequestParam("deleteIndex") int deleteIndex,
            HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !"DOC".equalsIgnoreCase(loggedInUser.getRole().getRoleId()))
            return "redirect:/login";

        @SuppressWarnings("unchecked")
        List<PrescriptionLineDTO> lines = (List<PrescriptionLineDTO>) session.getAttribute("prescriptionLines");
        if (lines != null && deleteIndex >= 0 && deleteIndex < lines.size()) {
            lines.remove(deleteIndex);
            session.setAttribute("prescriptionLines", lines);
        }
        return "redirect:/doctor/examine/" + examId + "/step4";
    }

    /** Complete the exam: save prescription + mark Completed */
    @PostMapping("/examine/{examId}/step4/complete")
    public String step4Complete(@PathVariable String examId, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !"DOC".equalsIgnoreCase(loggedInUser.getRole().getRoleId()))
            return "redirect:/login";

        @SuppressWarnings("unchecked")
        List<PrescriptionLineDTO> lines = (List<PrescriptionLineDTO>) session.getAttribute("prescriptionLines");
        clinicalExaminationService.completeExamination(examId, lines);

        Boolean isEditMode = (Boolean) session.getAttribute("examineEditMode");

        // Clean up session
        session.removeAttribute("prescriptionLines");
        session.removeAttribute("prescriptionExamId");
        session.removeAttribute("isPregnant_" + examId);
        session.removeAttribute("examineEditMode");

        if (Boolean.TRUE.equals(isEditMode)) {
            return "redirect:/doctor/queue?toast=updated";
        } else {
            return "redirect:/doctor/queue?toast=completed";
        }
    }
}
