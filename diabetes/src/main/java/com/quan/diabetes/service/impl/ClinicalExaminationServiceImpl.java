package com.quan.diabetes.service.impl;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import com.quan.diabetes.entity.*;
import com.quan.diabetes.repository.*;
import com.quan.diabetes.service.ClinicalExaminationService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
            IndicatorThresholdRepository indicatorThresholdRepository) {
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
            exam.setClinicalExamId("EXM-" + System.currentTimeMillis() + "-" + new Random().nextInt(1000));
            exam.setPatient(patientRepository.findById(patientId)
                    .orElseThrow(() -> new EntityNotFoundException("Patient not found: " + patientId)));
            exam.setDoctor(userRepository.findById(doctorId)
                    .orElseThrow(() -> new EntityNotFoundException("Doctor not found: " + doctorId)));
            exam.setExamDate(LocalDateTime.now());
        }
        exam.setStatus("InProgress");
        clinicalExaminationRepository.save(exam);
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
            exam.setClinicalExamId("EXM-" + System.currentTimeMillis() + "-" + new Random().nextInt(1000));
            exam.setPatient(patientRepository.findById(patientId)
                    .orElseThrow(() -> new EntityNotFoundException("Patient not found: " + patientId)));
            exam.setDoctor(userRepository.findById(doctorId)
                    .orElseThrow(() -> new EntityNotFoundException("Doctor not found: " + doctorId)));
            exam.setExamDate(LocalDateTime.now());
        }
        exam.setStatus("Cancelled");
        exam.setMedicalHistory("Cancelled: " + reason);
        exam.setDiagnosisNote("Cancelled");
        clinicalExaminationRepository.save(exam);
    }

    @Override
    @Transactional
    public void submitExamination(String patientId, com.quan.diabetes.dto.ClinicalExamForm form, String doctorId) {
        ClinicalExamination exam = clinicalExaminationRepository
                .findFirstByPatient_UserIdAndDoctor_UserIdAndStatusIn(
                        patientId, doctorId, List.of("Pending", "InProgress"))
                .orElse(null);

        if (exam == null) {
            exam = new ClinicalExamination();
            exam.setClinicalExamId("EXM-" + System.currentTimeMillis() + "-" + new Random().nextInt(1000));
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
        exam.setStatus("Completed");
        exam = clinicalExaminationRepository.save(exam);

        String examId = exam.getClinicalExamId();

        // 2. Lưu Triệu chứng (Clear cũ trước)
        examSymptomRepository.deleteById_ClinicalExamId(examId);
        examSymptomRepository.flush();

        Map<String, String> symptomComments = new HashMap<>();
        if (form.getSymptomCommentsJson() != null && !form.getSymptomCommentsJson().trim().isEmpty()) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                symptomComments = mapper.readValue(form.getSymptomCommentsJson(),
                        new TypeReference<Map<String, String>>() {});
            } catch (Exception e) {
                // Ignore or log
            }
        }

        if (form.getSymptomIds() != null) {
            for (String symId : form.getSymptomIds()) {
                SymptomsCatalog symptom = symptomsCatalogRepository.findById(symId).orElse(null);
                if (symptom != null) {
                    ExamSymptom examSymptom = new ExamSymptom();
                    examSymptom.setId(new ExamSymptomId(examId, symId));
                    examSymptom.setClinicalExamination(exam);
                    examSymptom.setSymptom(symptom);
                    if (symptomComments.containsKey(symId)) {
                        examSymptom.setNote(symptomComments.get(symId));
                    }
                    examSymptomRepository.save(examSymptom);
                }
            }
        }

        final ClinicalExamination finalExam = exam;
        // 3. Lưu Kế hoạch điều trị (Cập nhật nếu đã có, hoặc tạo mới nếu chưa để tránh lỗi UNIQUE KEY constraint)
        TreatmentPlan plan = treatmentPlanRepository.findByClinicalExamination_ClinicalExamId(examId)
                .orElseGet(() -> {
                    TreatmentPlan newPlan = new TreatmentPlan();
                    newPlan.setClinicalExamination(finalExam);
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
                            new TypeReference<List<Map<String, Object>>>() {});
                    for (Map<String, Object> item : list) {
                        String testId = (String) item.get("testId");
                        simulatedResults.put(testId, item);
                    }
                } catch (Exception e) {
                    // Ignore or log
                }
            }

            Patient patient = exam.getPatient();
            int age = 0;
            if (patient != null && patient.getDob() != null) {
                age = java.time.Period.between(patient.getDob(), java.time.LocalDate.now()).getYears();
            }
            final int finalAge = age;
            PatientType matchedType = patientTypeRepository.findAll().stream()
                    .filter(t -> (t.getMinAge() == null || finalAge >= t.getMinAge()) && (t.getMaxAge() == null || finalAge <= t.getMaxAge()))
                    .findFirst()
                    .orElse(null);

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
                            testId, matchedType.getPatientTypeId()
                        );
                    }
                    if (thresholdOpt.isEmpty()) {
                        List<IndicatorThreshold> thresholds = indicatorThresholdRepository.findByLabTest_LabTestId(testId);
                        if (!thresholds.isEmpty()) {
                            thresholdOpt = Optional.of(thresholds.get(0));
                        }
                    }

                    String range = "0 - 5.2";
                    BigDecimal dbMin = null;
                    BigDecimal dbMax = null;

                    if (thresholdOpt.isPresent()) {
                        dbMin = thresholdOpt.get().getMinValue();
                        dbMax = thresholdOpt.get().getMaxValue();
                        range = dbMin + " - " + dbMax;
                    } else {
                        String testName = test.getTestName().toLowerCase();
                        if (testId.equals("LAB003") || testName.contains("ogtt") || testName.contains("dung nạp")) {
                            range = "70 - 140";
                        } else if (testId.equals("LAB001") || testName.contains("fpg") || testName.contains("lúc đói") || testName.contains("fasting")) {
                            range = "70 - 100";
                        } else if (testId.equals("LAB002") || testName.contains("hba1c")) {
                            range = "4.0 - 5.6";
                        } else if (testId.equals("LAB004") || testName.contains("ngẫu nhiên") || testName.contains("random")) {
                            range = "70 - 140";
                        }
                    }

                    if (simulatedResults.containsKey(testId)) {
                        Map<String, Object> simInfo = simulatedResults.get(testId);
                        value = BigDecimal.valueOf(Double.parseDouble(simInfo.get("val").toString()));
                        flag = (String) simInfo.get("flag");
                    } else {
                        // Fallback generator in case JS submit didn't include it
                        Random rand = new Random();
                        String testName = test.getTestName().toLowerCase();
                        if (dbMin != null && dbMax != null) {
                            boolean isHigh = rand.nextBoolean();
                            double minV = dbMin.doubleValue();
                            double maxV = dbMax.doubleValue();
                            if (isHigh) {
                                double scale = maxV > 20 ? (maxV * 0.4) : 4.0;
                                value = BigDecimal.valueOf(Math.round((maxV + rand.nextDouble() * scale) * 10.0) / 10.0);
                                flag = "HIGH";
                            } else {
                                double diff = maxV - minV;
                                value = BigDecimal.valueOf(Math.round((minV + rand.nextDouble() * diff) * 10.0) / 10.0);
                                flag = "NORMAL";
                            }
                        } else {
                            if (testId.equals("LAB001") || testName.contains("fpg") || testName.contains("lúc đói") || testName.contains("fasting")) {
                                value = BigDecimal.valueOf(Math.round((70.0 + rand.nextDouble() * 80.0) * 10.0) / 10.0);
                                if (value.doubleValue() > 100.0) flag = "HIGH";
                            } else if (testId.equals("LAB002") || testName.contains("hba1c")) {
                                value = BigDecimal.valueOf(Math.round((4.0 + rand.nextDouble() * 4.0) * 10.0) / 10.0);
                                if (value.doubleValue() > 5.6) flag = "HIGH";
                            } else if (testId.equals("LAB003") || testName.contains("ogtt") || testName.contains("dung nạp")) {
                                value = BigDecimal.valueOf(Math.round((75.0 + rand.nextDouble() * 100.0) * 10.0) / 10.0);
                                if (value.doubleValue() >= 140.0) flag = "HIGH";
                            } else if (testId.equals("LAB004") || testName.contains("ngẫu nhiên") || testName.contains("random")) {
                                value = BigDecimal.valueOf(Math.round((75.0 + rand.nextDouble() * 100.0) * 10.0) / 10.0);
                                if (value.doubleValue() >= 140.0) flag = "HIGH";
                            } else if (testName.contains("creatinine")) {
                                value = BigDecimal.valueOf(Math.round(60 + rand.nextInt(75)));
                                if (value.doubleValue() > 115) flag = "HIGH";
                            } else if (testName.contains("cholesterol")) {
                                value = BigDecimal.valueOf(Math.round((4.0 + rand.nextDouble() * 2.5) * 10.0) / 10.0);
                                if (value.doubleValue() >= 5.2) flag = "HIGH";
                            }
                        }
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
                        new TypeReference<List<Map<String, Object>>>() {});

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
                            detail.setPrescriptionDetailId("PRD-" + System.currentTimeMillis() + "-" + new Random().nextInt(1000));
                            detail.setPrescription(presc);
                            detail.setMedication(med);
                            Object durationObj = line.get("duration");
                            int duration = durationObj instanceof Integer ? (Integer) durationObj : Integer.parseInt(durationObj.toString());
                            detail.setDurationDays(duration);
                            
                            Object qtyObj = line.get("quantity");
                            int quantity = qtyObj instanceof Integer ? (Integer) qtyObj : Integer.parseInt(qtyObj.toString());
                            detail.setTotalQuantity(quantity);
                            
                            Object clientDosageObj = line.get("dosage");
                            String dosage = (clientDosageObj != null && !clientDosageObj.toString().trim().isEmpty() && !"Auto".equalsIgnoreCase(clientDosageObj.toString())) 
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
                                    MedicationTiming timing = medicationTimingRepository.findByTimingName(singleTimingText)
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
            exam.setClinicalExamId("EXM-" + System.currentTimeMillis() + "-" + new Random().nextInt(1000));
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
}
