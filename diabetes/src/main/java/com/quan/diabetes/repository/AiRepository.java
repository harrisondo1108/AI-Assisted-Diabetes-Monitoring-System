package com.quan.diabetes.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import com.fasterxml.jackson.databind.ObjectMapper;

@Repository
public class AiRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public String getGeneralRecord(String patientId) {
        String sql = "SELECT p.FullName, p.PhoneNumber, p.Address, p.Dob, p.Gender, " +
                     "p.Height, p.Weight, p.Bloodgroup, p.PermanentMedicalHistory, p.AllergyNotes, " +
                     "pr.BreakfastTime, pr.LunchTime, pr.DinnerTime, pr.WakeUpTime, pr.SleepTime " +
                     "FROM Patient p " +
                     "LEFT JOIN PatientRoutine pr ON p.UserID = pr.UserID " +
                     "WHERE p.UserID = ?";
        List<Map<String, Object>> result = jdbcTemplate.queryForList(sql, patientId);
        return formatResult(result, "Hồ sơ bệnh án chung");
    }

    public String getClinicalExamination(String patientId) {
        String sql = "SELECT TOP 3 ce.ExamDate, ce.DiagnosisNote, ce.NextAppointment, " +
                     "(SELECT STRING_AGG(sc.SymptomName, ', ') FROM ExamSymptom es " +
                     "JOIN Symptoms_Catalog sc ON es.SymptomID = sc.SymptomID " +
                     "WHERE es.ClinicalExamID = ce.ClinicalExamID) as Symptoms " +
                     "FROM ClinicalExamination ce " +
                     "WHERE ce.PatientID = ? " +
                     "ORDER BY ce.ExamDate DESC";
        List<Map<String, Object>> result = jdbcTemplate.queryForList(sql, patientId);
        return formatResult(result, "Lịch sử khám lâm sàng");
    }

    public String getTreatmentPlan(String patientId) {
        String sql = "SELECT TOP 3 ce.ExamDate, tp.TreatmentGoal, tp.DietPlan, tp.ExercisePlan, tp.GlucoseMonitoringPlan " +
                     "FROM TreatmentPlan tp " +
                     "JOIN ClinicalExamination ce ON tp.ClinicalExamID = ce.ClinicalExamID " +
                     "WHERE ce.PatientID = ? " +
                     "ORDER BY tp.CreatedAt DESC";
        List<Map<String, Object>> result = jdbcTemplate.queryForList(sql, patientId);
        return formatResult(result, "Kế hoạch điều trị và dặn dò");
    }

    public String getLabResults(String patientId) {
        String sql = "SELECT TOP 10 lr.ResultValue, lr.ReferenceRange, lr.Flag, lc.TestName, lc.Unit, ce.ExamDate " +
                     "FROM LabResult lr " +
                     "JOIN LabOrder lo ON lr.LabOrderID = lo.LabOrderID " +
                     "JOIN ClinicalExamination ce ON lo.ClinicalExamID = ce.ClinicalExamID " +
                     "JOIN Lab_Test_Catalog lc ON lr.LabTestID = lc.LabTestID " +
                     "WHERE ce.PatientID = ? " +
                     "ORDER BY ce.ExamDate DESC";
        List<Map<String, Object>> result = jdbcTemplate.queryForList(sql, patientId);
        return formatResult(result, "Kết quả xét nghiệm");
    }

    public String getPrescriptions(String patientId) {
        String sql = "SELECT p.CreatedAt, m.MedicationName, m.Concentration, m.UsageInstruction, " +
                     "pd.Dosage, pd.TotalQuantity, pd.DurationDays, pd.MedicationPlan, pd.StartDate, pd.EndDate " +
                     "FROM Prescription p " +
                     "JOIN PrescriptionDetail pd ON p.PrescriptionID = pd.PrescriptionID " +
                     "JOIN Medication m ON pd.MedicationID = m.MedicationID " +
                     "JOIN ClinicalExamination ce ON p.ClinicalExamID = ce.ClinicalExamID " +
                     "WHERE ce.PatientID = ? " +
                     "ORDER BY p.CreatedAt DESC";
        List<Map<String, Object>> result = jdbcTemplate.queryForList(sql, patientId);
        return formatResult(result, "Lịch sử dùng thuốc và đơn thuốc");
    }

    private String formatResult(List<Map<String, Object>> resultList, String title) {
        if (resultList == null || resultList.isEmpty()) {
            return "{\"title\": \"" + title + "\", \"data\": []}";
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            // Wrap the data and title in a single Map for clean JSON output
            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("title", title);
            responseMap.put("data", resultList);
            // Convert to JSON string
            return mapper.writeValueAsString(responseMap);
        } catch (Exception e) {
            return "{\"title\": \"" + title + "\", \"error\": \"Không thể chuyển đổi dữ liệu thành JSON\"}";
        }
    }
}
