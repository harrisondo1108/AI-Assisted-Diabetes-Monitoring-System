package com.quan.diabetes.controller.patient;

import com.quan.diabetes.entity.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.beans.factory.annotation.Autowired;
import com.quan.diabetes.service.exam.DoctorRatingService;
import com.quan.diabetes.service.systemlog.SystemLogService;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
public class PatientMedicalHistoryController extends BasePatientController {

    @Autowired
    private DoctorRatingService doctorRatingService;

    @Autowired
    private SystemLogService systemLogService;

    @GetMapping("/patient/progress")
    public String progress(
            @RequestParam(name = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(name = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "2") int size,
            Model model, HttpSession session) {
        Patient patient = addCommonData(model, session, "progress");

        LocalDate today = LocalDate.now();
        List<ClinicalExamination> allExams = findExaminationsByPatient(patient).stream()
                .filter(exam -> {
                    if (exam.getStatus() != null && ("requested".equalsIgnoreCase(exam.getStatus()) || "pending".equalsIgnoreCase(exam.getStatus()))) {
                        return true;
                    }
                    return exam.getExamDate() != null && exam.getExamDate().toLocalDate().isEqual(today);
                })
                .collect(Collectors.toList());

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
                .filter(exam -> exam.getStatus() != null && ("completed".equalsIgnoreCase(exam.getStatus()) || "cancelled".equalsIgnoreCase(exam.getStatus())))
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

        List<String> examIds = pagedExams.stream()
                .map(ClinicalExamination::getClinicalExamId)
                .collect(Collectors.toList());
        Map<String, DoctorRating> ratingsMap = doctorRatingService.getRatingsForExams(examIds);

        model.addAttribute("examinations", pagedExams);
        model.addAttribute("labOrdersByExam", groupLabOrdersByExam(pagedExams, allLabOrders));
        model.addAttribute("labResultsByOrder", groupLabResultsByOrder(allLabOrders, allLabResults));
        model.addAttribute("prescriptionDetailsByExam", groupPrescriptionDetailsByExam(pagedExams, allPrescriptionDetails));
        model.addAttribute("treatmentPlansByExam", plansMap);
        model.addAttribute("ratingsMap", ratingsMap);
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
        if (exam.getStatus() == null || (!"completed".equalsIgnoreCase(exam.getStatus()) && !"cancelled".equalsIgnoreCase(exam.getStatus()))) {
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

    @PostMapping("/patient/request-exam")
    public String requestExam(
            @RequestParam("medicalHistory") String medicalHistory,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        Patient patient = getCurrentPatient(session);
        if (patient == null) {
            return "redirect:/login";
        }

        if (medicalHistory == null || medicalHistory.trim().isEmpty()) {
            systemLogService.saveLog(patient.getUserId(), "CREATE_MEDICAL_REQUEST", "MedicalRecord", patient.getUserId(), "Gửi yêu cầu khám thất bại (Lý do trống)", null, null, "FAILED");
            redirectAttributes.addFlashAttribute("requestErrorMessage", "Vui lòng nhập lý do khám hoặc triệu chứng của bạn.");
            return "redirect:/patient/progress";
        }

        try {
            clinicalExaminationService.requestExamination(patient.getUserId(), medicalHistory.trim());
            redirectAttributes.addFlashAttribute("requestSuccessMessage", "Gửi yêu cầu khám thành công! Vui lòng chờ bác sĩ duyệt.");
        } catch (Exception e) {
            String msg = e.getMessage();
            if ("PROFILE_INCOMPLETE".equals(msg)) {
                redirectAttributes.addFlashAttribute("profileIncomplete", true);
                systemLogService.saveLog(patient.getUserId(), "CREATE_MEDICAL_REQUEST", "MedicalRecord",
                        patient.getUserId(), "Gửi yêu cầu khám thất bại (Hồ sơ chưa đầy đủ)", null, null, "FAILED");
            } else {
                systemLogService.saveLog(patient.getUserId(), "CREATE_MEDICAL_REQUEST", "MedicalRecord",
                        patient.getUserId(), "Gửi yêu cầu khám thất bại: " + msg, null, null, "FAILED");
                redirectAttributes.addFlashAttribute("requestErrorMessage", msg);
            }
        }

        return "redirect:/patient/progress";
    }
}
