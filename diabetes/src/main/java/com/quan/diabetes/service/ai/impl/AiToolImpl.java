package com.quan.diabetes.service.ai.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.quan.diabetes.dto.aitool.ClinicalExaminationDto;
import com.quan.diabetes.dto.aitool.LabResultDto;
import com.quan.diabetes.dto.aitool.PatientProfileDto;
import com.quan.diabetes.dto.aitool.TreatmentPlanDto;
import com.quan.diabetes.dto.reminder.PrescriptionReminderDto;
import com.quan.diabetes.entity.ClinicalExamination;
import com.quan.diabetes.entity.Patient;
import com.quan.diabetes.entity.TreatmentPlan;
import com.quan.diabetes.repository.ClinicalExaminationRepository;
import com.quan.diabetes.repository.ExamSymptomRepository;
import com.quan.diabetes.repository.LabResultRepository;
import com.quan.diabetes.repository.PatientRepository;
import com.quan.diabetes.repository.PrescriptionDetailRepository;
import com.quan.diabetes.repository.TreatmentPlanRepository;
import com.quan.diabetes.service.ai.AiTool;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AiToolImpl implements AiTool {

    private final PatientRepository patientRepository;
    private final ClinicalExaminationRepository clinicalExaminationRepository;
    private final TreatmentPlanRepository treatmentPlanRepository;
    private final LabResultRepository labResultRepository;
    private final PrescriptionDetailRepository prescriptionDetailRepository;
    private final ExamSymptomRepository examSymptomRepository;
    private final ObjectMapper objectMapper;

    public AiToolImpl(PatientRepository patientRepository,
                      ClinicalExaminationRepository clinicalExaminationRepository,
                      TreatmentPlanRepository treatmentPlanRepository,
                      LabResultRepository labResultRepository,
                      PrescriptionDetailRepository prescriptionDetailRepository,
                      ExamSymptomRepository examSymptomRepository) {
        this.patientRepository = patientRepository;
        this.clinicalExaminationRepository = clinicalExaminationRepository;
        this.treatmentPlanRepository = treatmentPlanRepository;
        this.labResultRepository = labResultRepository;
        this.prescriptionDetailRepository = prescriptionDetailRepository;
        this.examSymptomRepository = examSymptomRepository;

        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    @Transactional(readOnly = true)
    public String getGeneralRecord(String patientId) {
        Optional<Patient> patientOpt = patientRepository.findById(patientId);
        if (patientOpt.isEmpty()) {
            return formatResult(null, "Hồ sơ bệnh án chung");
        }
        Patient p = patientOpt.get();
        PatientProfileDto dto = new PatientProfileDto(
                p.getFullName(),
                p.getGender(),
                p.getHeight(),
                p.getWeight(),
                p.getBloodgroup(),
                p.getPermanentMedicalHistory(),
                p.getAllergyNotes()
        );
        return formatResult(List.of(dto), "Hồ sơ bệnh án chung");
    }

    @Override
    @Transactional(readOnly = true)
    public String getClinicalExamination(String patientId) {
        Optional<ClinicalExamination> examOpt = clinicalExaminationRepository.findFirstByPatient_UserIdOrderByExamDateDesc(patientId);
        if (examOpt.isEmpty()) {
            return formatResult(null, "Lịch sử khám lâm sàng");
        }
        
        ClinicalExamination ce = examOpt.get();
        
        List<String> symptoms = examSymptomRepository.findSymptomNamesByClinicalExamId(ce.getClinicalExamId());
        // Let's rely on LabResultRepository for lab results
        List<LabResultDto> labResults = labResultRepository.findByLabOrder_ClinicalExamination_ClinicalExamId(ce.getClinicalExamId())
                .stream().map(lr -> new LabResultDto(
                        lr.getLabTest().getTestName(),
                        lr.getResultValue(),
                        lr.getReferenceRange(),
                        lr.getLabTest().getUnit(),
                        lr.getFlag(),
                        lr.getLabOrder().getClinicalExamination().getExamDate()
                )).collect(Collectors.toList());

        // Prescriptions
        List<PrescriptionReminderDto> prescriptions = prescriptionDetailRepository.findByClinicalExamIdWithDetails(ce.getClinicalExamId())
                .stream().map(pd -> {
                    String timingNames = null;
                    if (pd.getPrescriptionTimings() != null && !pd.getPrescriptionTimings().isEmpty()) {
                        timingNames = pd.getPrescriptionTimings().stream()
                                .map(pt -> pt.getTiming().getTimingName())
                                .collect(Collectors.joining(", "));
                    }
                    return new PrescriptionReminderDto(
                            pd.getPrescription().getClinicalExamination().getPatient().getUserId(),
                            pd.getPrescription().getClinicalExamination().getClinicalExamId(),
                            pd.getMedication().getMedicationName(),
                            pd.getDosage(),
                            pd.getStartDate(),
                            pd.getEndDate(),
                            pd.getMedication().getForm(),
                            pd.getMedication().getAdministrationRoute(),
                            pd.getMedication().getUsageInstruction(),
                            timingNames,
                            pd.getMedicationPlan(),
                            pd.getPrescription().getClinicalExamination().getTreatmentPlan()
                    );
                }).distinct().collect(Collectors.toList());

        // Treatment Plan
        TreatmentPlanDto treatmentPlanDto = null;
        if (ce.getTreatmentPlan() != null) {
            treatmentPlanDto = new TreatmentPlanDto(
                    ce.getTreatmentPlan().getTreatmentGoal(),
                    ce.getTreatmentPlan().getDietPlan(),
                    ce.getTreatmentPlan().getExercisePlan(),
                    ce.getTreatmentPlan().getGlucoseMonitoringPlan(),
                    ce.getTreatmentPlan().getCreatedAt()
            );
        }

        ClinicalExaminationDto dto = new ClinicalExaminationDto(
                ce.getExamDate(),
                ce.getDoctor() != null && ce.getDoctor().getProfile() != null ? ce.getDoctor().getProfile().getFullName() : null,
                ce.getDiagnosisNote(),
                ce.getNextAppointment(),
                symptoms,
                labResults,
                prescriptions,
                treatmentPlanDto,
                ce.getStatus(),
                ce.getCancelReason()
        );

        return formatResult(List.of(dto), "Lịch sử khám lâm sàng");
    }

    @Override
    @Transactional(readOnly = true)
    public String getTreatmentPlan(String patientId) {
        Optional<ClinicalExamination> examOpt = clinicalExaminationRepository.findFirstByPatient_UserIdOrderByExamDateDesc(patientId);
        if (examOpt.isEmpty()) {
            return formatResult(null, "Kế hoạch điều trị và dặn dò");
        }
        ClinicalExamination ce = examOpt.get();
        if (ce.getTreatmentPlan() == null) {
            return formatResult(List.of(), "Kế hoạch điều trị và dặn dò");
        }
        TreatmentPlan tp = ce.getTreatmentPlan();
        TreatmentPlanDto dto = new TreatmentPlanDto(
                tp.getTreatmentGoal(),
                tp.getDietPlan(),
                tp.getExercisePlan(),
                tp.getGlucoseMonitoringPlan(),
                tp.getCreatedAt()
        );
        return formatResult(List.of(dto), "Kế hoạch điều trị và dặn dò");
    }

    @Override
    @Transactional(readOnly = true)
    public String getLabResults(String patientId) {
        Optional<ClinicalExamination> examOpt = clinicalExaminationRepository.findFirstByPatient_UserIdOrderByExamDateDesc(patientId);
        if (examOpt.isEmpty()) {
            return formatResult(null, "Kết quả xét nghiệm");
        }
        List<LabResultDto> labResults = labResultRepository.findByLabOrder_ClinicalExamination_ClinicalExamId(examOpt.get().getClinicalExamId())
                .stream().map(lr -> new LabResultDto(
                        lr.getLabTest().getTestName(),
                        lr.getResultValue(),
                        lr.getReferenceRange(),
                        lr.getLabTest().getUnit(),
                        lr.getFlag(),
                        lr.getLabOrder().getClinicalExamination().getExamDate()
                )).collect(Collectors.toList());
        return formatResult(labResults, "Kết quả xét nghiệm");
    }

    @Override
    @Transactional(readOnly = true)
    public String getPrescriptions(String patientId) {
        Optional<ClinicalExamination> examOpt = clinicalExaminationRepository.findFirstByPatient_UserIdOrderByExamDateDesc(patientId);
        if (examOpt.isEmpty()) {
            return formatResult(null, "Lịch sử dùng thuốc và đơn thuốc");
        }
        
        List<PrescriptionReminderDto> dtos = prescriptionDetailRepository.findByClinicalExamIdWithDetails(examOpt.get().getClinicalExamId())
                .stream().map(pd -> {
                    String timingNames = null;
                    if (pd.getPrescriptionTimings() != null && !pd.getPrescriptionTimings().isEmpty()) {
                        timingNames = pd.getPrescriptionTimings().stream()
                                .map(pt -> pt.getTiming().getTimingName())
                                .collect(Collectors.joining(", "));
                    }
                    return new PrescriptionReminderDto(
                            pd.getPrescription().getClinicalExamination().getPatient().getUserId(),
                            pd.getPrescription().getClinicalExamination().getClinicalExamId(),
                            pd.getMedication().getMedicationName(),
                            pd.getDosage(),
                            pd.getStartDate(),
                            pd.getEndDate(),
                            pd.getMedication().getForm(),
                            pd.getMedication().getAdministrationRoute(),
                            pd.getMedication().getUsageInstruction(),
                            timingNames,
                            pd.getMedicationPlan(),
                            pd.getPrescription().getClinicalExamination().getTreatmentPlan()
                    );
                }).distinct().collect(Collectors.toList());

        return formatResult(dtos, "Lịch sử dùng thuốc và đơn thuốc");
    }

    private String formatResult(List<?> resultList, String title) {
        if (resultList == null || resultList.isEmpty()) {
            return "{\"title\": \"" + title + "\", \"data\": []}";
        }
        try {
            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("title", title);
            responseMap.put("data", resultList);
            return objectMapper.writeValueAsString(responseMap);
        } catch (Exception e) {
            return "{\"title\": \"" + title + "\", \"error\": \"Không thể chuyển đổi dữ liệu thành JSON\"}";
        }
    }
}
