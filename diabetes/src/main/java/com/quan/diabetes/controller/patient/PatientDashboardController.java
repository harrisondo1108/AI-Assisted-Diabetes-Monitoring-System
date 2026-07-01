package com.quan.diabetes.controller.patient;

import com.quan.diabetes.entity.*;
import com.quan.diabetes.dto.patient.MedicationReminderView;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class PatientDashboardController extends BasePatientController {

    @GetMapping({"/patient", "/patient/dashboard"})
    public String dashboard(Model model, HttpSession session) {
        Patient patient = addCommonData(model, session, "dashboard");

        List<ClinicalExamination> examinations = findExaminationsByPatient(patient);
        List<LabOrder> labOrders = findLabOrdersByPatient(patient);
        List<LabResult> labResults = findLabResultsByPatient(patient);
        List<AIReminder> aiReminders = findRemindersByPatient(patient);
        List<MedicationReminderView> medicationReminders = buildTodayMedicationReminders(patient);

        ClinicalExamination latestExam = examinations.isEmpty() ? null : examinations.get(0);

        TreatmentPlan latestTreatmentPlan = null;
        if (latestExam != null) {
            latestTreatmentPlan = treatmentPlanService.findByClinicalExamId(latestExam.getClinicalExamId()).orElse(null);
        }

        model.addAttribute("latestExam", latestExam);
        model.addAttribute("latestTreatmentPlan", latestTreatmentPlan);
        model.addAttribute("examinationCount", examinations.size());
        model.addAttribute("labOrderCount", labOrders.size());
        model.addAttribute("completedOrderCount", countStatus(labOrders, "completed"));
        model.addAttribute("abnormalResultCount", countAbnormalResults(labResults));

        model.addAttribute("latestHbA1c", getLatestResultValue(labResults, "hba1c", "N/A"));
        model.addAttribute("latestGlucose", getLatestResultValue(labResults, "glucose", "N/A"));
        model.addAttribute("latestHbA1cStatus", evaluateHbA1cStatus(labResults));
        model.addAttribute("latestGlucoseStatus", evaluateGlucoseStatus(labResults));

        model.addAttribute("bmi", calculateBmi(patient));
        model.addAttribute("bmiStatus", evaluateBmiStatus(patient));

        int riskScore = calculateRiskScore(labResults, patient);
        model.addAttribute("riskScore", riskScore);
        model.addAttribute("riskLevel", getRiskLevel(riskScore));
        model.addAttribute("riskBadgeClass", getRiskBadgeClass(riskScore));

        model.addAttribute("recentMedicationReminders", medicationReminders.stream()
                .filter(reminder -> !reminder.isPast())
                .limit(4)
                .collect(Collectors.toList()));

        model.addAttribute("dueMedicationReminderCount", medicationReminders.stream()
                .filter(MedicationReminderView::isDueNow)
                .count());

        model.addAttribute("todayMedicationReminderCount", medicationReminders.size());

        model.addAttribute("recentAiReminders", aiReminders.stream()
                .limit(3)
                .collect(Collectors.toList()));

        model.addAttribute("recentLabResults", labResults.stream().limit(5).collect(Collectors.toList()));
        model.addAttribute("recentLabOrders", labOrders.stream().limit(4).collect(Collectors.toList()));

        return "patient/dashboard";
    }

    @GetMapping("/patient/risk")
    public String risk(Model model, HttpSession session) {
        Patient patient = addCommonData(model, session, "risk");

        List<ClinicalExamination> examinations = findExaminationsByPatient(patient);
        List<LabResult> labResults = findLabResultsByPatient(patient);
        List<LabResult> abnormalResults = findAbnormalResults(labResults);

        int riskScore = calculateRiskScore(labResults, patient);

        ClinicalExamination latestExam = examinations.isEmpty() ? null : examinations.get(0);
        TreatmentPlan latestTreatmentPlan = null;
        if (latestExam != null) {
            latestTreatmentPlan = treatmentPlanService.findByClinicalExamId(latestExam.getClinicalExamId()).orElse(null);
        }

        model.addAttribute("riskScore", riskScore);
        model.addAttribute("riskLevel", getRiskLevel(riskScore));
        model.addAttribute("riskBadgeClass", getRiskBadgeClass(riskScore));
        model.addAttribute("riskDescription", getRiskDescription(riskScore));
        model.addAttribute("riskAdvice", getRiskAdvice(riskScore));

        model.addAttribute("latestHbA1c", getLatestResultValue(labResults, "hba1c", "N/A"));
        model.addAttribute("latestGlucose", getLatestResultValue(labResults, "glucose", "N/A"));
        model.addAttribute("latestHbA1cStatus", evaluateHbA1cStatus(labResults));
        model.addAttribute("latestGlucoseStatus", evaluateGlucoseStatus(labResults));

        model.addAttribute("bmi", calculateBmi(patient));
        model.addAttribute("bmiStatus", evaluateBmiStatus(patient));

        List<LabResult> highAbnormalResults = abnormalResults.stream()
                .filter(result -> {
                    if (result.getFlag() == null) return false;
                    String flag = result.getFlag().toLowerCase();
                    return flag.contains("high") || flag.contains("cao");
                })
                .limit(5)
                .collect(Collectors.toList());

        List<LabResult> recentLabResults = List.of();
        if (latestExam != null) {
            String latestExamId = latestExam.getClinicalExamId();
            recentLabResults = labResults.stream()
                    .filter(result -> result.getLabOrder() != null
                            && result.getLabOrder().getClinicalExamination() != null
                            && latestExamId.equals(result.getLabOrder().getClinicalExamination().getClinicalExamId()))
                    .collect(Collectors.toList());
        }

        model.addAttribute("examinationCount", examinations.size());
        model.addAttribute("abnormalResultCount", abnormalResults.size());
        model.addAttribute("abnormalResults", highAbnormalResults);
        model.addAttribute("recentLabResults", recentLabResults);
        model.addAttribute("latestTreatmentPlan", latestTreatmentPlan);

        return "patient/risk";
    }
}
