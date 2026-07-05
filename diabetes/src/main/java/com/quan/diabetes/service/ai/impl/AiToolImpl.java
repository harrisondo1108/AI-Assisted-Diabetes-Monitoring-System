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
            return "Danh mục: " + title + "\n(Không có dữ liệu)";
        }
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("Danh mục: ").append(title).append("\n");
            
            List<Map<String, Object>> mapList = objectMapper.convertValue(resultList, new com.fasterxml.jackson.core.type.TypeReference<List<Map<String, Object>>>() {});
            
            int index = 1;
            for (Map<String, Object> item : mapList) {
                if (mapList.size() > 1) {
                    sb.append("\n--- Bản ghi ").append(index++).append(" ---\n");
                } else {
                    sb.append("\n");
                }
                sb.append(formatMapToText(item, 0));
            }
            return sb.toString();
        } catch (Exception e) {
            return "Danh mục: " + title + "\n(Lỗi khi định dạng dữ liệu)";
        }
    }

    @SuppressWarnings("unchecked")
    private String formatMapToText(Map<String, Object> map, int indentLevel) {
        StringBuilder sb = new StringBuilder();
        StringBuilder indentBuilder = new StringBuilder();
        for (int i = 0; i < indentLevel; i++) indentBuilder.append("  ");
        String indent = indentBuilder.toString();
        
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            Object rawValue = entry.getValue();
            boolean isEmpty = (rawValue == null || String.valueOf(rawValue).trim().isEmpty() || "[]".equals(String.valueOf(rawValue)));
            
            String key = translateKey(entry.getKey());
            
            if (isEmpty) {
                sb.append(indent).append("- ").append(key).append(": Chưa cập nhật\n");
                continue;
            }
            
            if (entry.getValue() instanceof Map) {
                sb.append(indent).append("- ").append(key).append(":\n");
                sb.append(formatMapToText((Map<String, Object>) entry.getValue(), indentLevel + 1));
            } else if (entry.getValue() instanceof List) {
                List<?> list = (List<?>) entry.getValue();
                if (!list.isEmpty()) {
                    sb.append(indent).append("- ").append(key).append(":\n");
                    for (Object obj : list) {
                        if (obj instanceof Map) {
                            sb.append(formatMapToText((Map<String, Object>) obj, indentLevel + 1));
                            sb.append(indent).append("  ---\n");
                        } else {
                            sb.append(indent).append("  * ").append(obj.toString()).append("\n");
                        }
                    }
                }
            } else {
                sb.append(indent).append("- ").append(key).append(": ").append(entry.getValue().toString()).append("\n");
            }
        }
        return sb.toString();
    }

    private String translateKey(String key) {
        switch (key) {
            case "fullName": return "Họ và tên";
            case "gender": return "Giới tính";
            case "height": return "Chiều cao (cm)";
            case "weight": return "Cân nặng (kg)";
            case "bloodgroup": return "Nhóm máu";
            case "permanentMedicalHistory": return "Tiền sử bệnh lý";
            case "allergyNotes": return "Ghi chú dị ứng";
            case "examDate": return "Ngày khám";
            case "doctorName": return "Bác sĩ khám";
            case "diagnosisNote": return "Chẩn đoán";
            case "nextAppointment": return "Lịch tái khám";
            case "symptoms": return "Triệu chứng";
            case "labResults": return "Kết quả xét nghiệm";
            case "prescriptions": return "Đơn thuốc";
            case "treatmentPlan": return "Kế hoạch điều trị";
            case "status": return "Trạng thái";
            case "cancelReason": return "Lý do hủy";
            case "treatmentGoal": return "Mục tiêu điều trị";
            case "dietPlan": return "Chế độ ăn uống";
            case "exercisePlan": return "Chế độ tập luyện";
            case "glucoseMonitoringPlan": return "Kế hoạch đo đường huyết";
            case "createdAt": return "Ngày lập";
            case "testName": return "Tên xét nghiệm";
            case "resultValue": return "Kết quả";
            case "referenceRange": return "Chỉ số bình thường";
            case "unit": return "Đơn vị";
            case "flag": return "Đánh giá";
            case "patientId": return "Mã bệnh nhân";
            case "clinicalExamId": return "Mã lượt khám";
            case "medicationName": return "Tên thuốc";
            case "dosage": return "Liều lượng";
            case "startDate": return "Ngày bắt đầu";
            case "endDate": return "Ngày kết thúc";
            case "form": return "Dạng thuốc";
            case "administrationRoute": return "Đường dùng";
            case "usageInstruction": return "Hướng dẫn sử dụng";
            case "timings": return "Thời điểm dùng thuốc";
            case "medicationPlan": return "Kế hoạch dùng tự ghi";
            default:
                return key.substring(0, 1).toUpperCase() + key.substring(1).replaceAll("([a-z])([A-Z]+)", "$1 $2");
        }
    }
}
