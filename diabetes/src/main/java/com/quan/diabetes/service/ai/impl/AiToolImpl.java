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
        String genderStr = "Chưa có thông tin";
        if (Boolean.TRUE.equals(p.getGender())) genderStr = "Nam";
        else if (Boolean.FALSE.equals(p.getGender())) genderStr = "Nữ";

        PatientProfileDto dto = new PatientProfileDto(
                p.getFullName(),
                genderStr,
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

        // Lấy toàn bộ xét nghiệm của bệnh nhân theo patientId đảm bảo không sót dữ liệu
        List<LabResultDto> labResults = labResultRepository.findByPatientIdWithDetails(patientId)
                .stream().map(lr -> new LabResultDto(
                        lr.getLabTest().getTestName(),
                        lr.getResultValue(),
                        lr.getReferenceRange(),
                        lr.getLabTest().getUnit(),
                        lr.getFlag(),
                        lr.getLabOrder().getClinicalExamination().getExamDate()
                )).collect(Collectors.toList());

        // Lấy toàn bộ đơn thuốc của bệnh nhân theo patientId đảm bảo không rớt bản ghi khi lệch ID lượt khám
        List<PrescriptionReminderDto> prescriptions = prescriptionDetailRepository.findByPatientIdWithDetails(patientId)
                .stream().map(pd -> {
                    String timingNames = null;
                    if (pd.getPrescriptionTimings() != null && !pd.getPrescriptionTimings().isEmpty()) {
                        timingNames = pd.getPrescriptionTimings().stream()
                                .map(pt -> pt.getTiming().getTimingName())
                                .collect(Collectors.joining(", "));
                    }
                    String pId = pd.getPrescription() != null && pd.getPrescription().getClinicalExamination() != null && pd.getPrescription().getClinicalExamination().getPatient() != null
                            ? pd.getPrescription().getClinicalExamination().getPatient().getUserId() : patientId;
                    String examId = pd.getPrescription() != null && pd.getPrescription().getClinicalExamination() != null
                            ? pd.getPrescription().getClinicalExamination().getClinicalExamId() : ce.getClinicalExamId();
                    com.quan.diabetes.entity.TreatmentPlan tp = pd.getPrescription() != null && pd.getPrescription().getClinicalExamination() != null
                            ? pd.getPrescription().getClinicalExamination().getTreatmentPlan() : ce.getTreatmentPlan();
                    return new PrescriptionReminderDto(
                            pId,
                            examId,
                            pd.getMedication().getMedicationName(),
                            pd.getDosage(),
                            pd.getStartDate(),
                            pd.getEndDate(),
                            pd.getMedication().getForm(),
                            pd.getMedication().getAdministrationRoute(),
                            pd.getMedication().getUsageInstruction(),
                            timingNames,
                            pd.getMedicationPlan(),
                            tp
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
        List<ClinicalExamination> exams = clinicalExaminationRepository.findByPatient_UserIdOrderByExamDateDesc(patientId);
        if (exams == null || exams.isEmpty()) {
            return formatResult(null, "Kế hoạch điều trị và dặn dò");
        }
        // Tìm lần khám gần nhất có Kế hoạch điều trị (TreatmentPlan) hoặc lời dặn bác sĩ
        ClinicalExamination latestWithPlan = exams.stream()
                .filter(ce -> ce.getTreatmentPlan() != null)
                .findFirst()
                .orElse(exams.get(0));

        TreatmentPlan tp = latestWithPlan.getTreatmentPlan();
        Map<String, Object> planMap = new java.util.LinkedHashMap<>();
        planMap.put("Ngày khám gần nhất", latestWithPlan.getExamDate() != null ? latestWithPlan.getExamDate().toString() : "Gần đây");
        if (latestWithPlan.getDiagnosisNote() != null && !latestWithPlan.getDiagnosisNote().isBlank()) {
            planMap.put("Chẩn đoán & Lời dặn bác sĩ", latestWithPlan.getDiagnosisNote());
        }
        if (tp != null) {
            planMap.put("Mục tiêu điều trị", tp.getTreatmentGoal() != null ? tp.getTreatmentGoal() : "Theo dõi và duy trì đường huyết ổn định");
            planMap.put("Chế độ dinh dưỡng", tp.getDietPlan() != null ? tp.getDietPlan() : "Ăn đủ chất, hạn chế tinh bột nhanh và đường");
            planMap.put("Chế độ tập luyện", tp.getExercisePlan() != null ? tp.getExercisePlan() : "Duy trì vận động nhẹ nhàng 30 phút/ngày");
            planMap.put("Kế hoạch theo dõi đường huyết", tp.getGlucoseMonitoringPlan() != null ? tp.getGlucoseMonitoringPlan() : "Đo đường huyết định kỳ theo hướng dẫn");
        } else {
            planMap.put("Mục tiêu điều trị", "Duy trì đường huyết HbA1c mục tiêu dưới 7.0%");
            planMap.put("Chế độ dinh dưỡng", "Ăn nhiều rau xanh, chọn ngũ cốc nguyên hạt, hạn chế đường ngọt");
            planMap.put("Chế độ tập luyện", "Tập thể dục đều đặn ít nhất 30 phút mỗi ngày, 5 ngày/tuần");
            planMap.put("Kế hoạch theo dõi đường huyết", "Đo đường huyết lúc đói và sau ăn 2h theo chỉ định bác sĩ");
        }
        if (latestWithPlan.getNextAppointment() != null) {
            planMap.put("Lịch hẹn tái khám tiếp theo", latestWithPlan.getNextAppointment().toString());
        }

        return formatResult(List.of(planMap), "Kế hoạch điều trị và dặn dò");
    }

    @Override
    @Transactional(readOnly = true)
    public String getLabResults(String patientId) {
        java.util.List<LabResultDto> labResults = labResultRepository.findByPatientIdWithDetails(patientId)
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
        List<ClinicalExamination> exams = clinicalExaminationRepository.findByPatient_UserIdOrderByExamDateDesc(patientId);
        if (exams == null || exams.isEmpty()) {
            return "DANH SÁCH THUỐC TRONG ĐƠN THUỐC CỦA BỆNH NHÂN:\n(Bệnh nhân hiện chưa có lượt khám nào trong hồ sơ y tế)";
        }
        ClinicalExamination targetExam = exams.get(0);
        java.util.List<com.quan.diabetes.entity.PrescriptionDetail> details = prescriptionDetailRepository.findByClinicalExamIdWithDetails(targetExam.getClinicalExamId());
        if (details.isEmpty() && exams.size() > 1) {
            for (ClinicalExamination ce : exams) {
                java.util.List<com.quan.diabetes.entity.PrescriptionDetail> ceDetails = prescriptionDetailRepository.findByClinicalExamIdWithDetails(ce.getClinicalExamId());
                if (!ceDetails.isEmpty()) {
                    targetExam = ce;
                    details = ceDetails;
                    break;
                }
            }
        }

        final ClinicalExamination finalTargetExam = targetExam;
        java.util.List<PrescriptionReminderDto> dtos = details.stream().map(pd -> {
            String timingNames = null;
            if (pd.getPrescriptionTimings() != null && !pd.getPrescriptionTimings().isEmpty()) {
                timingNames = pd.getPrescriptionTimings().stream()
                        .map(pt -> pt.getTiming().getTimingName())
                        .collect(Collectors.joining(", "));
            }
            String pId = pd.getPrescription() != null && pd.getPrescription().getClinicalExamination() != null && pd.getPrescription().getClinicalExamination().getPatient() != null
                    ? pd.getPrescription().getClinicalExamination().getPatient().getUserId() : patientId;
            String examId = pd.getPrescription() != null && pd.getPrescription().getClinicalExamination() != null
                    ? pd.getPrescription().getClinicalExamination().getClinicalExamId() : finalTargetExam.getClinicalExamId();
            com.quan.diabetes.entity.TreatmentPlan tp = pd.getPrescription() != null && pd.getPrescription().getClinicalExamination() != null
                    ? pd.getPrescription().getClinicalExamination().getTreatmentPlan() : finalTargetExam.getTreatmentPlan();
            return new PrescriptionReminderDto(
                    pId,
                    examId,
                    pd.getMedication().getMedicationName(),
                    pd.getDosage(),
                    pd.getStartDate(),
                    pd.getEndDate(),
                    pd.getMedication().getForm(),
                    pd.getMedication().getAdministrationRoute(),
                    pd.getMedication().getUsageInstruction(),
                    timingNames,
                    pd.getMedicationPlan(),
                    tp
            );
        }).collect(Collectors.toList());

        java.util.List<PrescriptionReminderDto> distinctDtos = dtos.stream().distinct().collect(Collectors.toList());
        if (distinctDtos.isEmpty()) {
            return "DANH SÁCH THUỐC TRONG ĐƠN THUỐC CỦA BỆNH NHÂN:\n(Phiên khám gần nhất hiện chưa có thuốc nào được kê)";
        }

        String examDateStr = targetExam.getExamDate() != null ? formatFriendlyValue("examDate", targetExam.getExamDate().toString()) : "Gần nhất";
        StringBuilder sb = new StringBuilder();
        sb.append("DANH SÁCH THUỐC TRONG ĐƠN THUỐC CỦA BỆNH NHÂN (TỪ PHIÊN KHÁM GẦN NHẤT - ").append(examDateStr).append("):\n");
        int idx = 1;
        for (PrescriptionReminderDto dto : distinctDtos) {
            String medName = dto.getMedicationName() != null ? dto.getMedicationName() : "Chưa cập nhật";
            String dosageVN = translateMedicalTerms(dto.getDosage() != null ? dto.getDosage() : "Theo chỉ định bác sĩ");
            String formVN = translateMedicalTerms(dto.getForm() != null ? dto.getForm() : "Chưa cập nhật");
            String routeVN = translateMedicalTerms(dto.getAdministrationRoute() != null ? dto.getAdministrationRoute() : "");
            sb.append(idx++).append(". Tên thuốc: ").append(medName).append("\n");
            sb.append("   - Liều lượng: ").append(dosageVN).append("\n");
            sb.append("   - Dạng thuốc / Đường dùng: ").append(formVN).append(" / ").append(routeVN).append("\n");
            if (dto.getTimingName() != null && !dto.getTimingName().isBlank()) {
                sb.append("   - Thời điểm uống trong ngày: ").append(dto.getTimingName()).append("\n");
            }
            if (dto.getUsageInstruction() != null && !dto.getUsageInstruction().isBlank()) {
                sb.append("   - Hướng dẫn sử dụng: ").append(dto.getUsageInstruction()).append("\n");
            }
            if (dto.getStartDate() != null) {
                sb.append("   - Thời gian sử dụng: Từ ngày ").append(dto.getStartDate()).append(dto.getEndDate() != null ? " đến ngày " + dto.getEndDate() : "").append("\n");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    private String translateMedicalTerms(String text) {
        if (text == null) return "";
        return text
                .replaceAll("(?i)\\btablets/lần\\b", "viên/lần")
                .replaceAll("(?i)\\btablet/lần\\b", "viên/lần")
                .replaceAll("(?i)\\bpills/lần\\b", "viên/lần")
                .replaceAll("(?i)\\bpill/lần\\b", "viên/lần")
                .replaceAll("(?i)\\btablets\\b", "viên")
                .replaceAll("(?i)\\btablet\\b", "Viên nén")
                .replaceAll("(?i)\\bcapsule\\b", "Viên nhộng")
                .replaceAll("(?i)\\bsubcutaneous\\b", "Tiêm dưới da")
                .replaceAll("(?i)\\boral\\b", "Đường uống")
                .replaceAll("(?i)\\binjection\\b", "Tiêm")
                .replaceAll("(?i)\\binvenous\\b", "Tiêm tĩnh mạch")
                .replaceAll("(?i)\\bintramuscular\\b", "Tiêm bắp")
                .replaceAll("(?i)\\bsyrup\\b", "Siro");
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
                sb.append(indent).append("• ").append(key).append(": Chưa có thông tin\n");
                continue;
            }
            
            if (entry.getValue() instanceof Map) {
                sb.append(indent).append("• ").append(key).append(":\n");
                sb.append(formatMapToText((Map<String, Object>) entry.getValue(), indentLevel + 1));
            } else if (entry.getValue() instanceof List) {
                List<?> list = (List<?>) entry.getValue();
                if (!list.isEmpty()) {
                    sb.append(indent).append("• ").append(key).append(":\n");
                    for (Object obj : list) {
                        if (obj instanceof Map) {
                            sb.append(formatMapToText((Map<String, Object>) obj, indentLevel + 1));
                            sb.append(indent).append("  ---\n");
                        } else {
                            sb.append(indent).append("  * ").append(formatFriendlyValue(key, obj)).append("\n");
                        }
                    }
                }
            } else {
                sb.append(indent).append("• ").append(key).append(": ").append(formatFriendlyValue(key, entry.getValue())).append("\n");
            }
        }
        return sb.toString();
    }

    private String formatFriendlyValue(String key, Object rawValue) {
        if (rawValue == null) return "Chưa có thông tin";
        String str = rawValue.toString().trim();
        if ("Giới tính".equals(key)) {
            if ("true".equalsIgnoreCase(str) || "Nam".equalsIgnoreCase(str)) return "Nam";
            if ("false".equalsIgnoreCase(str) || "Nữ".equalsIgnoreCase(str)) return "Nữ";
            return str;
        }
        if (str.matches(".*\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}.*")) {
            try {
                if (str.contains("T00:00")) {
                    java.time.LocalDate ld = java.time.LocalDate.parse(str.substring(0, 10));
                    return "Ngày " + ld.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                }
                java.time.LocalDateTime ldt = java.time.LocalDateTime.parse(str.length() > 19 ? str.substring(0, 19) : str);
                return ldt.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy (HH:mm)"));
            } catch (Exception e) {
                // fallback
            }
        }
        if (str.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
            try {
                java.time.LocalDate ld = java.time.LocalDate.parse(str);
                return ld.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            } catch (Exception e) {
                // fallback
            }
        }
        return str;
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
