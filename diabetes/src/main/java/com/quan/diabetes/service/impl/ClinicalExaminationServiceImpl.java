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
            LabTestCatalogRepository labTestCatalogRepository) {
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
        if (form.getSymptomIds() != null) {
            for (String symId : form.getSymptomIds()) {
                SymptomsCatalog symptom = symptomsCatalogRepository.findById(symId).orElse(null);
                if (symptom != null) {
                    ExamSymptom examSymptom = new ExamSymptom();
                    examSymptom.setId(new ExamSymptomId(examId, symId));
                    examSymptom.setClinicalExamination(exam);
                    examSymptom.setSymptom(symptom);
                    examSymptomRepository.save(examSymptom);
                }
            }
        }

        final ClinicalExamination finalExam = exam;
        // 3. Lưu Kế hoạch điều trị (Cập nhật nếu đã có, hoặc tạo mới nếu chưa để tránh lỗi UNIQUE KEY constraint)
        TreatmentPlan plan = treatmentPlanRepository.findByClinicalExamination_ClinicalExamId(examId)
                .orElseGet(() -> {
                    TreatmentPlan newPlan = new TreatmentPlan();
                    newPlan.setPlanId("PLN-" + System.currentTimeMillis() + "-" + new Random().nextInt(1000));
                    newPlan.setClinicalExamination(finalExam);
                    newPlan.setCreatedAt(LocalDateTime.now());
                    return newPlan;
                });

        plan.setTreatmentGoal(form.getTreatmentGoal());
        plan.setDietPlan(form.getDietPlan());
        plan.setExercisePlan(form.getExercisePlan());
        plan.setGlucoseMonitoringPlan(form.getGlucoseMonitoringPlan());
        plan.setMedicationPlan("N/A");
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

            for (String testId : form.getLabTestIds()) {
                LabTestCatalog test = labTestCatalogRepository.findById(testId).orElse(null);
                if (test != null) {
                    LabResult result = new LabResult();
                    result.setLabResultId("LBR-" + System.currentTimeMillis() + "-" + new Random().nextInt(1000));
                    result.setLabOrder(labOrder);
                    result.setLabTest(test);
                    result.setStatus("Completed");

                    // Mô phỏng giá trị xét nghiệm và ngưỡng tương ứng
                    BigDecimal value = BigDecimal.valueOf(4.8);
                    String flag = "NORMAL";
                    String range = "3.9 - 5.6";

                    String testName = test.getTestName().toLowerCase();
                    Random rand = new Random();
                    if (testName.contains("fasting blood glucose")) {
                        value = BigDecimal.valueOf(Math.round((4.0 + rand.nextDouble() * 5.5) * 10.0) / 10.0);
                        range = "3.9 - 5.6";
                        if (value.doubleValue() > 5.6) flag = "HIGH";
                    } else if (testName.contains("hba1c")) {
                        value = BigDecimal.valueOf(Math.round((4.5 + rand.nextDouble() * 4.0) * 10.0) / 10.0);
                        range = "4.0 - 5.6";
                        if (value.doubleValue() > 5.6) flag = "HIGH";
                    } else if (testName.contains("ogtt")) {
                        value = BigDecimal.valueOf(Math.round((6.5 + rand.nextDouble() * 5.0) * 10.0) / 10.0);
                        range = "< 7.8";
                        if (value.doubleValue() >= 7.8) flag = "HIGH";
                    } else if (testName.contains("creatinine")) {
                        value = BigDecimal.valueOf(Math.round(60 + rand.nextInt(75)));
                        range = "62 - 115";
                        if (value.doubleValue() > 115) flag = "HIGH";
                    } else if (testName.contains("cholesterol")) {
                        value = BigDecimal.valueOf(Math.round((4.0 + rand.nextDouble() * 2.5) * 10.0) / 10.0);
                        range = "< 5.2";
                        if (value.doubleValue() >= 5.2) flag = "HIGH";
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
                            detail.setDosage((String) line.get("dosage"));
                            
                            Object durationObj = line.get("duration");
                            detail.setDurationDays(durationObj instanceof Integer ? (Integer) durationObj : Integer.parseInt(durationObj.toString()));
                            
                            Object qtyObj = line.get("quantity");
                            detail.setTotalQuantity(qtyObj instanceof Integer ? (Integer) qtyObj : Integer.parseInt(qtyObj.toString()));
                            
                            detail.setPrescriptionTimings(new ArrayList<>());
                            detail = prescriptionDetailRepository.save(detail);

                            // Lưu Timing cho đơn thuốc
                            String timingText = (String) line.get("timingText");
                            if (timingText != null && !timingText.trim().isEmpty()) {
                                final String finalTimingText = timingText;
                                MedicationTiming timing = medicationTimingRepository.findByTimingName(finalTimingText)
                                        .orElseGet(() -> {
                                            MedicationTiming newTiming = new MedicationTiming();
                                            newTiming.setTimingName(finalTimingText);
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
            } catch (Exception e) {
                throw new RuntimeException("Error deserializing prescription JSON: " + e.getMessage(), e);
            }
        }
    }
}
