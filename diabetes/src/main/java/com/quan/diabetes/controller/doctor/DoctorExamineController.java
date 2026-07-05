package com.quan.diabetes.controller.doctor;

import com.quan.diabetes.dto.doctor.ClinicalExamForm;
import com.quan.diabetes.entity.*;
import com.quan.diabetes.repository.*;

import com.quan.diabetes.service.exam.ClinicalExaminationService;
import com.quan.diabetes.service.user.PatientRoutineService;
import com.quan.diabetes.service.user.PatientService;
import com.quan.diabetes.service.user.ProfileService;
import jakarta.servlet.http.HttpSession;
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
    private final PatientService patientService;
    private final ProfileService profileService;
    private final SymptomsCatalogRepository symptomsCatalogRepository;
    private final LabTestCatalogRepository labTestCatalogRepository;
    private final MedicationRepository medicationRepository;
    private final ClinicalExaminationRepository clinicalExaminationRepository;
    private final LabResultRepository labResultRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final PrescriptionDetailRepository prescriptionDetailRepository;
    private final TreatmentPlanRepository treatmentPlanRepository;
    private final ExamSymptomRepository examSymptomRepository;
    private final PatientRoutineService patientRoutineService;
    private final MedicationTimingRepository medicationTimingRepository;
    private final PatientTypeRepository patientTypeRepository;
    private final IndicatorThresholdRepository indicatorThresholdRepository;

    public DoctorExamineController(
            ClinicalExaminationService clinicalExaminationService,
            PatientService patientService,
            ProfileService profileService,
            SymptomsCatalogRepository symptomsCatalogRepository,
            LabTestCatalogRepository labTestCatalogRepository,
            MedicationRepository medicationRepository,
            ClinicalExaminationRepository clinicalExaminationRepository,
            LabResultRepository labResultRepository,
            PrescriptionRepository prescriptionRepository,
            PrescriptionDetailRepository prescriptionDetailRepository,
            TreatmentPlanRepository treatmentPlanRepository,
            ExamSymptomRepository examSymptomRepository,
            PatientRoutineService patientRoutineService,
            MedicationTimingRepository medicationTimingRepository,
            PatientTypeRepository patientTypeRepository,
            IndicatorThresholdRepository indicatorThresholdRepository) {
        this.clinicalExaminationService = clinicalExaminationService;
        this.patientService = patientService;
        this.profileService = profileService;
        this.symptomsCatalogRepository = symptomsCatalogRepository;
        this.labTestCatalogRepository = labTestCatalogRepository;
        this.medicationRepository = medicationRepository;
        this.clinicalExaminationRepository = clinicalExaminationRepository;
        this.labResultRepository = labResultRepository;
        this.prescriptionRepository = prescriptionRepository;
        this.prescriptionDetailRepository = prescriptionDetailRepository;
        this.treatmentPlanRepository = treatmentPlanRepository;
        this.examSymptomRepository = examSymptomRepository;
        this.patientRoutineService = patientRoutineService;
        this.medicationTimingRepository = medicationTimingRepository;
        this.patientTypeRepository = patientTypeRepository;
        this.indicatorThresholdRepository = indicatorThresholdRepository;
    }

    @GetMapping("/examine")
    public String examinePage(
            @RequestParam(value = "patientId", required = false) String patientId,
            @RequestParam(value = "viewOnly", required = false) Boolean viewOnlyParam,
            HttpSession session,
            Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !"DOC".equalsIgnoreCase(loggedInUser.getRole().getRoleId())) {
            return "redirect:/login";
        }

        populateExamineModel(patientId, viewOnlyParam, session, model, loggedInUser);
        model.addAttribute("examForm", new ClinicalExamForm());

        return "doctor/examine";
    }

    private void populateExamineModel(String patientId, Boolean viewOnlyParam, HttpSession session, Model model, User loggedInUser) {
        String doctorId = loggedInUser.getUserId();
        Profile profile = profileService.findById(doctorId).orElse(null);
        model.addAttribute("doctorProfile", profile);

        // Check if there is an in-progress exam for this doctor in the database
        Optional<ClinicalExamination> activeExamOpt = clinicalExaminationRepository
                .findFirstByDoctor_UserIdAndStatus(doctorId, "InProgress");

        if (activeExamOpt.isPresent()) {
            patientId = activeExamOpt.get().getPatient().getUserId();
            session.setAttribute("selectedPatientId", patientId);
            session.setAttribute("examineViewOnly", "false");
        } else {
            if (patientId != null) {
                session.setAttribute("selectedPatientId", patientId);
            }
            if (viewOnlyParam != null) {
                session.setAttribute("examineViewOnly", viewOnlyParam ? "true" : "false");
            }
        }

        // Lấy selectedPatientId từ session
        String selectedPatientId = (String) session.getAttribute("selectedPatientId");
        if (selectedPatientId == null) {
            return;
        }

        Patient patient = patientService.findById(selectedPatientId).orElse(null);
        if (patient == null) {
            return;
        }
        model.addAttribute("patient", patient);

        // Pre-populate JS inline variables with safe default objects
        model.addAttribute("patientData", null);
        model.addAttribute("activeExamData", null);
        model.addAttribute("lastExamData", null);
        model.addAttribute("lastExamLabResultsData", Collections.emptyList());
        model.addAttribute("lastExamPrescriptionDetailsData", Collections.emptyList());
        model.addAttribute("labTestsCatalogData", Collections.emptyList());
        model.addAttribute("chosenSymptomNotes", Collections.emptyMap());

        // Convert patient to map for safe JS serialization without circular reference
        Map<String, Object> patientMap = new HashMap<>();
        patientMap.put("userId", patient.getUserId());
        patientMap.put("fullName", patient.getFullName());
        patientMap.put("dob", patient.getDob() != null ? patient.getDob().toString() : null);
        patientMap.put("gender", patient.getGender());
        patientMap.put("height", patient.getHeight());
        patientMap.put("weight", patient.getWeight());
        patientMap.put("bloodgroup", patient.getBloodgroup());
        patientMap.put("permanentMedicalHistory", patient.getPermanentMedicalHistory());
        patientMap.put("allergyNotes", patient.getAllergyNotes());
        model.addAttribute("patientData", patientMap);

        // Lấy thông tin Routine của bệnh nhân
        PatientRoutine routine = patientRoutineService.findById(selectedPatientId).orElse(null);
        model.addAttribute("routine", routine);
        Map<String, Object> routineMap = null;
        if (routine != null) {
            routineMap = new HashMap<>();
            routineMap.put("breakfastTime", routine.getBreakfastTime() != null ? routine.getBreakfastTime().toString() : null);
            routineMap.put("lunchTime", routine.getLunchTime() != null ? routine.getLunchTime().toString() : null);
            routineMap.put("dinnerTime", routine.getDinnerTime() != null ? routine.getDinnerTime().toString() : null);
            routineMap.put("sleepTime", routine.getSleepTime() != null ? routine.getSleepTime().toString() : null);
            routineMap.put("wakeUpTime", routine.getWakeUpTime() != null ? routine.getWakeUpTime().toString() : null);
        }
        model.addAttribute("routineData", routineMap);

        // Lấy danh mục triệu chứng (Active)
        List<SymptomsCatalog> symptomsCatalog = symptomsCatalogRepository.findAll().stream()
                .filter(s -> s.getStatus() == null || s.getStatus())
                .collect(Collectors.toList());
        model.addAttribute("symptomsCatalog", symptomsCatalog);

        // Lấy danh mục xét nghiệm (Active)
        List<LabTestCatalog> labTestsCatalog = labTestCatalogRepository.findAll().stream()
                .filter(l -> l.getStatus() == null || l.getStatus())
                .collect(Collectors.toList());
        model.addAttribute("labTestsCatalog", labTestsCatalog);

        // Tính tuổi để xác định khoảng tham chiếu/ngưỡng động từ CSDL
        int age = 0;
        if (patient.getDob() != null) {
            age = java.time.Period.between(patient.getDob(), java.time.LocalDate.now()).getYears();
        }
        final int finalAge = age;
        PatientType matchedType = patientTypeRepository.findAll().stream()
                .filter(t -> t.getMinAge() != null && t.getMaxAge() != null && finalAge >= t.getMinAge() && finalAge <= t.getMaxAge())
                .findFirst()
                .orElse(null);

        // Map labTestsCatalog for JS catalog to include referenceRange
        List<Map<String, Object>> labCatalogList = new ArrayList<>();
        if (labTestsCatalog != null) {
            for (LabTestCatalog l : labTestsCatalog) {
                Map<String, Object> lMap = new HashMap<>();
                lMap.put("testId", l.getLabTestId());
                lMap.put("testName", l.getTestName());
                lMap.put("unit", l.getUnit());
                
                Optional<IndicatorThreshold> thresholdOpt = Optional.empty();
                if (matchedType != null) {
                    thresholdOpt = indicatorThresholdRepository.findByLabTest_LabTestIdAndPatientType_PatientTypeId(
                        l.getLabTestId(), matchedType.getPatientTypeId()
                    );
                }
                if (thresholdOpt.isEmpty()) {
                    List<IndicatorThreshold> thresholds = indicatorThresholdRepository.findByLabTest_LabTestId(l.getLabTestId());
                    if (!thresholds.isEmpty()) {
                        thresholdOpt = Optional.of(thresholds.get(0));
                    }
                }
                
                String refRange = "N/A";
                if (thresholdOpt.isPresent()) {
                    refRange = thresholdOpt.get().getMinValue() + " - " + thresholdOpt.get().getMaxValue();
                    lMap.put("minValue", thresholdOpt.get().getMinValue());
                    lMap.put("maxValue", thresholdOpt.get().getMaxValue());
                } else {
                    lMap.put("minValue", null);
                    lMap.put("maxValue", null);
                }
                
                lMap.put("referenceRange", refRange);
                labCatalogList.add(lMap);
            }
        }
        model.addAttribute("labTestsCatalogData", labCatalogList);

        // Map labTestsPregnantCatalogData for JS catalog to swap in real-time
        PatientType pregnantType = patientTypeRepository.findAll().stream()
                .filter(t -> t.getTypeName().equalsIgnoreCase("Pregnant"))
                .findFirst()
                .orElse(null);
        
        List<Map<String, Object>> labPregnantCatalogList = new ArrayList<>();
        if (labTestsCatalog != null && pregnantType != null) {
            for (LabTestCatalog l : labTestsCatalog) {
                Map<String, Object> lMap = new HashMap<>();
                lMap.put("testId", l.getLabTestId());
                lMap.put("testName", l.getTestName());
                lMap.put("unit", l.getUnit());
                
                Optional<IndicatorThreshold> thresholdOpt = indicatorThresholdRepository.findByLabTest_LabTestIdAndPatientType_PatientTypeId(
                    l.getLabTestId(), pregnantType.getPatientTypeId()
                );
                
                String refRange = "N/A";
                if (thresholdOpt.isPresent()) {
                    refRange = thresholdOpt.get().getMinValue() + " - " + thresholdOpt.get().getMaxValue();
                    lMap.put("minValue", thresholdOpt.get().getMinValue());
                    lMap.put("maxValue", thresholdOpt.get().getMaxValue());
                } else {
                    lMap.put("minValue", null);
                    lMap.put("maxValue", null);
                }
                
                lMap.put("referenceRange", refRange);
                labPregnantCatalogList.add(lMap);
            }
        }
        model.addAttribute("labTestsPregnantCatalogData", labPregnantCatalogList);

        // Lấy danh mục thuốc (Active)
        List<Medication> medicationsCatalog = medicationRepository.findAll().stream()
                .filter(m -> m.getStatus() == null || "Active".equalsIgnoreCase(m.getStatus()))
                .collect(Collectors.toList());
        model.addAttribute("medicationsCatalog", medicationsCatalog);
        
        List<Map<String, Object>> medCatalogList = new ArrayList<>();
        if (medicationsCatalog != null) {
            for (Medication m : medicationsCatalog) {
                Map<String, Object> mMap = new HashMap<>();
                mMap.put("medicationId", m.getMedicationId());
                mMap.put("medicationName", m.getMedicationName());
                mMap.put("form", m.getForm());
                mMap.put("concentration", m.getConcentration());
                mMap.put("route", m.getAdministrationRoute());
                medCatalogList.add(mMap);
            }
        }
        model.addAttribute("medicationsCatalogData", medCatalogList);

        // Lấy danh mục intake timings từ DB
        model.addAttribute("medicationTimings", medicationTimingRepository.findAll());

        // Kiểm tra xem ca khám (Pending hoặc InProgress) có đang diễn ra hay không
        ClinicalExamination activeExam = null;
        if (selectedPatientId != null) {
            activeExam = clinicalExaminationRepository
                    .findFirstByPatient_UserIdAndDoctor_UserIdAndStatusIn(selectedPatientId, doctorId, List.of("Pending", "InProgress"))
                    .orElse(null);
        }
        if (activeExam == null) {
            activeExam = activeExamOpt.orElse(null);
        }

        if (activeExam != null) {
            model.addAttribute("activeExam", activeExam);
            Map<String, Object> examMap = new HashMap<>();
            examMap.put("clinicalExamId", activeExam.getClinicalExamId());
            examMap.put("status", activeExam.getStatus());
            model.addAttribute("activeExamData", examMap);
        }

        // Tải ca khám Completed/Cancelled cuối cùng của bệnh nhân
        List<ClinicalExamination> patientExams = clinicalExaminationService.findByPatientId(selectedPatientId).stream()
                .filter(e -> "Completed".equalsIgnoreCase(e.getStatus()) || "Cancelled".equalsIgnoreCase(e.getStatus()))
                .sorted((e1, e2) -> e2.getExamDate().compareTo(e1.getExamDate()))
                .collect(Collectors.toList());

        boolean viewOnly = "true".equalsIgnoreCase((String) session.getAttribute("examineViewOnly"));
        model.addAttribute("viewOnly", viewOnly);

        if (viewOnly) {
            // Trong chế độ ViewOnly, chúng ta hiển thị ca khám đang chọn qua url (nếu có), nếu không có thì lấy ca khám completed cuối cùng
            ClinicalExamination lastExam = null;
            if (activeExamOpt.isPresent()) {
                lastExam = activeExamOpt.get();
            } else if (!patientExams.isEmpty()) {
                lastExam = patientExams.get(0);
            }

            if (lastExam != null) {
                final String lastExamId = lastExam.getClinicalExamId();
                model.addAttribute("lastExam", lastExam);
                
                Map<String, Object> examMap = new HashMap<>();
                examMap.put("clinicalExamId", lastExamId);
                examMap.put("medicalHistory", lastExam.getMedicalHistory());
                examMap.put("diagnosisNote", lastExam.getDiagnosisNote());
                examMap.put("nextAppointment", lastExam.getNextAppointment() != null ? lastExam.getNextAppointment().toString() : null);
                examMap.put("status", lastExam.getStatus());
                model.addAttribute("lastExamData", examMap);

                // Nạp triệu chứng liên quan dưới dạng Map note cho frontend
                List<ExamSymptom> symptoms = examSymptomRepository.findAll().stream()
                        .filter(s -> s.getId().getClinicalExamId().equals(lastExamId))
                        .collect(Collectors.toList());
                Map<String, String> symptomNotes = new HashMap<>();
                for (ExamSymptom s : symptoms) {
                    symptomNotes.put(s.getSymptom().getSymptomId(), s.getNote() != null ? s.getNote() : "");
                }
                model.addAttribute("chosenSymptomNotes", symptomNotes);

                // Nạp kết quả xét nghiệm liên quan
                List<LabResult> labResults = labResultRepository.findByLabOrder_ClinicalExamination_ClinicalExamId(lastExam.getClinicalExamId());
                model.addAttribute("lastExamLabResults", labResults);

                List<Map<String, Object>> labResultsList = new ArrayList<>();
                for (LabResult r : labResults) {
                    Map<String, Object> rMap = new HashMap<>();
                    Map<String, Object> testMap = new HashMap<>();
                    testMap.put("testName", r.getLabTest().getTestName());
                    testMap.put("unit", r.getLabTest().getUnit());
                    rMap.put("labTest", testMap);
                    rMap.put("referenceRange", r.getReferenceRange());
                    rMap.put("resultValue", r.getResultValue());
                    rMap.put("flag", r.getFlag());
                    labResultsList.add(rMap);
                }
                model.addAttribute("lastExamLabResultsData", labResultsList);

                // Nạp chi tiết đơn thuốc
                Prescription prescription = prescriptionRepository.findByClinicalExamination_ClinicalExamId(lastExam.getClinicalExamId()).orElse(null);
                List<PrescriptionDetail> details = Collections.emptyList();
                if (prescription != null) {
                    details = prescriptionDetailRepository.findByPrescription_PrescriptionId(prescription.getPrescriptionId());
                    model.addAttribute("lastExamPrescriptionDetails", details);
                } else {
                    model.addAttribute("lastExamPrescriptionDetails", Collections.emptyList());
                }

                List<Map<String, Object>> prescList = new ArrayList<>();
                for (PrescriptionDetail p : details) {
                    Map<String, Object> pMap = new HashMap<>();
                    Map<String, Object> medMap = new HashMap<>();
                    medMap.put("medicationId", p.getMedication().getMedicationId());
                    medMap.put("medicationName", p.getMedication().getMedicationName());
                    medMap.put("concentration", p.getMedication().getConcentration());
                    medMap.put("form", p.getMedication().getForm());
                    pMap.put("medication", medMap);
                    pMap.put("dosage", p.getDosage());
                    pMap.put("durationDays", p.getDurationDays());
                    pMap.put("totalQuantity", p.getTotalQuantity());
                    pMap.put("medicationPlan", p.getMedicationPlan());
                    pMap.put("startDate", p.getStartDate() != null ? p.getStartDate().toString() : null);
                    pMap.put("endDate", p.getEndDate() != null ? p.getEndDate().toString() : null);
                    
                    List<Map<String, Object>> timingsList = new ArrayList<>();
                    if (p.getPrescriptionTimings() != null) {
                        for (PrescriptionTiming t : p.getPrescriptionTimings()) {
                            Map<String, Object> tMap = new HashMap<>();
                            Map<String, Object> timingNameMap = new HashMap<>();
                            timingNameMap.put("timingName", t.getTiming().getTimingName());
                            tMap.put("timing", timingNameMap);
                            timingsList.add(tMap);
                        }
                    }
                    pMap.put("prescriptionTimings", timingsList);
                    prescList.add(pMap);
                }
                model.addAttribute("lastExamPrescriptionDetailsData", prescList);
            }
        }
    }

    @PostMapping("/examine/{patientId}/start")
    public String startExam(@PathVariable("patientId") String patientId, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !"DOC".equalsIgnoreCase(loggedInUser.getRole().getRoleId())) {
            return "redirect:/login";
        }

        clinicalExaminationService.startExamination(patientId, loggedInUser.getUserId());
        session.setAttribute("selectedPatientId", patientId);
        session.setAttribute("examineViewOnly", "false");
        return "redirect:/doctor/examine";
    }

    @PostMapping("/examine/{patientId}/cancel")
    public String cancelExam(
            @PathVariable("patientId") String patientId,
            @RequestParam("reason") String reason,
            HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !"DOC".equalsIgnoreCase(loggedInUser.getRole().getRoleId())) {
            return "redirect:/login";
        }

        clinicalExaminationService.cancelExamination(patientId, reason, loggedInUser.getUserId());
        return "redirect:/doctor/dashboard";
    }

    @PostMapping("/examine/{patientId}/submit")
    public String submitExam(
            @PathVariable("patientId") String patientId,
            @Valid @ModelAttribute("examForm") ClinicalExamForm form,
            BindingResult bindingResult,
            HttpSession session,
            Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !"DOC".equalsIgnoreCase(loggedInUser.getRole().getRoleId())) {
            return "redirect:/login";
        }

        // Custom validation check 1: prescription must not be empty
        if (form.getPrescriptionJson() == null || form.getPrescriptionJson().trim().isEmpty() || "[]".equals(form.getPrescriptionJson().trim())) {
            bindingResult.rejectValue("prescriptionJson", "error.prescriptionJson", "Vui lòng kê đơn ít nhất một loại thuốc.");
        }

        // Custom validation check 2: treatment plan must have at least one field filled
        if (ParseUtil.isBlank(form.getTreatmentGoal()) && ParseUtil.isBlank(form.getDietPlan()) &&
            ParseUtil.isBlank(form.getExercisePlan()) && ParseUtil.isBlank(form.getGlucoseMonitoringPlan()) &&
            ParseUtil.isBlank(form.getMedicationPlan())) {
            bindingResult.rejectValue("treatmentGoal", "error.treatmentGoal", "Yêu cầu điền ít nhất 1 trường của Kế hoạch & Phác đồ điều trị.");
        }

        if (bindingResult.hasErrors()) {
            populateExamineModel(patientId, false, session, model, loggedInUser);
            model.addAttribute("validationError", "Có lỗi xảy ra trong dữ liệu nhập vào. Vui lòng kiểm tra lại các trường thông báo đỏ.");
            return "doctor/examine";
        }

        clinicalExaminationService.submitExamination(patientId, form, loggedInUser.getUserId());
        return "redirect:/doctor/dashboard?toast=completed";
    }

    @GetMapping("/examine/edit/{examId}")
    public String editExaminePage(
            @PathVariable("examId") String examId,
            HttpSession session,
            Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !"DOC".equalsIgnoreCase(loggedInUser.getRole().getRoleId())) {
            return "redirect:/login";
        }

        ClinicalExamination exam = clinicalExaminationRepository.findById(examId)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Không tìm thấy ca khám: " + examId));

        if (!exam.getDoctor().getUserId().equalsIgnoreCase(loggedInUser.getUserId())) {
            return "redirect:/doctor/dashboard";
        }

        String patientId = exam.getPatient().getUserId();
        session.setAttribute("selectedPatientId", patientId);
        session.setAttribute("examineViewOnly", "false");

        populateExamineModel(patientId, false, session, model, loggedInUser);

        // Prepopulate ClinicalExamForm with exam data
        ClinicalExamForm form = new ClinicalExamForm();
        form.setMedicalHistory(exam.getMedicalHistory());
        form.setDiagnosisNote(exam.getDiagnosisNote());
        if (exam.getNextAppointment() != null) {
            form.setNextAppointment(exam.getNextAppointment().toLocalDate().toString());
        }
        if (exam.getTreatmentPlan() != null) {
            form.setTreatmentGoal(exam.getTreatmentPlan().getTreatmentGoal());
            form.setDietPlan(exam.getTreatmentPlan().getDietPlan());
            form.setExercisePlan(exam.getTreatmentPlan().getExercisePlan());
            form.setGlucoseMonitoringPlan(exam.getTreatmentPlan().getGlucoseMonitoringPlan());
        }

        // Set symptom checkboxed list
        List<ExamSymptom> symptoms = examSymptomRepository.findAll().stream()
                .filter(s -> s.getId().getClinicalExamId().equals(examId))
                .collect(Collectors.toList());
        List<String> symptomIds = symptoms.stream()
                .map(s -> s.getSymptom().getSymptomId())
                .collect(Collectors.toList());
        form.setSymptomIds(symptomIds);

        Map<String, String> symptomNotes = new HashMap<>();
        for (ExamSymptom s : symptoms) {
            symptomNotes.put(s.getSymptom().getSymptomId(), s.getNote() != null ? s.getNote() : "");
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            form.setSymptomCommentsJson(mapper.writeValueAsString(symptomNotes));
        } catch (Exception e) {
            // Ignore
        }

        // Load pregnancy flag if any lab results mapped under "Pregnant" type
        boolean isPregnant = false;
        List<LabResult> results = labResultRepository.findByLabOrder_ClinicalExamination_ClinicalExamId(examId);
        if (!results.isEmpty()) {
            // If any test threshold matches Pregnant, check pregnancy checkbox
            Optional<PatientType> pregTypeOpt = patientTypeRepository.findAll().stream()
                    .filter(t -> t.getTypeName().equalsIgnoreCase("Pregnant"))
                    .findFirst();
            if (pregTypeOpt.isPresent()) {
                for (LabResult res : results) {
                    String ref = res.getReferenceRange();
                    if (ref != null) {
                        isPregnant = true;
                        break;
                    }
                }
            }
        }
        form.setIsPregnant(isPregnant);

        model.addAttribute("examForm", form);
        model.addAttribute("isEditMode", true);
        model.addAttribute("lastExam", exam);

        Map<String, Object> examMap = new HashMap<>();
        examMap.put("clinicalExamId", examId);
        examMap.put("medicalHistory", exam.getMedicalHistory());
        examMap.put("diagnosisNote", exam.getDiagnosisNote());
        examMap.put("nextAppointment", exam.getNextAppointment() != null ? exam.getNextAppointment().toString() : null);
        examMap.put("status", exam.getStatus());
        if (exam.getTreatmentPlan() != null) {
            Map<String, Object> planMap = new HashMap<>();
            planMap.put("treatmentGoal", exam.getTreatmentPlan().getTreatmentGoal());
            planMap.put("dietPlan", exam.getTreatmentPlan().getDietPlan());
            planMap.put("exercisePlan", exam.getTreatmentPlan().getExercisePlan());
            planMap.put("glucoseMonitoringPlan", exam.getTreatmentPlan().getGlucoseMonitoringPlan());
            examMap.put("treatmentPlan", planMap);
        }
        model.addAttribute("lastExamData", examMap);

        // Put chosenSymptomNotes to model so JS can populate selectedSymptoms
        model.addAttribute("chosenSymptomNotes", symptomNotes);

        // Load results to populate simulatedResults in JS
        if (!results.isEmpty()) {
            model.addAttribute("lastExamLabResults", results);
            
            List<Map<String, Object>> resultsList = new ArrayList<>();
            for (LabResult r : results) {
                Map<String, Object> rMap = new HashMap<>();
                Map<String, Object> testMap = new HashMap<>();
                testMap.put("testId", r.getLabTest().getLabTestId());
                testMap.put("testName", r.getLabTest().getTestName());
                testMap.put("unit", r.getLabTest().getUnit());
                
                rMap.put("labTest", testMap);
                rMap.put("testId", r.getLabTest().getLabTestId());
                rMap.put("referenceRange", r.getReferenceRange());
                rMap.put("resultValue", r.getResultValue());
                rMap.put("val", r.getResultValue());
                rMap.put("flag", r.getFlag());
                resultsList.add(rMap);
            }
            try {
                ObjectMapper mapper = new ObjectMapper();
                model.addAttribute("lastExamLabResultsData", resultsList);
                form.setLabResultsJson(mapper.writeValueAsString(resultsList));
            } catch (Exception e) {
                // Ignore
            }
        }

        // Put drug details to model
        Prescription prescription = prescriptionRepository.findByClinicalExamination_ClinicalExamId(examId).orElse(null);
        List<PrescriptionDetail> details = Collections.emptyList();
        if (prescription != null) {
            details = prescriptionDetailRepository.findByPrescription_PrescriptionId(prescription.getPrescriptionId());
            model.addAttribute("lastExamPrescriptionDetails", details);
            
            List<Map<String, Object>> prescList = new ArrayList<>();
            List<Map<String, Object>> flatPrescList = new ArrayList<>();
            for (PrescriptionDetail p : details) {
                Map<String, Object> pMap = new HashMap<>();
                Map<String, Object> medMap = new HashMap<>();
                medMap.put("medicationId", p.getMedication().getMedicationId());
                medMap.put("medicationName", p.getMedication().getMedicationName());
                medMap.put("concentration", p.getMedication().getConcentration());
                medMap.put("form", p.getMedication().getForm());
                
                pMap.put("medication", medMap);
                pMap.put("dosage", p.getDosage());
                pMap.put("durationDays", p.getDurationDays());
                pMap.put("totalQuantity", p.getTotalQuantity());
                pMap.put("medicationPlan", p.getMedicationPlan());
                pMap.put("startDate", p.getStartDate() != null ? p.getStartDate().toString() : "");
                pMap.put("endDate", p.getEndDate() != null ? p.getEndDate().toString() : "");
                
                List<Map<String, Object>> timingsList = new ArrayList<>();
                List<String> flatTimings = new ArrayList<>();
                if (p.getPrescriptionTimings() != null) {
                    for (PrescriptionTiming t : p.getPrescriptionTimings()) {
                        Map<String, Object> tMap = new HashMap<>();
                        Map<String, Object> timingNameMap = new HashMap<>();
                        timingNameMap.put("timingName", t.getTiming().getTimingName());
                        tMap.put("timing", timingNameMap);
                        timingsList.add(tMap);
                        flatTimings.add(t.getTiming().getTimingName());
                    }
                }
                pMap.put("prescriptionTimings", timingsList);
                prescList.add(pMap);

                // Flat mapping for form action
                Map<String, Object> flatLine = new HashMap<>();
                flatLine.put("medId", p.getMedication().getMedicationId());
                flatLine.put("name", p.getMedication().getMedicationName());
                flatLine.put("concentration", p.getMedication().getConcentration());
                flatLine.put("form", p.getMedication().getForm());
                flatLine.put("dosage", p.getDosage());
                flatLine.put("duration", p.getDurationDays());
                flatLine.put("quantity", p.getTotalQuantity());
                flatLine.put("medicationPlan", p.getMedicationPlan());
                flatLine.put("startDate", p.getStartDate() != null ? p.getStartDate().toString() : "");
                flatLine.put("endDate", p.getEndDate() != null ? p.getEndDate().toString() : "");
                flatLine.put("timing", flatTimings);
                flatLine.put("timingText", String.join(", ", flatTimings));
                flatPrescList.add(flatLine);
            }
            model.addAttribute("lastExamPrescriptionDetailsData", prescList);
            try {
                ObjectMapper mapper = new ObjectMapper();
                form.setPrescriptionJson(mapper.writeValueAsString(flatPrescList));
            } catch (Exception e) {
                // Ignore
            }
        } else {
            model.addAttribute("lastExamPrescriptionDetails", Collections.emptyList());
            model.addAttribute("lastExamPrescriptionDetailsData", Collections.emptyList());
        }

        return "doctor/examine";
    }

    @PostMapping("/examine/update/{examId}")
    public String updateExaminationForm(
            @PathVariable("examId") String examId,
            @Valid @ModelAttribute("examForm") ClinicalExamForm form,
            BindingResult bindingResult,
            HttpSession session,
            Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !"DOC".equalsIgnoreCase(loggedInUser.getRole().getRoleId())) {
            return "redirect:/login";
        }

        // Custom validation check 1: prescription must not be empty
        if (form.getPrescriptionJson() == null || form.getPrescriptionJson().trim().isEmpty() || "[]".equals(form.getPrescriptionJson().trim())) {
            bindingResult.rejectValue("prescriptionJson", "error.prescriptionJson", "Vui lòng kê đơn ít nhất một loại thuốc.");
        }

        // Custom validation check 2: treatment plan must have at least one field filled
        if (ParseUtil.isBlank(form.getTreatmentGoal()) && ParseUtil.isBlank(form.getDietPlan()) &&
            ParseUtil.isBlank(form.getExercisePlan()) && ParseUtil.isBlank(form.getGlucoseMonitoringPlan()) &&
            ParseUtil.isBlank(form.getMedicationPlan())) {
            bindingResult.rejectValue("treatmentGoal", "error.treatmentGoal", "Yêu cầu điền ít nhất 1 trường của Kế hoạch & Phác đồ điều trị.");
        }

        ClinicalExamination exam = clinicalExaminationRepository.findById(examId)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Không tìm thấy ca khám: " + examId));

        if (bindingResult.hasErrors()) {
            String patientId = exam.getPatient().getUserId();
            populateExamineModel(patientId, false, session, model, loggedInUser);
            model.addAttribute("isEditMode", true);
            model.addAttribute("lastExam", exam);
            model.addAttribute("validationError", "Có lỗi xảy ra trong dữ liệu nhập vào. Vui lòng kiểm tra lại các trường thông báo đỏ.");

            // Re-populate all required lists to prevent UI crashing on validation failure
            List<ExamSymptom> symptoms = examSymptomRepository.findAll().stream()
                    .filter(s -> s.getId().getClinicalExamId().equals(examId))
                    .collect(Collectors.toList());
            Map<String, String> symptomNotes = new HashMap<>();
            for (ExamSymptom s : symptoms) {
                symptomNotes.put(s.getSymptom().getSymptomId(), s.getNote() != null ? s.getNote() : "");
            }
            model.addAttribute("chosenSymptomNotes", symptomNotes);

            List<LabResult> results = labResultRepository.findByLabOrder_ClinicalExamination_ClinicalExamId(examId);
            if (!results.isEmpty()) {
                model.addAttribute("lastExamLabResults", results);
                List<Map<String, Object>> resultsList = new ArrayList<>();
                for (LabResult r : results) {
                    Map<String, Object> rMap = new HashMap<>();
                    Map<String, Object> testMap = new HashMap<>();
                    testMap.put("testId", r.getLabTest().getLabTestId());
                    testMap.put("testName", r.getLabTest().getTestName());
                    testMap.put("unit", r.getLabTest().getUnit());
                    rMap.put("labTest", testMap);
                    rMap.put("testId", r.getLabTest().getLabTestId());
                    rMap.put("referenceRange", r.getReferenceRange());
                    rMap.put("resultValue", r.getResultValue());
                    rMap.put("val", r.getResultValue());
                    rMap.put("flag", r.getFlag());
                    resultsList.add(rMap);
                }
                model.addAttribute("lastExamLabResultsData", resultsList);
            }

            Prescription prescription = prescriptionRepository.findByClinicalExamination_ClinicalExamId(examId).orElse(null);
            List<PrescriptionDetail> details = Collections.emptyList();
            if (prescription != null) {
                details = prescriptionDetailRepository.findByPrescription_PrescriptionId(prescription.getPrescriptionId());
                model.addAttribute("lastExamPrescriptionDetails", details);
                List<Map<String, Object>> prescList = new ArrayList<>();
                for (PrescriptionDetail p : details) {
                    Map<String, Object> pMap = new HashMap<>();
                    Map<String, Object> medMap = new HashMap<>();
                    medMap.put("medicationId", p.getMedication().getMedicationId());
                    medMap.put("medicationName", p.getMedication().getMedicationName());
                    medMap.put("concentration", p.getMedication().getConcentration());
                    medMap.put("form", p.getMedication().getForm());
                    pMap.put("medication", medMap);
                    pMap.put("dosage", p.getDosage());
                    pMap.put("durationDays", p.getDurationDays());
                    pMap.put("totalQuantity", p.getTotalQuantity());
                    pMap.put("medicationPlan", p.getMedicationPlan());
                    pMap.put("startDate", p.getStartDate() != null ? p.getStartDate().toString() : "");
                    pMap.put("endDate", p.getEndDate() != null ? p.getEndDate().toString() : "");
                    List<Map<String, Object>> timingsList = new ArrayList<>();
                    if (p.getPrescriptionTimings() != null) {
                        for (PrescriptionTiming t : p.getPrescriptionTimings()) {
                            Map<String, Object> tMap = new HashMap<>();
                            Map<String, Object> timingNameMap = new HashMap<>();
                            timingNameMap.put("timingName", t.getTiming().getTimingName());
                            tMap.put("timing", timingNameMap);
                            timingsList.add(tMap);
                        }
                    }
                    pMap.put("prescriptionTimings", timingsList);
                    prescList.add(pMap);
                }
                model.addAttribute("lastExamPrescriptionDetailsData", prescList);
            } else {
                model.addAttribute("lastExamPrescriptionDetails", Collections.emptyList());
                model.addAttribute("lastExamPrescriptionDetailsData", Collections.emptyList());
            }

            Map<String, Object> examMap = new HashMap<>();
            examMap.put("clinicalExamId", examId);
            examMap.put("medicalHistory", exam.getMedicalHistory());
            examMap.put("diagnosisNote", exam.getDiagnosisNote());
            examMap.put("nextAppointment", exam.getNextAppointment() != null ? exam.getNextAppointment().toString() : null);
            examMap.put("status", exam.getStatus());
            if (exam.getTreatmentPlan() != null) {
                Map<String, Object> planMap = new HashMap<>();
                planMap.put("treatmentGoal", exam.getTreatmentPlan().getTreatmentGoal());
                planMap.put("dietPlan", exam.getTreatmentPlan().getDietPlan());
                planMap.put("exercisePlan", exam.getTreatmentPlan().getExercisePlan());
                planMap.put("glucoseMonitoringPlan", exam.getTreatmentPlan().getGlucoseMonitoringPlan());
                examMap.put("treatmentPlan", planMap);
            }
            model.addAttribute("lastExamData", examMap);

            return "doctor/examine";
        }

        clinicalExaminationService.updateExamination(examId, form);

        return "redirect:/doctor/dashboard?toast=updated";
    }
}
