package com.quan.diabetes.service.exam.impl;

import com.quan.diabetes.dto.doctor.ClinicalExamForm;
import com.quan.diabetes.dto.doctor.ExamStep1Form;
import com.quan.diabetes.dto.doctor.ExamStep2Form;
import com.quan.diabetes.dto.doctor.ExamStep3Form;
import com.quan.diabetes.dto.doctor.PrescriptionLineDTO;
import com.quan.diabetes.service.exam.ClinicalExaminationService;
import com.quan.diabetes.service.reminder.MedicationSchedualeService;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import com.quan.diabetes.entity.*;
import com.quan.diabetes.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import com.quan.diabetes.service.reminder.AppointmentSchedule;
import com.quan.diabetes.service.systemlog.SystemLogService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class ClinicalExaminationServiceImpl implements ClinicalExaminationService {

    private final ClinicalExaminationRepository clinicalExaminationRepository;
    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final SymptomsCatalogRepository symptomsCatalogRepository;
    private final ExamSymptomRepository examSymptomRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final PrescriptionDetailRepository prescriptionDetailRepository;
    private final MedicationRepository medicationRepository;
    private final MedicationTimingRepository medicationTimingRepository;
    private final PrescriptionTimingRepository prescriptionTimingRepository;
    private final TreatmentPlanRepository treatmentPlanRepository;
    private final LabOrderRepository labOrderRepository;
    private final LabResultRepository labResultRepository;
    private final LabTestCatalogRepository labTestCatalogRepository;
    private final PatientTypeRepository patientTypeRepository;
    private final IndicatorThresholdRepository indicatorThresholdRepository;

    // khai báo Reminder
    private final MedicationSchedualeService medicationSchedualeService;

    @Autowired
    private AppointmentSchedule appointmentSchedule;

    @Autowired
    private ReminderRepository reminderRepository;

    private final SystemLogService systemLogService;

    public ClinicalExaminationServiceImpl(
            ClinicalExaminationRepository clinicalExaminationRepository,
            UserRepository userRepository,
            PatientRepository patientRepository,
            SymptomsCatalogRepository symptomsCatalogRepository,
            ExamSymptomRepository examSymptomRepository,
            PrescriptionRepository prescriptionRepository,
            PrescriptionDetailRepository prescriptionDetailRepository,
            MedicationRepository medicationRepository,
            MedicationTimingRepository medicationTimingRepository,
            PrescriptionTimingRepository prescriptionTimingRepository,
            TreatmentPlanRepository treatmentPlanRepository,
            LabOrderRepository labOrderRepository,
            LabResultRepository labResultRepository,
            LabTestCatalogRepository labTestCatalogRepository,
            PatientTypeRepository patientTypeRepository,
            IndicatorThresholdRepository indicatorThresholdRepository,
            MedicationSchedualeService medicationSchedualeService,
            SystemLogService systemLogService) {
        this.clinicalExaminationRepository = clinicalExaminationRepository;
        this.userRepository = userRepository;
        this.patientRepository = patientRepository;
        this.symptomsCatalogRepository = symptomsCatalogRepository;
        this.examSymptomRepository = examSymptomRepository;
        this.prescriptionRepository = prescriptionRepository;
        this.prescriptionDetailRepository = prescriptionDetailRepository;
        this.medicationRepository = medicationRepository;
        this.medicationTimingRepository = medicationTimingRepository;
        this.prescriptionTimingRepository = prescriptionTimingRepository;
        this.treatmentPlanRepository = treatmentPlanRepository;
        this.labOrderRepository = labOrderRepository;
        this.labResultRepository = labResultRepository;
        this.labTestCatalogRepository = labTestCatalogRepository;
        this.patientTypeRepository = patientTypeRepository;
        this.indicatorThresholdRepository = indicatorThresholdRepository;
        this.medicationSchedualeService = medicationSchedualeService;
        this.systemLogService = systemLogService;
    }

    @Override
    public List<ClinicalExamination> findAll() {
        return clinicalExaminationRepository.findAll();
    }

    @Override
    public Optional<ClinicalExamination> findById(String id) {
        return clinicalExaminationRepository.findById(id);
    }

    @Override
    public ClinicalExamination create(ClinicalExamination entity) {
        return clinicalExaminationRepository.save(entity);
    }

    @Override
    public ClinicalExamination update(String id, ClinicalExamination entity) {
        if (!clinicalExaminationRepository.existsById(id)) {
            throw new EntityNotFoundException("ClinicalExamination not found with id: " + id);
        }
        return clinicalExaminationRepository.save(entity);
    }

    @Override
    public void deleteById(String id) {
        if (!clinicalExaminationRepository.existsById(id)) {
            throw new EntityNotFoundException("ClinicalExamination not found with id: " + id);
        }
        clinicalExaminationRepository.deleteById(id);
    }

    @Override
    public boolean existsById(String id) {
        return clinicalExaminationRepository.existsById(id);
    }

    @Override
    public List<ClinicalExamination> findByDoctorId(String doctorId) {
        return clinicalExaminationRepository.findByDoctor_UserIdOrderByExamDateAsc(doctorId);
    }

    @Override
    public List<ClinicalExamination> findByPatientId(String patientId) {
        return clinicalExaminationRepository.findByPatient_UserIdOrderByExamDateDesc(patientId);
    }

    @Override
    @Transactional
    public void startExamination(String patientId, String doctorId) {
        ClinicalExamination exam = clinicalExaminationRepository
                .findFirstByPatient_UserIdAndDoctor_UserIdAndStatusIn(
                        patientId, doctorId, List.of("Pending", "InProgress"))
                .orElse(null);

        if (exam == null) {
            exam = new ClinicalExamination();
            String clinicalExamId = generateClinicalExamId();
            exam.setClinicalExamId(clinicalExamId);
            exam.setPatient(patientRepository.findById(patientId)
                    .orElseThrow(() -> new EntityNotFoundException("Patient not found: " + patientId)));
            exam.setDoctor(userRepository.findById(doctorId)
                    .orElseThrow(() -> new EntityNotFoundException("Doctor not found: " + doctorId)));
            exam.setExamDate(LocalDateTime.now());
        }
        Map<String, Object> oldExam = createLogExam(exam.getClinicalExamId());

        exam.setStatus("InProgress");
        clinicalExaminationRepository.save(exam);

        systemLogService.saveLogWithObject(null, "APPROVE_MEDICAL_RECORD", "MedicalRecord", exam.getClinicalExamId(),
                "Bác sĩ tiếp nhận bệnh án", oldExam, createLogExam(exam.getClinicalExamId()), "SUCCESS");
    }

    @Override
    @Transactional
    public void cancelExamination(String patientId, String reason, String doctorId) {
        ClinicalExamination exam = clinicalExaminationRepository
                .findFirstByPatient_UserIdAndDoctor_UserIdAndStatusIn(
                        patientId, doctorId, List.of("Pending", "InProgress"))
                .orElse(null);

        if (exam == null) {
            exam = new ClinicalExamination();
            String clinicalExamId = generateClinicalExamId();
            exam.setClinicalExamId(clinicalExamId);
            exam.setPatient(patientRepository.findById(patientId)
                    .orElseThrow(() -> new EntityNotFoundException("Patient not found: " + patientId)));
            exam.setDoctor(userRepository.findById(doctorId)
                    .orElseThrow(() -> new EntityNotFoundException("Doctor not found: " + doctorId)));
            exam.setExamDate(LocalDateTime.now());
        }
        Map<String, Object> oldExam = createLogExam(exam.getClinicalExamId());

        exam.setStatus("Cancelled");
        exam.setCancelReason(reason);
        exam.setDiagnosisNote(null);
        clinicalExaminationRepository.save(exam);

        systemLogService.saveLogWithObject(null, "REJECT_MEDICAL_RECORD", "MedicalRecord", exam.getClinicalExamId(),
                "Bác sĩ từ chối/hủy bệnh án", oldExam, createLogExam(exam.getClinicalExamId()), "SUCCESS");
    }

    @Override
    @Transactional
    public void submitExamination(String patientId, ClinicalExamForm form, String doctorId) {
        ClinicalExamination exam = clinicalExaminationRepository
                .findFirstByPatient_UserIdAndDoctor_UserIdAndStatusIn(
                        patientId, doctorId, List.of("Pending", "InProgress"))
                .orElse(null);
        if (exam == null) {
            exam = new ClinicalExamination();
            // code của quân
            String clinicalExamId = generateClinicalExamId();
            // code của quân
            exam.setClinicalExamId(clinicalExamId);
            exam.setPatient(patientRepository.findById(patientId)
                    .orElseThrow(() -> new EntityNotFoundException("Patient not found: " + patientId)));
            exam.setDoctor(userRepository.findById(doctorId)
                    .orElseThrow(() -> new EntityNotFoundException("Doctor not found: " + doctorId)));
            exam.setExamDate(LocalDateTime.now());
        }

        Map<String, Object> oldExam = createLogExam(exam.getClinicalExamId());

        // 1. Cập nhật thông tin chính ca khám
        exam.setMedicalHistory(form.getMedicalHistory());
        exam.setDiagnosisNote(form.getDiagnosisNote());
        if (form.getNextAppointment() != null && !form.getNextAppointment().trim().isEmpty()) {
            exam.setNextAppointment(LocalDate.parse(form.getNextAppointment()).atStartOfDay());
        } else {
            exam.setNextAppointment(null);
        }
        exam.setStatus("Completed");
        exam = clinicalExaminationRepository.save(exam);

        String examId = exam.getClinicalExamId();

        // 2. Lưu Triệu chứng (Clear cũ trước)
        examSymptomRepository.deleteById_ClinicalExamId(examId);
        examSymptomRepository.flush();

        Map<String, String> symptomComments = form.getSymptomComments();

        if (form.getSymptomIds() != null) {
            for (String symId : form.getSymptomIds()) {
                SymptomsCatalog symptom = symptomsCatalogRepository.findById(symId).orElse(null);
                if (symptom != null) {
                    ExamSymptom examSymptom = new ExamSymptom();
                    examSymptom.setId(new ExamSymptomId(examId, symId));
                    examSymptom.setClinicalExamination(exam);
                    examSymptom.setSymptom(symptom);
                    if (symptomComments != null && symptomComments.containsKey(symId)) {
                        examSymptom.setNote(symptomComments.get(symId));
                    }
                    examSymptomRepository.save(examSymptom);
                }
            }
        }

        final ClinicalExamination finalExam = exam;
        // 3. Lưu Kế hoạch điều trị (Cập nhật nếu đã có, hoặc tạo mới nếu chưa để tránh
        // lỗi UNIQUE KEY constraint)
        TreatmentPlan plan = treatmentPlanRepository.findByClinicalExam_ClinicalExamId(examId)
                .orElseGet(() -> {
                    TreatmentPlan newPlan = new TreatmentPlan();
                    newPlan.setClinicalExam(finalExam);
                    newPlan.setCreatedAt(LocalDateTime.now());
                    return newPlan;
                });

        plan.setTreatmentGoal(form.getTreatmentGoal());
        plan.setDietPlan(form.getDietPlan());
        plan.setExercisePlan(form.getExercisePlan());
        plan.setGlucoseMonitoringPlan(form.getGlucoseMonitoringPlan());
        treatmentPlanRepository.save(plan);

        // 4. Lưu Chỉ định & Kết quả xét nghiệm (Clear cũ trước)
        labOrderRepository.findByClinicalExamination_ClinicalExamId(examId)
                .ifPresent(labOrderRepository::delete);

        if (form.getLabTestIds() != null && !form.getLabTestIds().isEmpty()) {
            LabOrder labOrder = new LabOrder();
            labOrder.setLabOrderId("LBO-" + System.currentTimeMillis() + "-" + new Random().nextInt(1000));
            labOrder.setClinicalExamination(exam);
            labOrder.setStatus("Completed");
            labOrder = labOrderRepository.save(labOrder);

            // Parse simulated lab values from frontend if available
            Map<String, Map<String, Object>> simulatedResults = new HashMap<>();
            if (form.getLabResultsJson() != null && !form.getLabResultsJson().trim().isEmpty()) {
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    List<Map<String, Object>> list = mapper.readValue(form.getLabResultsJson(),
                            new TypeReference<List<Map<String, Object>>>() {
                            });
                    for (Map<String, Object> item : list) {
                        String testId = (String) item.get("testId");
                        simulatedResults.put(testId, item);
                    }
                } catch (Exception e) {
                    // Ignore or log
                }
            }

            Patient patient = exam.getPatient();
            PatientType matchedType = null;
            if (Boolean.TRUE.equals(form.getIsPregnant())) {
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

            for (String testId : form.getLabTestIds()) {
                LabTestCatalog test = labTestCatalogRepository.findById(testId).orElse(null);
                if (test != null) {
                    LabResult result = new LabResult();
                    result.setLabResultId("LBR-" + System.currentTimeMillis() + "-" + new Random().nextInt(1000));
                    result.setLabOrder(labOrder);
                    result.setLabTest(test);
                    result.setStatus("Completed");

                    BigDecimal value = BigDecimal.valueOf(4.8);
                    String flag = "NORMAL";

                    // Retrieve thresholds dynamically from database
                    Optional<IndicatorThreshold> thresholdOpt = Optional.empty();
                    if (matchedType != null) {
                        thresholdOpt = indicatorThresholdRepository.findByLabTest_LabTestIdAndPatientType_PatientTypeId(
                                testId, matchedType.getPatientTypeId());
                    }
                    if (thresholdOpt.isEmpty()) {
                        List<IndicatorThreshold> thresholds = indicatorThresholdRepository
                                .findByLabTest_LabTestId(testId);
                        if (!thresholds.isEmpty()) {
                            thresholdOpt = Optional.of(thresholds.get(0));
                        }
                    }

                    String range = "N/A";
                    BigDecimal dbMin = null;
                    BigDecimal dbMax = null;

                    if (thresholdOpt.isPresent()) {
                        dbMin = thresholdOpt.get().getMinValue();
                        dbMax = thresholdOpt.get().getMaxValue();
                        range = dbMin + " - " + dbMax;
                    }

                    if (simulatedResults.containsKey(testId)) {
                        Map<String, Object> simInfo = simulatedResults.get(testId);
                        value = BigDecimal.valueOf(Double.parseDouble(simInfo.get("val").toString()));

                        // Backend calculations for flag safety
                        if (dbMin != null && dbMax != null) {
                            if (value.compareTo(dbMin) < 0)
                                flag = "LOW";
                            else if (value.compareTo(dbMax) > 0)
                                flag = "HIGH";
                            else
                                flag = "NORMAL";
                        } else {
                            flag = "NORMAL";
                        }
                    } else {
                        value = BigDecimal.valueOf(0.0);
                        flag = "NORMAL";
                    }

                    result.setResultValue(value);
                    result.setReferenceRange(range);
                    result.setFlag(flag);
                    labResultRepository.save(result);
                }
            }
        }

        // 5. Lưu đơn thuốc (Clear cũ trước)
        prescriptionRepository.findByClinicalExamination_ClinicalExamId(examId)
                .ifPresent(prescriptionRepository::delete);

        if (form.getPrescriptionJson() != null && !form.getPrescriptionJson().trim().isEmpty()) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                List<Map<String, Object>> lines = mapper.readValue(form.getPrescriptionJson(),
                        new TypeReference<List<Map<String, Object>>>() {
                        });

                if (!lines.isEmpty()) {
                    Prescription presc = new Prescription();
                    presc.setPrescriptionId("PRC-" + System.currentTimeMillis() + "-" + new Random().nextInt(1000));
                    presc.setClinicalExamination(exam);
                    presc.setCreatedAt(LocalDateTime.now());
                    presc = prescriptionRepository.save(presc);

                    for (Map<String, Object> line : lines) {
                        String medId = (String) line.get("medId");
                        Medication med = medicationRepository.findById(medId).orElse(null);
                        if (med != null) {
                            PrescriptionDetail detail = new PrescriptionDetail();
                            detail.setPrescriptionDetailId(
                                    "PRD-" + System.currentTimeMillis() + "-" + new Random().nextInt(1000));
                            detail.setPrescription(presc);
                            detail.setMedication(med);
                            Object durationObj = line.get("duration");
                            int duration = durationObj instanceof Integer ? (Integer) durationObj
                                    : Integer.parseInt(durationObj.toString());
                            detail.setDurationDays(duration);

                            Object qtyObj = line.get("quantity");
                            int quantity = qtyObj instanceof Integer ? (Integer) qtyObj
                                    : Integer.parseInt(qtyObj.toString());
                            detail.setTotalQuantity(quantity);

                            Object clientDosageObj = line.get("dosage");
                            String dosage = (clientDosageObj != null && !clientDosageObj.toString().trim().isEmpty()
                                    && !"Auto".equalsIgnoreCase(clientDosageObj.toString()))
                                            ? clientDosageObj.toString()
                                            : calculateDosage(quantity, duration, med.getForm());
                            detail.setDosage(dosage);

                            Object medPlanObj = line.get("medicationPlan");
                            detail.setMedicationPlan(medPlanObj != null ? medPlanObj.toString() : "");

                            Object startDateObj = line.get("startDate");
                            if (startDateObj != null && !startDateObj.toString().trim().isEmpty()) {
                                detail.setStartDate(LocalDate.parse(startDateObj.toString()));
                            }
                            Object endDateObj = line.get("endDate");
                            if (endDateObj != null && !endDateObj.toString().trim().isEmpty()) {
                                detail.setEndDate(LocalDate.parse(endDateObj.toString()));
                            }

                            detail.setPrescriptionTimings(new ArrayList<>());
                            detail = prescriptionDetailRepository.save(detail);

                            // Lưu Timing cho đơn thuốc
                            String timingText = (String) line.get("timingText");
                            if (timingText != null && !timingText.trim().isEmpty()) {
                                String[] parts = timingText.split(",\\s*");
                                for (String part : parts) {
                                    if (part.trim().isEmpty()) {
                                        continue;
                                    }
                                    final String singleTimingText = part.trim();
                                    MedicationTiming timing = medicationTimingRepository
                                            .findByTimingName(singleTimingText)
                                            .orElseGet(() -> {
                                                MedicationTiming newTiming = new MedicationTiming();
                                                newTiming.setTimingName(singleTimingText);
                                                return medicationTimingRepository.save(newTiming);
                                            });

                                    PrescriptionTiming pTiming = new PrescriptionTiming();
                                    pTiming.setPrescriptionDetail(detail);
                                    pTiming.setTiming(timing);
                                    prescriptionTimingRepository.save(pTiming);

                                }
                            }
                        }
                    }
                }
                // tao và luu reminder
                medicationSchedualeService.generateReminder(examId);
                appointmentSchedule.generateAppointmentReminder(examId);
                // tao và luu reminder
            } catch (Exception e) {
                throw new RuntimeException("Error deserializing prescription JSON: " + e.getMessage(), e);
            }
        }

        systemLogService.saveLogWithObject(null, "COMPLETE_MEDICAL_RECORD", "MedicalRecord", examId,
                "Bác sĩ hoàn thành bệnh án", oldExam, createLogExam(exam.getClinicalExamId()), "SUCCESS");
    }

    @Override
    @Transactional
    public void createAutoPendingExamination(String patientId) {
        User doctor = userRepository.findFirstByRole_RoleId("DOC").orElse(null);
        if (doctor == null) {
            // No doctor in DB, skip auto-creation silently or log it
            return;
        }

        boolean hasActive = clinicalExaminationRepository
                .findFirstByPatient_UserIdAndDoctor_UserIdAndStatusIn(
                        patientId, doctor.getUserId(), List.of("Pending", "InProgress"))
                .isPresent();

        if (!hasActive) {
            ClinicalExamination exam = new ClinicalExamination();
            String clinicalExamId = generateClinicalExamId();
            exam.setClinicalExamId(clinicalExamId);
            exam.setPatient(patientRepository.findById(patientId)
                    .orElseThrow(() -> new EntityNotFoundException("Patient not found: " + patientId)));
            exam.setDoctor(doctor);
            exam.setExamDate(LocalDateTime.now());
            exam.setStatus("Pending");
            clinicalExaminationRepository.save(exam);
        }
    }

    private String calculateDosage(int totalQuantity, int durationDays, String form) {
        if (durationDays <= 0) {
            return "0 viên/ngày";
        }
        double rate = (double) totalQuantity / durationDays;

        // Format rate: if it has no decimal part, show as integer
        String rateStr;
        if (rate == (long) rate) {
            rateStr = String.format("%d", (long) rate);
        } else {
            rateStr = String.format(Locale.US, "%.1f", rate);
        }

        String unit = "viên";
        if (form != null) {
            String formLower = form.toLowerCase();
            if (formLower.contains("viên") || formLower.contains("nén") || formLower.contains("nang")) {
                unit = "viên";
            } else if (formLower.contains("gói")) {
                unit = "gói";
            } else if (formLower.contains("chai")) {
                unit = "chai";
            } else if (formLower.contains("ống")) {
                unit = "ống";
            } else if (formLower.contains("tablet")) {
                unit = "tablet";
            } else if (formLower.contains("capsule")) {
                unit = "capsule";
            } else if (!formLower.trim().isEmpty()) {
                unit = formLower.trim();
            }
        }

        return rateStr + " " + unit + "/ngày";
    }

    @Override
    @Transactional
    public void updateExamination(String examId, ClinicalExamForm form) {
        ClinicalExamination exam = clinicalExaminationRepository.findById(examId)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException(
                        "Không tìm thấy ca khám để cập nhật: " + examId));

        Map<String, Object> oldExam = createLogExam(exam.getClinicalExamId());

        // 1. Cập nhật thông tin chính ca khám
        exam.setMedicalHistory(form.getMedicalHistory());
        exam.setDiagnosisNote(form.getDiagnosisNote());
        if (form.getNextAppointment() != null && !form.getNextAppointment().trim().isEmpty()) {
            exam.setNextAppointment(LocalDate.parse(form.getNextAppointment()).atStartOfDay());
        } else {
            exam.setNextAppointment(null);
        }
        exam.setStatus("Completed");
        exam = clinicalExaminationRepository.save(exam);

        // 2. Lưu Triệu chứng (Clear cũ trước)
        examSymptomRepository.deleteById_ClinicalExamId(examId);
        examSymptomRepository.flush();

        Map<String, String> symptomComments = form.getSymptomComments();

        if (form.getSymptomIds() != null) {
            for (String symId : form.getSymptomIds()) {
                SymptomsCatalog symptom = symptomsCatalogRepository.findById(symId).orElse(null);
                if (symptom != null) {
                    ExamSymptom examSymptom = new ExamSymptom();
                    examSymptom.setId(new ExamSymptomId(examId, symId));
                    examSymptom.setClinicalExamination(exam);
                    examSymptom.setSymptom(symptom);
                    if (symptomComments != null && symptomComments.containsKey(symId)) {
                        examSymptom.setNote(symptomComments.get(symId));
                    }
                    examSymptomRepository.save(examSymptom);
                }
            }
        }

        final ClinicalExamination finalExam = exam;
        // 3. Lưu Kế hoạch điều trị (Cập nhật nếu đã có, hoặc tạo mới nếu chưa)
        TreatmentPlan plan = treatmentPlanRepository.findByClinicalExam_ClinicalExamId(examId)
                .orElseGet(() -> {
                    TreatmentPlan newPlan = new TreatmentPlan();
                    newPlan.setClinicalExam(finalExam);
                    newPlan.setCreatedAt(LocalDateTime.now());
                    return newPlan;
                });

        plan.setTreatmentGoal(form.getTreatmentGoal());
        plan.setDietPlan(form.getDietPlan());
        plan.setExercisePlan(form.getExercisePlan());
        plan.setGlucoseMonitoringPlan(form.getGlucoseMonitoringPlan());
        treatmentPlanRepository.save(plan);

        // 4. Lưu Chỉ định & Kết quả xét nghiệm (Clear cũ trước)
        labOrderRepository.findByClinicalExamination_ClinicalExamId(examId)
                .ifPresent(labOrderRepository::delete);

        if (form.getLabTestIds() != null && !form.getLabTestIds().isEmpty()) {
            LabOrder labOrder = new LabOrder();
            labOrder.setLabOrderId("LBO-" + System.currentTimeMillis() + "-" + new Random().nextInt(1000));
            labOrder.setClinicalExamination(exam);
            labOrder.setStatus("Completed");
            labOrder = labOrderRepository.save(labOrder);

            // Parse simulated lab values from frontend if available
            Map<String, Map<String, Object>> simulatedResults = new HashMap<>();
            if (form.getLabResultsJson() != null && !form.getLabResultsJson().trim().isEmpty()) {
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    List<Map<String, Object>> list = mapper.readValue(form.getLabResultsJson(),
                            new TypeReference<List<Map<String, Object>>>() {
                            });
                    for (Map<String, Object> item : list) {
                        String testId = (String) item.get("testId");
                        simulatedResults.put(testId, item);
                    }
                } catch (Exception e) {
                    // Ignore or log
                }
            }

            Patient patient = exam.getPatient();
            PatientType matchedType = null;
            if (Boolean.TRUE.equals(form.getIsPregnant())) {
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

            for (String testId : form.getLabTestIds()) {
                LabTestCatalog test = labTestCatalogRepository.findById(testId).orElse(null);
                if (test != null) {
                    LabResult result = new LabResult();
                    result.setLabResultId("LBR-" + System.currentTimeMillis() + "-" + new Random().nextInt(1000));
                    result.setLabOrder(labOrder);
                    result.setLabTest(test);
                    result.setStatus("Completed");

                    BigDecimal value = BigDecimal.valueOf(4.8);
                    String flag = "NORMAL";

                    // Retrieve thresholds dynamically from database
                    Optional<IndicatorThreshold> thresholdOpt = Optional.empty();
                    if (matchedType != null) {
                        thresholdOpt = indicatorThresholdRepository.findByLabTest_LabTestIdAndPatientType_PatientTypeId(
                                testId, matchedType.getPatientTypeId());
                    }
                    if (thresholdOpt.isEmpty()) {
                        List<IndicatorThreshold> thresholds = indicatorThresholdRepository
                                .findByLabTest_LabTestId(testId);
                        if (!thresholds.isEmpty()) {
                            thresholdOpt = Optional.of(thresholds.get(0));
                        }
                    }

                    String range = "N/A";
                    BigDecimal dbMin = null;
                    BigDecimal dbMax = null;

                    if (thresholdOpt.isPresent()) {
                        dbMin = thresholdOpt.get().getMinValue();
                        dbMax = thresholdOpt.get().getMaxValue();
                        range = dbMin + " - " + dbMax;
                    }

                    if (simulatedResults.containsKey(testId)) {
                        Map<String, Object> simInfo = simulatedResults.get(testId);
                        value = BigDecimal.valueOf(Double.parseDouble(simInfo.get("val").toString()));

                        // Backend calculations for flag safety
                        if (dbMin != null && dbMax != null) {
                            if (value.compareTo(dbMin) < 0)
                                flag = "LOW";
                            else if (value.compareTo(dbMax) > 0)
                                flag = "HIGH";
                            else
                                flag = "NORMAL";
                        } else {
                            flag = "NORMAL";
                        }
                    } else {
                        value = BigDecimal.valueOf(0.0);
                        flag = "NORMAL";
                    }

                    result.setResultValue(value);
                    result.setReferenceRange(range);
                    result.setFlag(flag);
                    labResultRepository.save(result);
                }
            }
        }

        // 5. Lưu đơn thuốc (Clear cũ trước)
        prescriptionRepository.findByClinicalExamination_ClinicalExamId(examId)
                .ifPresent(old -> {
                    prescriptionDetailRepository.deleteAll(
                            prescriptionDetailRepository.findByPrescription_PrescriptionId(old.getPrescriptionId()));
                    prescriptionRepository.delete(old);
                });

        if (form.getPrescriptionJson() != null && !form.getPrescriptionJson().trim().isEmpty()) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                List<Map<String, Object>> lines = mapper.readValue(form.getPrescriptionJson(),
                        new TypeReference<List<Map<String, Object>>>() {
                        });

                if (!lines.isEmpty()) {
                    Prescription presc = new Prescription();
                    presc.setPrescriptionId("PRC-" + System.currentTimeMillis() + "-" + new Random().nextInt(1000));
                    presc.setClinicalExamination(exam);
                    presc.setCreatedAt(LocalDateTime.now());
                    presc = prescriptionRepository.saveAndFlush(presc);

                    for (Map<String, Object> line : lines) {
                        String medId = (String) line.get("medId");
                        Medication med = medicationRepository.findById(medId).orElse(null);
                        if (med != null) {
                            PrescriptionDetail detail = new PrescriptionDetail();
                            detail.setPrescriptionDetailId(
                                    "PRD-" + System.currentTimeMillis() + "-" + new Random().nextInt(1000));
                            detail.setPrescription(presc);
                            detail.setMedication(med);
                            Object durationObj = line.get("duration");
                            int duration = durationObj instanceof Integer ? (Integer) durationObj
                                    : Integer.parseInt(durationObj.toString());
                            detail.setDurationDays(duration);

                            Object qtyObj = line.get("quantity");
                            int quantity = qtyObj instanceof Integer ? (Integer) qtyObj
                                    : Integer.parseInt(qtyObj.toString());
                            detail.setTotalQuantity(quantity);

                            Object clientDosageObj = line.get("dosage");
                            String dosage = (clientDosageObj != null && !clientDosageObj.toString().trim().isEmpty()
                                    && !"Auto".equalsIgnoreCase(clientDosageObj.toString()))
                                            ? clientDosageObj.toString()
                                            : calculateDosage(quantity, duration, med.getForm());
                            detail.setDosage(dosage);

                            Object medPlanObj = line.get("medicationPlan");
                            detail.setMedicationPlan(medPlanObj != null ? medPlanObj.toString() : "");

                            Object startDateObj = line.get("startDate");
                            if (startDateObj != null && !startDateObj.toString().trim().isEmpty()) {
                                detail.setStartDate(LocalDate.parse(startDateObj.toString()));
                            }
                            Object endDateObj = line.get("endDate");
                            if (endDateObj != null && !endDateObj.toString().trim().isEmpty()) {
                                detail.setEndDate(LocalDate.parse(endDateObj.toString()));
                            }

                            detail.setPrescriptionTimings(new ArrayList<>());
                            detail = prescriptionDetailRepository.saveAndFlush(detail);

                            // Lưu Timing cho đơn thuốc
                            String timingText = (String) line.get("timingText");
                            if (timingText != null && !timingText.trim().isEmpty()) {
                                String[] parts = timingText.split(",\\s*");
                                for (String part : parts) {
                                    if (part.trim().isEmpty()) {
                                        continue;
                                    }
                                    final String singleTimingText = part.trim();
                                    MedicationTiming timing = medicationTimingRepository
                                            .findByTimingName(singleTimingText)
                                            .orElseGet(() -> {
                                                MedicationTiming newTiming = new MedicationTiming();
                                                newTiming.setTimingName(singleTimingText);
                                                return medicationTimingRepository.save(newTiming);
                                            });

                                    PrescriptionTiming pTiming = new PrescriptionTiming();
                                    pTiming.setPrescriptionDetail(detail);
                                    pTiming.setTiming(timing);
                                    prescriptionTimingRepository.save(pTiming);
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException("Error deserializing prescription JSON: " + e.getMessage(), e);
            }
        }

        // Lock các reminder cũ của phiên khám (set lockStatus = true)
        List<Reminder> oldReminders = reminderRepository.findByClinicalExamination_ClinicalExamId(examId);
        if (oldReminders != null && !oldReminders.isEmpty()) {
            for (Reminder r : oldReminders) {
                r.setLockStatus(true);
            }
            reminderRepository.saveAll(oldReminders);
        }

        // Tạo lại reminders mới dựa vào lịch tái khám và đơn thuốc
        medicationSchedualeService.generateReminder(examId);
        appointmentSchedule.generateAppointmentReminder(examId);
    }

    @Override
    @Transactional
    public void requestExamination(String patientId, String medicalHistory) {
        User doctor = userRepository.findFirstByRole_RoleId("DOC").orElse(null);
        if (doctor == null) {
            throw new RuntimeException("Không tìm thấy bác sĩ nào trong hệ thống.");
        }

        // Check if there is an active exam (Pending, InProgress, Requested)
        boolean hasActive = clinicalExaminationRepository
                .findFirstByPatient_UserIdAndDoctor_UserIdAndStatusIn(
                        patientId, doctor.getUserId(), List.of("Requested", "Pending", "InProgress"))
                .isPresent();

        if (hasActive) {
            throw new RuntimeException("Bạn đã có một yêu cầu khám đang chờ duyệt hoặc một ca khám đang diễn ra.");
        }

        ClinicalExamination exam = new ClinicalExamination();
        String clinicalExamId = generateClinicalExamId();
        exam.setClinicalExamId(clinicalExamId);
        exam.setPatient(patientRepository.findById(patientId)
                .orElseThrow(() -> new EntityNotFoundException("Patient not found: " + patientId)));
        exam.setDoctor(doctor);
        exam.setExamDate(LocalDateTime.now());
        exam.setStatus("Requested");
        exam.setMedicalHistory(medicalHistory);
        clinicalExaminationRepository.save(exam);

        systemLogService.saveLogWithObject(patientId, "CREATE_MEDICAL_REQUEST", "MedicalRequest",
                exam.getClinicalExamId(), "Bệnh nhân tạo yêu cầu khám", null, createLogExam(exam.getClinicalExamId()),
                "SUCCESS");
    }

    @Override
    @Transactional
    public void saveDraft(String patientId, ClinicalExamForm form, String doctorId) {
        ClinicalExamination exam = clinicalExaminationRepository
                .findFirstByPatient_UserIdAndDoctor_UserIdAndStatusIn(
                        patientId, doctorId, List.of("Pending", "InProgress"))
                .orElse(null);
        if (exam == null) {
            exam = new ClinicalExamination();
            String clinicalExamId = generateClinicalExamId();
            exam.setClinicalExamId(clinicalExamId);
            exam.setPatient(patientRepository.findById(patientId)
                    .orElseThrow(() -> new EntityNotFoundException("Patient not found: " + patientId)));
            exam.setDoctor(userRepository.findById(doctorId)
                    .orElseThrow(() -> new EntityNotFoundException("Doctor not found: " + doctorId)));
            exam.setExamDate(LocalDateTime.now());
        }

        // 1. Cập nhật thông tin chính ca khám
        exam.setMedicalHistory(form.getMedicalHistory());
        exam.setDiagnosisNote(form.getDiagnosisNote());
        if (form.getNextAppointment() != null && !form.getNextAppointment().trim().isEmpty()) {
            exam.setNextAppointment(LocalDate.parse(form.getNextAppointment()).atStartOfDay());
        } else {
            exam.setNextAppointment(null);
        }
        exam.setStatus("InProgress");
        exam = clinicalExaminationRepository.save(exam);

        String examId = exam.getClinicalExamId();

        // 2. Lưu Triệu chứng (Clear cũ trước)
        examSymptomRepository.deleteById_ClinicalExamId(examId);
        examSymptomRepository.flush();

        Map<String, String> symptomComments = form.getSymptomComments();

        if (form.getSymptomIds() != null) {
            for (String symId : form.getSymptomIds()) {
                SymptomsCatalog symptom = symptomsCatalogRepository.findById(symId).orElse(null);
                if (symptom != null) {
                    ExamSymptom examSymptom = new ExamSymptom();
                    examSymptom.setId(new ExamSymptomId(examId, symId));
                    examSymptom.setClinicalExamination(exam);
                    examSymptom.setSymptom(symptom);
                    if (symptomComments != null && symptomComments.containsKey(symId)) {
                        examSymptom.setNote(symptomComments.get(symId));
                    }
                    examSymptomRepository.save(examSymptom);
                }
            }
        }

        final ClinicalExamination finalExam = exam;
        // 3. Lưu Kế hoạch điều trị
        TreatmentPlan plan = treatmentPlanRepository.findByClinicalExam_ClinicalExamId(examId)
                .orElseGet(() -> {
                    TreatmentPlan newPlan = new TreatmentPlan();
                    newPlan.setClinicalExam(finalExam);
                    newPlan.setCreatedAt(LocalDateTime.now());
                    return newPlan;
                });

        plan.setTreatmentGoal(form.getTreatmentGoal());
        plan.setDietPlan(form.getDietPlan());
        plan.setExercisePlan(form.getExercisePlan());
        plan.setGlucoseMonitoringPlan(form.getGlucoseMonitoringPlan());
        treatmentPlanRepository.save(plan);

        // 4. Lưu Chỉ định & Kết quả xét nghiệm
        labOrderRepository.findByClinicalExamination_ClinicalExamId(examId)
                .ifPresent(labOrderRepository::delete);

        if (form.getLabTestIds() != null && !form.getLabTestIds().isEmpty()) {
            LabOrder labOrder = new LabOrder();
            labOrder.setLabOrderId("LBO-" + System.currentTimeMillis() + "-" + new Random().nextInt(1000));
            labOrder.setClinicalExamination(exam);
            labOrder.setStatus("Completed");
            labOrder = labOrderRepository.save(labOrder);

            Map<String, Map<String, Object>> simulatedResults = new HashMap<>();
            if (form.getLabResultsJson() != null && !form.getLabResultsJson().trim().isEmpty()) {
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    List<Map<String, Object>> list = mapper.readValue(form.getLabResultsJson(),
                            new TypeReference<List<Map<String, Object>>>() {
                            });
                    for (Map<String, Object> item : list) {
                        String testId = (String) item.get("testId");
                        simulatedResults.put(testId, item);
                    }
                } catch (Exception e) {
                    // Ignore
                }
            }

            Patient patient = exam.getPatient();
            PatientType matchedType = null;
            if (Boolean.TRUE.equals(form.getIsPregnant())) {
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

            for (String testId : form.getLabTestIds()) {
                LabTestCatalog test = labTestCatalogRepository.findById(testId).orElse(null);
                if (test != null) {
                    LabResult result = new LabResult();
                    result.setLabResultId("LBR-" + System.currentTimeMillis() + "-" + new Random().nextInt(1000));
                    result.setLabOrder(labOrder);
                    result.setLabTest(test);
                    result.setStatus("Completed");

                    BigDecimal value = BigDecimal.valueOf(4.8);
                    String flag = "NORMAL";

                    Optional<IndicatorThreshold> thresholdOpt = Optional.empty();
                    if (matchedType != null) {
                        thresholdOpt = indicatorThresholdRepository.findByLabTest_LabTestIdAndPatientType_PatientTypeId(
                                testId, matchedType.getPatientTypeId());
                    }
                    if (thresholdOpt.isEmpty()) {
                        List<IndicatorThreshold> thresholds = indicatorThresholdRepository
                                .findByLabTest_LabTestId(testId);
                        if (!thresholds.isEmpty()) {
                            thresholdOpt = Optional.of(thresholds.get(0));
                        }
                    }

                    String range = "N/A";
                    BigDecimal dbMin = null;
                    BigDecimal dbMax = null;

                    if (thresholdOpt.isPresent()) {
                        dbMin = thresholdOpt.get().getMinValue();
                        dbMax = thresholdOpt.get().getMaxValue();
                        range = dbMin + " - " + dbMax;
                    }

                    if (simulatedResults.containsKey(testId)) {
                        Map<String, Object> simInfo = simulatedResults.get(testId);
                        value = BigDecimal.valueOf(Double.parseDouble(simInfo.get("val").toString()));

                        if (dbMin != null && dbMax != null) {
                            if (value.compareTo(dbMin) < 0)
                                flag = "LOW";
                            else if (value.compareTo(dbMax) > 0)
                                flag = "HIGH";
                            else
                                flag = "NORMAL";
                        } else {
                            flag = "NORMAL";
                        }
                    } else {
                        value = BigDecimal.valueOf(0.0);
                        flag = "NORMAL";
                    }

                    result.setResultValue(value);
                    result.setReferenceRange(range);
                    result.setFlag(flag);
                    labResultRepository.save(result);
                }
            }
        }

        // 5. Lưu đơn thuốc (Clear cũ trước)
        prescriptionRepository.findByClinicalExamination_ClinicalExamId(examId)
                .ifPresent(prescriptionRepository::delete);

        if (form.getPrescriptionJson() != null && !form.getPrescriptionJson().trim().isEmpty()) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                List<Map<String, Object>> lines = mapper.readValue(form.getPrescriptionJson(),
                        new TypeReference<List<Map<String, Object>>>() {
                        });

                if (!lines.isEmpty()) {
                    Prescription presc = new Prescription();
                    presc.setPrescriptionId("PRC-" + System.currentTimeMillis() + "-" + new Random().nextInt(1000));
                    presc.setClinicalExamination(exam);
                    presc.setCreatedAt(LocalDateTime.now());
                    presc = prescriptionRepository.save(presc);

                    for (Map<String, Object> line : lines) {
                        String medId = (String) line.get("medId");
                        Medication med = medicationRepository.findById(medId).orElse(null);
                        if (med != null) {
                            PrescriptionDetail detail = new PrescriptionDetail();
                            detail.setPrescriptionDetailId(
                                    "PRD-" + System.currentTimeMillis() + "-" + new Random().nextInt(1000));
                            detail.setPrescription(presc);
                            detail.setMedication(med);
                            Object durationObj = line.get("duration");
                            int duration = durationObj instanceof Integer ? (Integer) durationObj
                                    : Integer.parseInt(durationObj.toString());
                            detail.setDurationDays(duration);

                            Object qtyObj = line.get("quantity");
                            int quantity = qtyObj instanceof Integer ? (Integer) qtyObj
                                    : Integer.parseInt(qtyObj.toString());
                            detail.setTotalQuantity(quantity);

                            Object clientDosageObj = line.get("dosage");
                            String dosage = (clientDosageObj != null && !clientDosageObj.toString().trim().isEmpty()
                                    && !"Auto".equalsIgnoreCase(clientDosageObj.toString()))
                                            ? clientDosageObj.toString()
                                            : calculateDosage(quantity, duration, med.getForm());
                            detail.setDosage(dosage);

                            Object medPlanObj = line.get("medicationPlan");
                            detail.setMedicationPlan(medPlanObj != null ? medPlanObj.toString() : "");

                            Object startDateObj = line.get("startDate");
                            if (startDateObj != null && !startDateObj.toString().trim().isEmpty()) {
                                detail.setStartDate(LocalDate.parse(startDateObj.toString()));
                            }
                            Object endDateObj = line.get("endDate");
                            if (endDateObj != null && !endDateObj.toString().trim().isEmpty()) {
                                detail.setEndDate(LocalDate.parse(endDateObj.toString()));
                            }

                            detail.setPrescriptionTimings(new ArrayList<>());
                            detail = prescriptionDetailRepository.save(detail);

                            String timingText = (String) line.get("timingText");
                            if (timingText != null && !timingText.trim().isEmpty()) {
                                String[] parts = timingText.split(",\\s*");
                                for (String part : parts) {
                                    if (part.trim().isEmpty()) {
                                        continue;
                                    }
                                    final String singleTimingText = part.trim();
                                    MedicationTiming timing = medicationTimingRepository
                                            .findByTimingName(singleTimingText)
                                            .orElseGet(() -> {
                                                MedicationTiming newTiming = new MedicationTiming();
                                                newTiming.setTimingName(singleTimingText);
                                                return medicationTimingRepository.save(newTiming);
                                            });

                                    PrescriptionTiming pTiming = new PrescriptionTiming();
                                    pTiming.setPrescriptionDetail(detail);
                                    pTiming.setTiming(timing);
                                    prescriptionTimingRepository.save(pTiming);
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException("Error deserializing prescription JSON for draft: " + e.getMessage(), e);
            }
        }
    }

    private String generateClinicalExamId() {
        String examId = null;
        Random random = new Random();
        do {
            String number = "00000" + random.nextInt(1000000);
            examId = "EX" + number.substring(number.length() - 6);
        } while (clinicalExaminationRepository.existsById(examId));
        return examId;
    }

    private Map<String, Object> createLogExam(String examId) {
        ClinicalExamination exam = clinicalExaminationRepository.findById(examId).orElse(null);
        if (exam == null)
            return null;

        Map<String, Object> logExam = new HashMap<>();
        logExam.put("clinicalExamId", exam.getClinicalExamId());
        logExam.put("status", exam.getStatus());
        logExam.put("medicalHistory", exam.getMedicalHistory());
        logExam.put("diagnosisNote", exam.getDiagnosisNote());
        logExam.put("cancelReason", exam.getCancelReason());
        logExam.put("nextAppointment", exam.getNextAppointment() != null ? exam.getNextAppointment().toString() : null);
        logExam.put("examDate", exam.getExamDate() != null ? exam.getExamDate().toString() : null);

        List<ExamSymptom> symptoms = examSymptomRepository.findAll().stream()
                .filter(s -> s.getId().getClinicalExamId().equals(examId))
                .collect(java.util.stream.Collectors.toList());
        if (!symptoms.isEmpty()) {
            List<Map<String, String>> symList = new ArrayList<>();
            for (ExamSymptom s : symptoms) {
                Map<String, String> sMap = new HashMap<>();
                sMap.put("symptomName", s.getSymptom().getSymptomName());
                sMap.put("note", s.getNote());
                symList.add(sMap);
            }
            logExam.put("symptoms", symList);
        }

        TreatmentPlan plan = treatmentPlanRepository.findByClinicalExam_ClinicalExamId(examId).orElse(null);
        if (plan != null) {
            Map<String, Object> planMap = new HashMap<>();
            planMap.put("treatmentGoal", plan.getTreatmentGoal());
            planMap.put("dietPlan", plan.getDietPlan());
            planMap.put("exercisePlan", plan.getExercisePlan());
            planMap.put("glucoseMonitoringPlan", plan.getGlucoseMonitoringPlan());
            logExam.put("treatmentPlan", planMap);
        }

        Prescription prescription = prescriptionRepository.findByClinicalExamination_ClinicalExamId(examId)
                .orElse(null);
        if (prescription != null) {
            List<PrescriptionDetail> details = prescriptionDetailRepository
                    .findByPrescription_PrescriptionId(prescription.getPrescriptionId());
            List<Map<String, Object>> prescList = new ArrayList<>();
            for (PrescriptionDetail d : details) {
                Map<String, Object> dMap = new HashMap<>();
                dMap.put("medicationName", d.getMedication().getMedicationName());
                dMap.put("dosage", d.getDosage());
                dMap.put("durationDays", d.getDurationDays());
                dMap.put("totalQuantity", d.getTotalQuantity());
                dMap.put("medicationPlan", d.getMedicationPlan());

                List<String> timings = new ArrayList<>();
                if (d.getPrescriptionTimings() != null) {
                    for (PrescriptionTiming pt : d.getPrescriptionTimings()) {
                        timings.add(pt.getTiming().getTimingName());
                    }
                }
                dMap.put("timings", timings);

                prescList.add(dMap);
            }
            logExam.put("prescription", prescList);
        }

        return logExam;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrescriptionLineDTO> getPrescriptionLines(String examId) {
        List<PrescriptionLineDTO> list = new ArrayList<>();
        List<PrescriptionDetail> details = prescriptionDetailRepository.findByClinicalExamIdWithDetails(examId);
        if (details == null) {
            return list;
        }
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

    // =========================================================================
    // TAB-BASED STEP METHODS
    // =========================================================================

    @Override
    @Transactional
    public void saveStep1(String examId, ExamStep1Form form, String doctorId) {
        Map<String, Object> oldExamMap = createLogExam(examId);
        ClinicalExamination exam = clinicalExaminationRepository.findById(examId)
                .orElseThrow(() -> new EntityNotFoundException("Ca kham khong ton tai: " + examId));
        if (form.getMedicalHistory() != null && !form.getMedicalHistory().trim().isEmpty()) {
            exam.setMedicalHistory(form.getMedicalHistory());
        }
        if (!"Completed".equalsIgnoreCase(exam.getStatus())) {
            exam.setStatus("InProgress");
        }
        exam = clinicalExaminationRepository.save(exam);

        // Save symptoms (clear old ones first)
        examSymptomRepository.deleteById_ClinicalExamId(examId);
        examSymptomRepository.flush();
        if (form.getSymptomIds() != null) {
            Map<String, String> comments = form.getSymptomComments();
            for (String symId : form.getSymptomIds()) {
                SymptomsCatalog symptom = symptomsCatalogRepository.findById(symId).orElse(null);
                if (symptom != null) {
                    ExamSymptom es = new ExamSymptom();
                    es.setId(new ExamSymptomId(examId, symId));
                    es.setClinicalExamination(exam);
                    es.setSymptom(symptom);
                    if (comments != null)
                        es.setNote(comments.getOrDefault(symId, ""));
                    examSymptomRepository.save(es);
                }
            }
        }

        systemLogService.saveLogWithObject(doctorId, "UPDATE_MEDICAL_RECORD", "MedicalRecord", examId,
                "Bác sĩ cập nhật tiền sử bệnh và triệu chứng", oldExamMap, createLogExam(examId), "SUCCESS");
    }

    @Override
    @Transactional
    public void saveStep2(String examId, ExamStep2Form form, PatientType matchedType, List<LabTestCatalog> testCatalog,
            String doctorId) {
        Map<String, Object> oldExamMap = createLogExam(examId);
        ClinicalExamination exam = clinicalExaminationRepository.findById(examId)
                .orElseThrow(() -> new EntityNotFoundException("Ca kham khong ton tai: " + examId));

        if (form.getDiagnosisNote() != null) {
            exam.setDiagnosisNote(form.getDiagnosisNote());
            clinicalExaminationRepository.save(exam);
        }

        // Pregnancy flag is stored in session; nothing specific to persist for
        // isPregnant alone
        // If orderLabs is requested, create lab order + results
        if (Boolean.TRUE.equals(form.getOrderLabs()) && testCatalog != null && !testCatalog.isEmpty()) {

            // Delete existing lab order and results
            labOrderRepository.findByClinicalExamination_ClinicalExamId(examId).ifPresent(existing -> {
                labResultRepository
                        .deleteAll(labResultRepository.findByLabOrder_ClinicalExamination_ClinicalExamId(examId));
                labOrderRepository.delete(existing);
            });

            LabOrder labOrder = new LabOrder();
            labOrder.setLabOrderId("LBO-" + System.currentTimeMillis() + "-" + new Random().nextInt(1000));
            labOrder.setClinicalExamination(exam);
            labOrder.setStatus("Completed");
            labOrder = labOrderRepository.save(labOrder);

            Random random = new Random();
            for (LabTestCatalog test : testCatalog) {
                LabResult result = new LabResult();
                result.setLabResultId("LBR-" + System.currentTimeMillis() + "-" + random.nextInt(1000));
                result.setLabOrder(labOrder);
                result.setLabTest(test);
                result.setStatus("Completed");

                Optional<IndicatorThreshold> thresholdOpt = Optional.empty();
                if (matchedType != null) {
                    thresholdOpt = indicatorThresholdRepository.findByLabTest_LabTestIdAndPatientType_PatientTypeId(
                            test.getLabTestId(), matchedType.getPatientTypeId());
                }
                String range = "N/A";
                BigDecimal dbMin = null, dbMax = null;
                if (thresholdOpt.isPresent()) {
                    dbMin = thresholdOpt.get().getMinValue();
                    dbMax = thresholdOpt.get().getMaxValue();
                    range = dbMin + " - " + dbMax;
                }
                result.setReferenceRange(range);

                BigDecimal value;
                String flag = "NORMAL";
                if (dbMin != null && dbMax != null) {
                    double span = dbMax.subtract(dbMin).doubleValue();
                    double mid = dbMin.add(dbMax).doubleValue() / 2;
                    double v = mid + (random.nextDouble() * span * 0.6 - span * 0.3);
                    value = BigDecimal.valueOf(Math.round(v * 100.0) / 100.0);
                    if (value.compareTo(dbMin) < 0)
                        flag = "LOW";
                    else if (value.compareTo(dbMax) > 0)
                        flag = "HIGH";
                } else {
                    value = BigDecimal.ZERO;
                }
                result.setResultValue(value);
                result.setFlag(flag);
                labResultRepository.save(result);
            }
        }

        systemLogService.saveLogWithObject(doctorId, "UPDATE_MEDICAL_RECORD", "MedicalRecord", examId,
                "Bác sĩ cập nhật chỉ định cận lâm sàng", oldExamMap, createLogExam(examId), "SUCCESS");
    }

    @Override
    @Transactional
    public void saveStep3(String examId, ExamStep3Form form, String doctorId) {
        Map<String, Object> oldExamMap = createLogExam(examId);
        ClinicalExamination exam = clinicalExaminationRepository.findById(examId)
                .orElseThrow(() -> new EntityNotFoundException("Ca kham khong ton tai: " + examId));
        if (form.getNextAppointment() != null && !form.getNextAppointment().trim().isEmpty()) {
            exam.setNextAppointment(LocalDate.parse(form.getNextAppointment()).atStartOfDay());
        } else {
            exam.setNextAppointment(null);
        }
        if (!"Completed".equalsIgnoreCase(exam.getStatus())) {
            exam.setStatus("InProgress");
        }
        exam = clinicalExaminationRepository.save(exam);

        final ClinicalExamination finalExam = exam;
        TreatmentPlan plan = treatmentPlanRepository.findByClinicalExam_ClinicalExamId(examId)
                .orElseGet(() -> {
                    TreatmentPlan p = new TreatmentPlan();
                    p.setClinicalExam(finalExam);
                    p.setCreatedAt(LocalDateTime.now());
                    return p;
                });
        plan.setTreatmentGoal(form.getTreatmentGoal());
        plan.setDietPlan(form.getDietPlan());
        plan.setExercisePlan(form.getExercisePlan());
        plan.setGlucoseMonitoringPlan(form.getGlucoseMonitoringPlan());
        treatmentPlanRepository.save(plan);

        systemLogService.saveLogWithObject(doctorId, "UPDATE_MEDICAL_RECORD", "MedicalRecord", examId,
                "Bác sĩ cập nhật chẩn đoán và hướng điều trị", oldExamMap, createLogExam(examId), "SUCCESS");
    }

    @Override
    @Transactional
    public void completeExamination(String examId, List<PrescriptionLineDTO> prescriptionLines, String doctorId) {
        ClinicalExamination exam = clinicalExaminationRepository.findById(examId)
                .orElseThrow(() -> new EntityNotFoundException("Ca kham khong ton tai: " + examId));
        Map<String, Object> oldExamMap = createLogExam(exam.getClinicalExamId());
        exam.setStatus("Completed");

        clinicalExaminationRepository.save(exam);

        // Clear old prescription
        prescriptionRepository.findByClinicalExamination_ClinicalExamId(examId)
                .ifPresent(old -> {
                    prescriptionDetailRepository.deleteAll(
                            prescriptionDetailRepository.findByPrescription_PrescriptionId(old.getPrescriptionId()));
                    prescriptionRepository.delete(old);
                });

        if (prescriptionLines != null && !prescriptionLines.isEmpty()) {
            Prescription presc = new Prescription();
            presc.setPrescriptionId("PRC-" + System.currentTimeMillis() + "-" + new Random().nextInt(1000));
            presc.setClinicalExamination(exam);
            presc.setCreatedAt(LocalDateTime.now());
            presc = prescriptionRepository.saveAndFlush(presc);

            for (PrescriptionLineDTO line : prescriptionLines) {
                Medication med = medicationRepository.findById(line.getMedId()).orElse(null);
                if (med == null)
                    continue;

                int duration = line.getDuration() != null ? line.getDuration() : 0;
                int quantity = line.getQuantity() != null ? line.getQuantity() : 0;
                if (quantity == 0 && duration > 0 && line.getDosagePerDose() != null) {
                    // timing count as doses per day
                    int timingCount = (line.getTiming() != null && !line.getTiming().isEmpty())
                            ? line.getTiming().size()
                            : 1;
                    quantity = (int) Math.ceil(line.getDosagePerDose() * timingCount * duration);
                }

                PrescriptionDetail detail = new PrescriptionDetail();
                detail.setPrescriptionDetailId("PRD-" + System.currentTimeMillis() + "-" + new Random().nextInt(1000));
                detail.setPrescription(presc);
                detail.setMedication(med);
                detail.setDurationDays(duration);
                detail.setTotalQuantity(quantity);
                detail.setDosage(calculateDosage(quantity, duration, med.getForm()));
                detail.setMedicationPlan(line.getMedicationPlan() != null ? line.getMedicationPlan() : "");
                if (line.getStartDate() != null && !line.getStartDate().isEmpty()) {
                    detail.setStartDate(LocalDate.parse(line.getStartDate()));
                }
                if (line.getEndDate() != null && !line.getEndDate().isEmpty()) {
                    detail.setEndDate(LocalDate.parse(line.getEndDate()));
                }
                detail.setPrescriptionTimings(new ArrayList<>());
                detail = prescriptionDetailRepository.saveAndFlush(detail);

                if (line.getTiming() != null) {
                    for (String timingName : line.getTiming()) {
                        if (timingName == null || timingName.trim().isEmpty())
                            continue;
                        final String tName = timingName.trim();
                        MedicationTiming mt = medicationTimingRepository.findByTimingName(tName)
                                .orElseGet(() -> {
                                    MedicationTiming newMt = new MedicationTiming();
                                    newMt.setTimingName(tName);
                                    return medicationTimingRepository.save(newMt);
                                });
                        PrescriptionTiming pt = new PrescriptionTiming();
                        pt.setPrescriptionDetail(detail);
                        pt.setTiming(mt);
                        prescriptionTimingRepository.saveAndFlush(pt);
                    }
                }
            }
        }

        // Lock old reminders and regenerate
        List<Reminder> oldReminders = reminderRepository.findByClinicalExamination_ClinicalExamId(examId);
        if (oldReminders != null) {
            oldReminders.forEach(r -> r.setLockStatus(true));
            reminderRepository.saveAll(oldReminders);
        }
        medicationSchedualeService.generateReminder(examId);
        appointmentSchedule.generateAppointmentReminder(examId);

        systemLogService.saveLogWithObject(doctorId, "COMPLETE_MEDICAL_RECORD", "MedicalRecord", examId,
                "Bác sĩ hoàn thành bệnh án", oldExamMap, createLogExam(examId), "SUCCESS");
    }
}
