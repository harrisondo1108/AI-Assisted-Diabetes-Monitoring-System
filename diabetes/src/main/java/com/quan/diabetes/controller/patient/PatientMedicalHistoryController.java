package com.quan.diabetes.controller.patient;

import com.quan.diabetes.entity.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
public class PatientMedicalHistoryController extends BasePatientController {

    @GetMapping("/patient/progress")
    public String progress(
            @RequestParam(name = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(name = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "2") int size,
            Model model, HttpSession session) {
        Patient patient = addCommonData(model, session, "progress");

        List<ClinicalExamination> allExams = findExaminationsByPatient(patient);
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            model.addAttribute("errorMessage", "Ngày bắt đầu (Từ ngày) không thể lớn hơn Ngày kết thúc (Đến ngày).");
            allExams = List.of();
        } else {
            if (startDate != null) {
                allExams = allExams.stream()
                        .filter(exam -> exam.getExamDate() != null && !exam.getExamDate().isBefore(startDate.atStartOfDay()))
                        .collect(Collectors.toList());
            }
            if (endDate != null) {
                allExams = allExams.stream()
                        .filter(exam -> exam.getExamDate() != null && !exam.getExamDate().isAfter(endDate.atTime(23, 59, 59)))
                        .collect(Collectors.toList());
            }
        }

        List<LabOrder> allLabOrders = findLabOrdersByPatient(patient);
        List<LabResult> allLabResults = findLabResultsByPatient(patient);
        List<PrescriptionDetail> allPrescriptionDetails = findPrescriptionDetailsByPatient(patient);

        Set<String> filteredExamIds = allExams.stream()
                .map(ClinicalExamination::getClinicalExamId)
                .collect(Collectors.toSet());

        List<LabOrder> filteredLabOrders = allLabOrders.stream()
                .filter(order -> order.getClinicalExamination() != null && filteredExamIds.contains(order.getClinicalExamination().getClinicalExamId()))
                .collect(Collectors.toList());

        Set<String> filteredOrderIds = filteredLabOrders.stream()
                .map(LabOrder::getLabOrderId)
                .collect(Collectors.toSet());
        List<LabResult> filteredLabResults = allLabResults.stream()
                .filter(result -> result.getLabOrder() != null && filteredOrderIds.contains(result.getLabOrder().getLabOrderId()))
                .collect(Collectors.toList());

        Set<String> prescriptionIds = prescriptionService.findAll()
                .stream()
                .filter(prescription -> prescription.getClinicalExamination() != null)
                .filter(prescription -> filteredExamIds.contains(prescription.getClinicalExamination().getClinicalExamId()))
                .map(Prescription::getPrescriptionId)
                .collect(Collectors.toSet());

        List<PrescriptionDetail> filteredPrescriptionDetails = allPrescriptionDetails.stream()
                .filter(detail -> detail.getPrescription() != null && prescriptionIds.contains(detail.getPrescription().getPrescriptionId()))
                .collect(Collectors.toList());

        int totalItems = allExams.size();
        int totalPages = (int) Math.ceil((double) totalItems / size);

        int start = Math.min(page * size, totalItems);
        int end = Math.min((page + 1) * size, totalItems);
        List<ClinicalExamination> pagedExams = (start < end) ? allExams.subList(start, end) : List.of();

        Map<String, TreatmentPlan> plansMap = groupTreatmentPlansByExam(pagedExams);

        model.addAttribute("examinations", pagedExams);
        model.addAttribute("labOrders", filteredLabOrders);
        model.addAttribute("labResults", filteredLabResults);
        model.addAttribute("prescriptionDetails", filteredPrescriptionDetails);
        model.addAttribute("labOrdersByExam", groupLabOrdersByExam(pagedExams, filteredLabOrders));
        model.addAttribute("labResultsByOrder", groupLabResultsByOrder(filteredLabOrders, filteredLabResults));
        model.addAttribute("prescriptionDetailsByExam", groupPrescriptionDetailsByExam(pagedExams, filteredPrescriptionDetails));
        model.addAttribute("treatmentPlansByExam", plansMap);
        model.addAttribute("abnormalResultCount", countAbnormalResults(filteredLabResults));
        model.addAttribute("completedOrderCount", countStatus(filteredLabOrders, "completed"));
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalItems", totalItems);
        model.addAttribute("pageSize", size);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        return "patient/progress";
    }

