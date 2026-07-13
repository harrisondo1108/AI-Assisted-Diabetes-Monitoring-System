package com.quan.diabetes.service.exam.impl;

import com.quan.diabetes.dto.doctor.ClinicalExamForm;
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
            MedicationSchedualeService medicationSchedualeService) {
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
            String clinicalExamId = "";
            do {
                clinicalExamId = "EXM-" + System.currentTimeMillis() + "-" + new Random().nextInt(1000);
            } while (clinicalExaminationRepository.existsById(clinicalExamId));
            exam.setClinicalExamId(clinicalExamId);
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
            String clinicalExamId = "";
            do {
                clinicalExamId = "EXM-" + System.currentTimeMillis() + "-" + new Random().nextInt(1000);
            } while (clinicalExaminationRepository.existsById(clinicalExamId));
            exam.setClinicalExamId(clinicalExamId);
            exam.setPatient(patientRepository.findById(patientId)
                    .orElseThrow(() -> new EntityNotFoundException("Patient not found: " + patientId)));
            exam.setDoctor(userRepository.findById(doctorId)
                    .orElseThrow(() -> new EntityNotFoundException("Doctor not found: " + doctorId)));
            exam.setExamDate(LocalDateTime.now());
        }
        exam.setStatus("Cancelled");
        exam.setCancelReason(reason);
        exam.setDiagnosisNote(null);
        clinicalExaminationRepository.save(exam);
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
            String clinicalExamId = "";
            do {
                clinicalExamId = "EXM-" + System.currentTimeMillis() + "-" + new Random().nextInt(1000);
            } while (clinicalExaminationRepository.existsById(clinicalExamId));
            // code của quân
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

        // 4. Chỉ định & Kết quả xét nghiệm đã được lưu trực tiếp qua AJAX nên không cần xử lý ở đây.

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
            String clinicalExamId = "";
            do {
                clinicalExamId = "EXM-" + System.currentTimeMillis() + "-" + new Random().nextInt(1000);
            } while (clinicalExaminationRepository.existsById(clinicalExamId));
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

        // 4. Chỉ định & Kết quả xét nghiệm đã được lưu trực tiếp qua AJAX nên không cần xử lý ở đây.

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
        String clinicalExamId = "";
        do {
            clinicalExamId = "EXM-" + System.currentTimeMillis() + "-" + new Random().nextInt(1000);
        } while (clinicalExaminationRepository.existsById(clinicalExamId));
        exam.setClinicalExamId(clinicalExamId);
        exam.setPatient(patientRepository.findById(patientId)
                .orElseThrow(() -> new EntityNotFoundException("Patient not found: " + patientId)));
        exam.setDoctor(doctor);
        exam.setExamDate(LocalDateTime.now());
        exam.setStatus("Requested");
        exam.setMedicalHistory(medicalHistory);
        clinicalExaminationRepository.save(exam);
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
            String clinicalExamId = "";
            do {
                clinicalExamId = "EXM-" + System.currentTimeMillis() + "-" + new Random().nextInt(1000);
            } while (clinicalExaminationRepository.existsById(clinicalExamId));
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

        // 4. Chỉ định & Kết quả xét nghiệm đã được lưu trực tiếp qua AJAX nên không cần xử lý ở đây.

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
}
