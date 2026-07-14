package com.quan.diabetes.controller.doctor;

import com.quan.diabetes.dto.doctor.ClinicalExamForm;
import com.quan.diabetes.entity.*;
import com.quan.diabetes.repository.*;
import com.quan.diabetes.service.exam.ClinicalExaminationService;
import com.quan.diabetes.service.user.PatientRoutineService;
import com.quan.diabetes.service.user.PatientService;
import com.quan.diabetes.service.user.ProfileService;
import com.quan.diabetes.service.systemlog.SystemLogService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
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
    private final LabOrderRepository labOrderRepository;
    private final PatientTypeRepository patientTypeRepository;
    private final IndicatorThresholdRepository indicatorThresholdRepository;

    @Autowired
    private SystemLogService systemLogService;

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
            IndicatorThresholdRepository indicatorThresholdRepository,
            LabOrderRepository labOrderRepository) {
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
        this.labOrderRepository = labOrderRepository;
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

        ClinicalExamForm form = new ClinicalExamForm();
        String selectedPatientId = (String) session.getAttribute("selectedPatientId");
        if (selectedPatientId != null) {
            String doctorId = loggedInUser.getUserId();
            Optional<ClinicalExamination> activeOpt = clinicalExaminationRepository
                    .findFirstByPatient_UserIdAndDoctor_UserIdAndStatusIn(selectedPatientId, doctorId,
                            List.of("Pending", "InProgress"));
            if (activeOpt.isPresent()) {
                ClinicalExamination exam = activeOpt.get();
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

                // Load active symptoms details into form
                List<ExamSymptom> symptoms = examSymptomRepository.findAll().stream()
                        .filter(s -> s.getId().getClinicalExamId().equals(exam.getClinicalExamId()))
                        .collect(Collectors.toList());
                List<String> symptomIds = symptoms.stream()
                        .map(s -> s.getSymptom().getSymptomId())
                        .collect(Collectors.toList());
                form.setSymptomIds(symptomIds);

                Map<String, String> symptomNotes = new HashMap<>();
                for (ExamSymptom s : symptoms) {
                    symptomNotes.put(s.getSymptom().getSymptomId(), s.getNote() != null ? s.getNote() : "");
                }
                form.setSymptomComments(symptomNotes);

                // Determine pregnancy status based on indicator thresholds
                boolean isPregnant = false;
                Patient patient = exam.getPatient();
                if (patient != null && Boolean.TRUE.equals(patient.getGender())) {
                    List<LabResult> results = labResultRepository
                            .findByLabOrder_ClinicalExamination_ClinicalExamId(exam.getClinicalExamId());
                    if (!results.isEmpty()) {
                        Optional<PatientType> pregTypeOpt = patientTypeRepository.findAll().stream()
                                .filter(t -> t.getTypeName().equalsIgnoreCase("Pregnant"))
                                .findFirst();
                        int age = 0;
                        if (patient.getDob() != null) {
                            age = java.time.Period.between(patient.getDob(), java.time.LocalDate.now()).getYears();
                        }
                        final int finalAge = age;
                        Optional<PatientType> normalTypeOpt = patientTypeRepository.findAll().stream()
                                .filter(t -> t.getMinAge() != null && t.getMaxAge() != null && finalAge >= t.getMinAge()
                                        && finalAge <= t.getMaxAge())
                                .findFirst();

                        if (pregTypeOpt.isPresent()) {
                            Integer pregTypeId = pregTypeOpt.get().getPatientTypeId();
                            Integer normalTypeId = normalTypeOpt.map(PatientType::getPatientTypeId).orElse(null);
                            for (LabResult res : results) {
                                Optional<IndicatorThreshold> pregThresholdOpt = indicatorThresholdRepository
                                        .findByLabTest_LabTestIdAndPatientType_PatientTypeId(
                                                res.getLabTest().getLabTestId(), pregTypeId);
                                if (pregThresholdOpt.isPresent()) {
                                    String pregRange = pregThresholdOpt.get().getMinValue() + " - "
                                            + pregThresholdOpt.get().getMaxValue();
                                    if (pregRange.equals(res.getReferenceRange())) {
                                        boolean distinct = true;
                                        if (normalTypeId != null) {
                                            Optional<IndicatorThreshold> normalThresholdOpt = indicatorThresholdRepository
                                                    .findByLabTest_LabTestIdAndPatientType_PatientTypeId(
                                                            res.getLabTest().getLabTestId(), normalTypeId);
                                            if (normalThresholdOpt.isPresent()) {
                                                String normalRange = normalThresholdOpt.get().getMinValue() + " - "
                                                        + normalThresholdOpt.get().getMaxValue();
                                                if (normalRange.equals(pregRange)) {
                                                    distinct = false;
                                                }
                                            }
                                        }
                                        if (distinct) {
                                            isPregnant = true;
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                form.setIsPregnant(isPregnant);
            }
        }
        model.addAttribute("examForm", form);

        return "doctor/examine";
    }

    private void populateExamineModel(String patientId, Boolean viewOnlyParam, HttpSession session, Model model,
            User loggedInUser) {
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
        model.addAttribute("lastExamPrescriptionDetailsData", Collections.emptyList());
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
        patientMap.put("imageUrl", patient.getImageUrl());
        model.addAttribute("patientData", patientMap);

        // Lấy thông tin Routine của bệnh nhân
        PatientRoutine routine = patientRoutineService.findById(selectedPatientId).orElse(null);
        model.addAttribute("routine", routine);
        Map<String, Object> routineMap = null;
        if (routine != null) {
            routineMap = new HashMap<>();
            routineMap.put("breakfastTime",
                    routine.getBreakfastTime() != null ? routine.getBreakfastTime().toString() : null);
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
                .filter(t -> t.getMinAge() != null && t.getMaxAge() != null && finalAge >= t.getMinAge()
                        && finalAge <= t.getMaxAge())
                .findFirst()
                .orElse(null);



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
                    .findFirstByPatient_UserIdAndDoctor_UserIdAndStatusIn(selectedPatientId, doctorId,
                            List.of("Pending", "InProgress"))
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

        ClinicalExamination lastExam = null;
        if (viewOnly) {
            // Trong chế độ ViewOnly, chúng ta hiển thị ca khám đang chọn qua url (nếu có),
            // nếu không có thì lấy ca khám completed cuối cùng
            if (activeExamOpt.isPresent()) {
                lastExam = activeExamOpt.get();
            } else if (!patientExams.isEmpty()) {
                lastExam = patientExams.get(0);
            }
        } else if (activeExam != null && "InProgress".equalsIgnoreCase(activeExam.getStatus())) {
            // Nếu không phải viewOnly và đang có ca khám dở dang (InProgress), nạp toàn bộ
            // dữ liệu lưu nháp của ca khám đó từ DB lên
            lastExam = activeExam;
        }

        if (lastExam != null) {
            final String lastExamId = lastExam.getClinicalExamId();
            model.addAttribute("lastExam", lastExam);

            Map<String, Object> examMap = new HashMap<>();
            examMap.put("clinicalExamId", lastExamId);
            examMap.put("medicalHistory", lastExam.getMedicalHistory());
            examMap.put("diagnosisNote", lastExam.getDiagnosisNote());
            examMap.put("nextAppointment",
                    lastExam.getNextAppointment() != null ? lastExam.getNextAppointment().toString() : null);
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
            List<LabResult> labResults = labResultRepository
                    .findByLabOrder_ClinicalExamination_ClinicalExamId(lastExam.getClinicalExamId());
            model.addAttribute("lastExamLabResults", labResults);
            model.addAttribute("labResults", labResults);



            // Nạp chi tiết đơn thuốc
            Prescription prescription = prescriptionRepository
                    .findByClinicalExamination_ClinicalExamId(lastExam.getClinicalExamId()).orElse(null);
            List<PrescriptionDetail> details = Collections.emptyList();
            if (prescription != null) {
                details = prescriptionDetailRepository
                        .findByPrescription_PrescriptionId(prescription.getPrescriptionId());
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
        return "redirect:/doctor/examine";
    }

    @PostMapping("/examine/{examId}/order-labs")
    @org.springframework.transaction.annotation.Transactional
    public String orderLabs(
            @PathVariable("examId") String examId,
            @RequestParam(value = "isPregnant", required = false) Boolean isPregnant,
            HttpSession session,
            Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !"DOC".equalsIgnoreCase(loggedInUser.getRole().getRoleId())) {
            return "redirect:/login";
        }

        ClinicalExamination exam = clinicalExaminationRepository.findById(examId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy ca khám: " + examId));

        // Delete any existing LabOrder and LabResults for this exam
        Optional<LabOrder> existingOrderOpt = labOrderRepository.findByClinicalExamination_ClinicalExamId(examId);
        if (existingOrderOpt.isPresent()) {
            LabOrder existingOrder = existingOrderOpt.get();
            List<LabResult> existingResults = labResultRepository.findByLabOrder_ClinicalExamination_ClinicalExamId(examId);
            labResultRepository.deleteAll(existingResults);
            labOrderRepository.delete(existingOrder);
        }

        // Create new LabOrder
        LabOrder labOrder = new LabOrder();
        labOrder.setLabOrderId("LBO-" + System.currentTimeMillis() + "-" + new java.util.Random().nextInt(1000));
        labOrder.setClinicalExamination(exam);
        labOrder.setStatus("Completed");
        labOrder = labOrderRepository.save(labOrder);

        // Find patient type for reference range thresholds
        Patient patient = exam.getPatient();
        PatientType matchedType = null;
        if (Boolean.TRUE.equals(isPregnant) && patient != null && Boolean.TRUE.equals(patient.getGender())) {
            matchedType = patientTypeRepository.findAll().stream()
                    .filter(t -> t.getTypeName().equalsIgnoreCase("Pregnant"))
                    .findFirst()
                    .orElse(null);
        } else {
            int age = 0;
            if (patient != null && patient.getDob() != null) {
                age = java.time.Period.between(patient.getDob(), java.time.LocalDate.now()).getYears();
            }
            final int finalAge = age;
            matchedType = patientTypeRepository.findAll().stream()
                    .filter(t -> t.getMinAge() != null && t.getMaxAge() != null && finalAge >= t.getMinAge()
                            && finalAge <= t.getMaxAge())
                    .findFirst()
                    .orElse(null);
        }

        List<LabTestCatalog> testCatalog = labTestCatalogRepository.findAll().stream()
                .filter(l -> l.getStatus() == null || l.getStatus())
                .collect(Collectors.toList());

        List<LabResult> savedResults = new ArrayList<>();
        java.util.Random random = new java.util.Random();

        for (LabTestCatalog test : testCatalog) {
            LabResult result = new LabResult();
            result.setLabResultId("LBR-" + System.currentTimeMillis() + "-" + random.nextInt(1000));
            result.setLabOrder(labOrder);
            result.setLabTest(test);
            result.setStatus("Completed");

            // Fetch threshold
            Optional<IndicatorThreshold> thresholdOpt = Optional.empty();
            if (matchedType != null) {
                thresholdOpt = indicatorThresholdRepository.findByLabTest_LabTestIdAndPatientType_PatientTypeId(
                        test.getLabTestId(), matchedType.getPatientTypeId());
            }

            String range = "N/A";
            java.math.BigDecimal dbMin = null;
            java.math.BigDecimal dbMax = null;

            if (thresholdOpt.isPresent()) {
                dbMin = thresholdOpt.get().getMinValue();
                dbMax = thresholdOpt.get().getMaxValue();
                range = dbMin + " - " + dbMax;
            }

            java.math.BigDecimal value = new java.math.BigDecimal("0.0");
            String flag = "NORMAL";

            if (dbMin != null && dbMax != null) {
                double minVal = dbMin.doubleValue();
                double maxVal = dbMax.doubleValue();
                double randType = Math.random();
                double simulatedVal;

                if (randType < 0.30) { // 30% chance of LOW
                    simulatedVal = minVal - 0.5 - Math.random() * (minVal * 0.15);
                    if (simulatedVal < 0) simulatedVal = 0;
                } else if (randType > 0.60) { // 40% chance of HIGH
                    double scale = maxVal > 20 ? (maxVal * 0.3) : 4.0;
                    simulatedVal = maxVal + 0.1 + Math.random() * scale;
                } else { // 30% chance of NORMAL
                    simulatedVal = minVal + Math.random() * (maxVal - minVal);
                }

                // Round to 1 decimal place
                simulatedVal = Math.round(simulatedVal * 10.0) / 10.0;
                value = java.math.BigDecimal.valueOf(simulatedVal);

                if (value.compareTo(dbMin) < 0) {
                    flag = "LOW";
                } else if (value.compareTo(dbMax) > 0) {
                    flag = "HIGH";
                } else {
                    flag = "NORMAL";
                }
            }

            result.setResultValue(value);
            result.setReferenceRange(range);
            result.setFlag(flag);
            savedResults.add(labResultRepository.save(result));
        }

        model.addAttribute("labResults", savedResults);
        return "doctor/examine :: labResultsTable";
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

        try {
            clinicalExaminationService.cancelExamination(patientId, reason, loggedInUser.getUserId());
        } catch (Exception e) {
            systemLogService.saveLog(loggedInUser.getUserId(), "REJECT_MEDICAL_RECORD", "MedicalRecord", null, "Bác sĩ từ chối/hủy bệnh án thất bại: " + e.getMessage(), null, null, "FAILED");
            throw e;
        }
        return "redirect:/doctor/queue";
    }

    @PostMapping("/examine/{patientId}/draft")
    public String saveDraft(
            @PathVariable("patientId") String patientId,
            @ModelAttribute("examForm") ClinicalExamForm form,
            HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !"DOC".equalsIgnoreCase(loggedInUser.getRole().getRoleId())) {
            return "redirect:/login";
        }

        clinicalExaminationService.saveDraft(patientId, form, loggedInUser.getUserId());
        return "redirect:/doctor/history?patientId=" + patientId + "&from=examine";
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

        // Custom validation check 1: prescription must not be empty (Commented out to
        // allow submitting without prescription)
        // if (form.getPrescriptionJson() == null ||
        // form.getPrescriptionJson().trim().isEmpty() ||
        // "[]".equals(form.getPrescriptionJson().trim())) {
        // bindingResult.rejectValue("prescriptionJson", "error.prescriptionJson", "Vui
        // lòng kê đơn ít nhất một loại thuốc.");
        // }

        // Custom validation check 2: treatment plan must have at least one field filled
        // if (ParseUtil.isBlank(form.getTreatmentGoal()) &&
        // ParseUtil.isBlank(form.getDietPlan()) &&
        // ParseUtil.isBlank(form.getExercisePlan()) &&
        // ParseUtil.isBlank(form.getGlucoseMonitoringPlan()) &&
        // ParseUtil.isBlank(form.getMedicationPlan())) {
        // bindingResult.rejectValue("treatmentGoal", "error.treatmentGoal", "Yêu cầu
        // điền ít nhất 1 trường của Kế hoạch & Phác đồ điều trị.");
        // }

        if (bindingResult.hasErrors()) {
            populateExamineModel(patientId, false, session, model, loggedInUser);
            model.addAttribute("validationError",
                    "Có lỗi xảy ra trong dữ liệu nhập vào. Vui lòng kiểm tra lại các trường thông báo đỏ.");
            return "doctor/examine";
        }

        try {
            clinicalExaminationService.submitExamination(patientId, form, loggedInUser.getUserId());
        } catch (Exception e) {
            systemLogService.saveLog(loggedInUser.getUserId(), "COMPLETE_MEDICAL_RECORD", "MedicalRecord", null, "Hoàn tất bệnh án thất bại: " + e.getMessage(), null, null, "FAILED");
            throw e;
        }
        return "redirect:/doctor/queue?toast=completed";
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
                .orElseThrow(
                        () -> new jakarta.persistence.EntityNotFoundException("Không tìm thấy ca khám: " + examId));

        if (!exam.getDoctor().getUserId().equalsIgnoreCase(loggedInUser.getUserId())) {
            return "redirect:/doctor/queue";
        }

        if (exam.getExamDate() != null) {
            java.time.LocalDate examLocalDate = exam.getExamDate().toLocalDate();
            java.time.LocalDate today = java.time.LocalDate.now();
            if (!examLocalDate.equals(today)) {
                return "redirect:/doctor/queue?toast=not_today";
            }
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
        form.setSymptomComments(symptomNotes);

        // Load pregnancy flag if any lab results mapped under "Pregnant" type
        boolean isPregnant = false;
        List<LabResult> results = labResultRepository.findByLabOrder_ClinicalExamination_ClinicalExamId(examId);
        Patient patient = exam.getPatient();
        if (patient != null && Boolean.TRUE.equals(patient.getGender())) {
            if (!results.isEmpty()) {
                Optional<PatientType> pregTypeOpt = patientTypeRepository.findAll().stream()
                        .filter(t -> t.getTypeName().equalsIgnoreCase("Pregnant"))
                        .findFirst();
                int age = 0;
                if (patient.getDob() != null) {
                    age = java.time.Period.between(patient.getDob(), java.time.LocalDate.now()).getYears();
                }
                final int finalAge = age;
                Optional<PatientType> normalTypeOpt = patientTypeRepository.findAll().stream()
                        .filter(t -> t.getMinAge() != null && t.getMaxAge() != null && finalAge >= t.getMinAge()
                                && finalAge <= t.getMaxAge())
                        .findFirst();

                if (pregTypeOpt.isPresent()) {
                    Integer pregTypeId = pregTypeOpt.get().getPatientTypeId();
                    Integer normalTypeId = normalTypeOpt.map(PatientType::getPatientTypeId).orElse(null);
                    for (LabResult res : results) {
                        Optional<IndicatorThreshold> pregThresholdOpt = indicatorThresholdRepository
                                .findByLabTest_LabTestIdAndPatientType_PatientTypeId(res.getLabTest().getLabTestId(),
                                        pregTypeId);
                        if (pregThresholdOpt.isPresent()) {
                            String pregRange = pregThresholdOpt.get().getMinValue() + " - "
                                    + pregThresholdOpt.get().getMaxValue();
                            if (pregRange.equals(res.getReferenceRange())) {
                                boolean distinct = true;
                                if (normalTypeId != null) {
                                    Optional<IndicatorThreshold> normalThresholdOpt = indicatorThresholdRepository
                                            .findByLabTest_LabTestIdAndPatientType_PatientTypeId(
                                                    res.getLabTest().getLabTestId(), normalTypeId);
                                    if (normalThresholdOpt.isPresent()) {
                                        String normalRange = normalThresholdOpt.get().getMinValue() + " - "
                                                + normalThresholdOpt.get().getMaxValue();
                                        if (normalRange.equals(pregRange)) {
                                            distinct = false;
                                        }
                                    }
                                }
                                if (distinct) {
                                    isPregnant = true;
                                    break;
                                }
                            }
                        }
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

        // Load results
        if (!results.isEmpty()) {
            model.addAttribute("lastExamLabResults", results);
            model.addAttribute("labResults", results);
        }

        // Put drug details to model
        Prescription prescription = prescriptionRepository.findByClinicalExamination_ClinicalExamId(examId)
                .orElse(null);
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

        // Custom validation check 1: prescription must not be empty (Commented out to
        // allow updating without prescription)
        // if (form.getPrescriptionJson() == null ||
        // form.getPrescriptionJson().trim().isEmpty() ||
        // "[]".equals(form.getPrescriptionJson().trim())) {
        // bindingResult.rejectValue("prescriptionJson", "error.prescriptionJson", "Vui
        // lòng kê đơn ít nhất một loại thuốc.");
        // }

        // Custom validation check 2: treatment plan must have at least one field filled
        // if (ParseUtil.isBlank(form.getTreatmentGoal()) &&
        // ParseUtil.isBlank(form.getDietPlan()) &&
        // ParseUtil.isBlank(form.getExercisePlan()) &&
        // ParseUtil.isBlank(form.getGlucoseMonitoringPlan()) &&
        // ParseUtil.isBlank(form.getMedicationPlan())) {
        // bindingResult.rejectValue("treatmentGoal", "error.treatmentGoal", "Yêu cầu
        // điền ít nhất 1 trường của Kế hoạch & Phác đồ điều trị.");
        // }

        ClinicalExamination exam = clinicalExaminationRepository.findById(examId)
                .orElseThrow(
                        () -> new jakarta.persistence.EntityNotFoundException("Không tìm thấy ca khám: " + examId));

        if (exam.getExamDate() != null) {
            java.time.LocalDate examLocalDate = exam.getExamDate().toLocalDate();
            java.time.LocalDate today = java.time.LocalDate.now();
            if (!examLocalDate.equals(today)) {
                throw new IllegalStateException("Chỉ được phép chỉnh sửa ca khám được thực hiện trong ngày hôm nay.");
            }
        }

        if (bindingResult.hasErrors()) {
            String patientId = exam.getPatient().getUserId();
            populateExamineModel(patientId, false, session, model, loggedInUser);
            model.addAttribute("isEditMode", true);
            model.addAttribute("lastExam", exam);
            model.addAttribute("validationError",
                    "Có lỗi xảy ra trong dữ liệu nhập vào. Vui lòng kiểm tra lại các trường thông báo đỏ.");

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
                model.addAttribute("labResults", results);
            }

            Prescription prescription = prescriptionRepository.findByClinicalExamination_ClinicalExamId(examId)
                    .orElse(null);
            List<PrescriptionDetail> details = Collections.emptyList();
            if (prescription != null) {
                details = prescriptionDetailRepository
                        .findByPrescription_PrescriptionId(prescription.getPrescriptionId());
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
            examMap.put("nextAppointment",
                    exam.getNextAppointment() != null ? exam.getNextAppointment().toString() : null);
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

        return "redirect:/doctor/queue?toast=updated";
    }
}
