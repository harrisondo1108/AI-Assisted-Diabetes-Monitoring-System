package com.quan.diabetes.controller.doctor;

import com.quan.diabetes.dto.ClinicalExamForm;
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
            return "redirect:/doctor/dashboard";
        }

        Patient patient = patientService.findById(selectedPatientId).orElse(null);
        if (patient == null) {
            return "redirect:/doctor/dashboard";
        }
        model.addAttribute("patient", patient);
        model.addAttribute("examForm", new ClinicalExamForm());

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
                .filter(t -> (t.getMinAge() == null || finalAge >= t.getMinAge()) && (t.getMaxAge() == null || finalAge <= t.getMaxAge()))
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
                
                String refRange = "0 - 5.2";
                if (thresholdOpt.isPresent()) {
                    refRange = thresholdOpt.get().getMinValue() + " - " + thresholdOpt.get().getMaxValue();
                    lMap.put("minValue", thresholdOpt.get().getMinValue());
                    lMap.put("maxValue", thresholdOpt.get().getMaxValue());
                } else {
                    String testName = l.getTestName().toLowerCase();
                    String tId = l.getLabTestId();
                    if (tId.equals("LAB003") || testName.contains("ogtt") || testName.contains("dung nạp")) {
                        refRange = "70 - 140";
                    } else if (tId.equals("LAB001") || testName.contains("fpg") || testName.contains("lúc đói") || testName.contains("fasting")) {
                        refRange = "70 - 100";
                    } else if (tId.equals("LAB002") || testName.contains("hba1c")) {
                        refRange = "4.0 - 5.6";
                    } else if (tId.equals("LAB004") || testName.contains("ngẫu nhiên") || testName.contains("random")) {
                        refRange = "70 - 140";
                    }
                    lMap.put("minValue", null);
                    lMap.put("maxValue", null);
                }
                
                lMap.put("referenceRange", refRange);
                labCatalogList.add(lMap);
            }
        }
        model.addAttribute("labTestsCatalogData", labCatalogList);

        // Lấy danh mục thuốc (Active) cho ô tìm kiếm autocomplete
        List<Medication> medications = medicationRepository.findAllActiveList();
        model.addAttribute("medications", medications);

        // Lấy danh mục intake timings từ DB
        model.addAttribute("medicationTimings", medicationTimingRepository.findAll());

        // Kiểm tra xem ca khám có đang ở trạng thái Pending hoặc InProgress hay không
        ClinicalExamination activeExam = clinicalExaminationRepository
                .findFirstByPatient_UserIdAndDoctor_UserIdAndStatusIn(
                        selectedPatientId, doctorId, List.of("Pending", "InProgress"))
                .orElse(null);
        model.addAttribute("activeExam", activeExam);

        Map<String, Object> activeExamMap = null;
        if (activeExam != null) {
            activeExamMap = new HashMap<>();
            activeExamMap.put("clinicalExamId", activeExam.getClinicalExamId());
            activeExamMap.put("status", activeExam.getStatus());
        }
        model.addAttribute("activeExamData", activeExamMap);

        boolean viewOnly = "true".equals(session.getAttribute("examineViewOnly"));
        model.addAttribute("viewOnly", viewOnly);

        // Nếu là chế độ chỉ xem, nạp thông tin ca khám cũ
        if (viewOnly) {
            // Lấy ca khám Completed/Cancelled gần nhất
            ClinicalExamination lastExam = clinicalExaminationRepository.findByPatient_UserIdOrderByExamDateDesc(selectedPatientId).stream()
                    .filter(e -> "Completed".equalsIgnoreCase(e.getStatus()) || "Cancelled".equalsIgnoreCase(e.getStatus()))
                    .findFirst()
                    .orElse(null);

            if (lastExam != null) {
                model.addAttribute("lastExam", lastExam);

                Map<String, Object> lastExamMap = new HashMap<>();
                lastExamMap.put("clinicalExamId", lastExam.getClinicalExamId());
                lastExamMap.put("diagnosisNote", lastExam.getDiagnosisNote());
                lastExamMap.put("medicalHistory", lastExam.getMedicalHistory());
                lastExamMap.put("nextAppointment", lastExam.getNextAppointment() != null ? lastExam.getNextAppointment().toString() : null);
                
                if (lastExam.getTreatmentPlan() != null) {
                    Map<String, Object> planMap = new HashMap<>();
                    planMap.put("treatmentGoal", lastExam.getTreatmentPlan().getTreatmentGoal());
                    planMap.put("dietPlan", lastExam.getTreatmentPlan().getDietPlan());
                    planMap.put("exercisePlan", lastExam.getTreatmentPlan().getExercisePlan());
                    planMap.put("glucoseMonitoringPlan", lastExam.getTreatmentPlan().getGlucoseMonitoringPlan());
                    planMap.put("medicationPlan", "");
                    lastExamMap.put("treatmentPlan", planMap);
                }
                model.addAttribute("lastExamData", lastExamMap);

                // Nạp triệu chứng đã khám
                List<ExamSymptom> chosenSymptoms = examSymptomRepository.findAll().stream()
                        .filter(s -> lastExam.getClinicalExamId().equals(s.getId().getClinicalExamId()))
                        .collect(Collectors.toList());
                List<String> chosenSymptomIds = chosenSymptoms.stream()
                        .map(s -> s.getSymptom().getSymptomId())
                        .collect(Collectors.toList());
                model.addAttribute("chosenSymptomIds", chosenSymptomIds);

                Map<String, String> chosenSymptomNotes = new HashMap<>();
                for (ExamSymptom s : chosenSymptoms) {
                    chosenSymptomNotes.put(s.getSymptom().getSymptomId(), s.getNote() != null ? s.getNote() : "");
                }
                model.addAttribute("chosenSymptomNotes", chosenSymptomNotes);

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

        return "doctor/examine";
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
            @ModelAttribute("examForm") ClinicalExamForm form,
            HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !"DOC".equalsIgnoreCase(loggedInUser.getRole().getRoleId())) {
            return "redirect:/login";
        }

        clinicalExaminationService.submitExamination(patientId, form, loggedInUser.getUserId());
        return "redirect:/doctor/dashboard";
    }
}