    @GetMapping("/patient/tests")
    public String tests() {
        return "redirect:/patient/progress";
    }

    @GetMapping("/patient/results")
    public String results() {
        return "redirect:/patient/progress";
    }

    @GetMapping("/patient/history")
    public String history(
            @RequestParam(name = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(name = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "5") int size,
            Model model, HttpSession session) {
        Patient patient = addCommonData(model, session, "history");

        List<ClinicalExamination> allExams = findExaminationsByPatient(patient).stream()
                .filter(exam -> exam.getStatus() != null && "completed".equalsIgnoreCase(exam.getStatus()))
                .collect(Collectors.toList());
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            model.addAttribute("errorMessage", "Ngày bắt đầu (Từ ngày) không thể lớn hơn Ngày kết thúc (Đến ngày).");
            allExams = List.of();
        } else {
            if (startDate != null) {
                allExams = allExams.stream()
                        .filter(exam -> exam.getExamDate() != null && !exam.getExamDate().isBefore(startDate.atStartOfDay()))
                        .collect(Collectors.toList());
            }
            if (endDate != null) {
                allExams = allExams.stream()
                        .filter(exam -> exam.getExamDate() != null && !exam.getExamDate().isAfter(endDate.atTime(23, 59, 59)))
                        .collect(Collectors.toList());
            }
        }

        int totalItems = allExams.size();
        int totalPages = (int) Math.ceil((double) totalItems / size);

        int start = Math.min(page * size, totalItems);
        int end = Math.min((page + 1) * size, totalItems);
        List<ClinicalExamination> pagedExams = (start < end) ? allExams.subList(start, end) : List.of();

        List<LabOrder> allLabOrders = findLabOrdersByPatient(patient);
        List<LabResult> allLabResults = findLabResultsByPatient(patient);
        List<PrescriptionDetail> allPrescriptionDetails = findPrescriptionDetailsByPatient(patient);

        Map<String, TreatmentPlan> plansMap = groupTreatmentPlansByExam(pagedExams);

        model.addAttribute("examinations", pagedExams);
        model.addAttribute("labOrdersByExam", groupLabOrdersByExam(pagedExams, allLabOrders));
        model.addAttribute("labResultsByOrder", groupLabResultsByOrder(allLabOrders, allLabResults));
        model.addAttribute("prescriptionDetailsByExam", groupPrescriptionDetailsByExam(pagedExams, allPrescriptionDetails));
        model.addAttribute("treatmentPlansByExam", plansMap);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalItems", totalItems);
        model.addAttribute("pageSize", size);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        return "patient/history";
    }

    @GetMapping("/patient/history/detail")
    public String historyDetail(
            @RequestParam("examId") String examId,
            Model model, HttpSession session) {
        Patient patient = addCommonData(model, session, "history");
        if (patient == null) {
            return "redirect:/login";
        }

        ClinicalExamination exam = clinicalExaminationService.findById(examId).orElse(null);
        if (exam == null || exam.getPatient() == null || !patient.getUserId().equals(exam.getPatient().getUserId())) {
            return "redirect:/patient/history";
        }
        if (exam.getStatus() == null || !"completed".equalsIgnoreCase(exam.getStatus())) {
            return "redirect:/patient/history";
        }

        List<LabOrder> allLabOrders = findLabOrdersByPatient(patient);
        List<LabResult> allLabResults = findLabResultsByPatient(patient);
        List<PrescriptionDetail> allPrescriptionDetails = findPrescriptionDetailsByPatient(patient);

        List<ClinicalExamination> examList = List.of(exam);
        
        TreatmentPlan plan = treatmentPlanService.findByClinicalExamId(examId).orElse(null);

        model.addAttribute("exam", exam);
        model.addAttribute("labOrders", groupLabOrdersByExam(examList, allLabOrders).get(examId));
        model.addAttribute("labResultsByOrder", groupLabResultsByOrder(allLabOrders, allLabResults));
        model.addAttribute("prescriptionDetails", groupPrescriptionDetailsByExam(examList, allPrescriptionDetails).get(examId));
        model.addAttribute("treatmentPlan", plan);

        return "patient/history-detail";
    }
}
