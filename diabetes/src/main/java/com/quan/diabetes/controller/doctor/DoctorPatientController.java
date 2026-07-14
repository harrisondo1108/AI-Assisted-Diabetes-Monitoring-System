package com.quan.diabetes.controller.doctor;

import com.quan.diabetes.entity.*;
import com.quan.diabetes.repository.*;

import com.quan.diabetes.service.exam.ClinicalExaminationService;
import com.quan.diabetes.service.user.PatientService;
import com.quan.diabetes.service.user.ProfileService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/doctor")
public class DoctorPatientController {

    private final ClinicalExaminationService clinicalExaminationService;
    private final PatientService patientService;
    private final ProfileService profileService;
    private final LabResultRepository labResultRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final PrescriptionDetailRepository prescriptionDetailRepository;
    private final ExamSymptomRepository examSymptomRepository;
    private final ClinicalExaminationRepository clinicalExaminationRepository;

    public DoctorPatientController(
            ClinicalExaminationService clinicalExaminationService,
            PatientService patientService,
            ProfileService profileService,
            LabResultRepository labResultRepository,
            PrescriptionRepository prescriptionRepository,
            PrescriptionDetailRepository prescriptionDetailRepository,
            ExamSymptomRepository examSymptomRepository,
            ClinicalExaminationRepository clinicalExaminationRepository) {
        this.clinicalExaminationService = clinicalExaminationService;
        this.patientService = patientService;
        this.profileService = profileService;
        this.labResultRepository = labResultRepository;
        this.prescriptionRepository = prescriptionRepository;
        this.prescriptionDetailRepository = prescriptionDetailRepository;
        this.examSymptomRepository = examSymptomRepository;
        this.clinicalExaminationRepository = clinicalExaminationRepository;
    }

    @GetMapping("/history")
    public String patientHistoryPage(
            @RequestParam(value = "patientId", required = false) String patientId,
            @RequestParam(value = "from", required = false) String from,
            @RequestParam(value = "fromDate", required = false) String fromDate,
            @RequestParam(value = "toDate", required = false) String toDate,
            @RequestParam(value = "page", defaultValue = "1") int page,
            HttpSession session,
            Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !"DOC".equalsIgnoreCase(loggedInUser.getRole().getRoleId())) {
            return "redirect:/login";
        }
        model.addAttribute("loggedInUser", loggedInUser);

        String doctorId = loggedInUser.getUserId();
        Profile profile = profileService.findById(doctorId).orElse(null);
        model.addAttribute("doctorProfile", profile);

        // Check if there is an in-progress exam for this doctor in the database
        Optional<ClinicalExamination> activeExam = clinicalExaminationRepository
                .findFirstByDoctor_UserIdAndStatus(doctorId, "InProgress");
        model.addAttribute("hasActiveExam", activeExam.isPresent());

        if (activeExam.isPresent()) {
            patientId = activeExam.get().getPatient().getUserId();
            session.setAttribute("selectedPatientId", patientId);
        } else {
            if (patientId != null) {
                session.setAttribute("selectedPatientId", patientId);
            }
        }

        String selectedPatientId = (String) session.getAttribute("selectedPatientId");
        if (selectedPatientId == null) {
            return "redirect:/doctor/queue";
        }

        Patient patient = patientService.findById(selectedPatientId).orElse(null);
        if (patient == null) {
            return "redirect:/doctor/queue";
        }

        java.util.Map<String, Object> patientMap = new java.util.HashMap<>();
        patientMap.put("userId", patient.getUserId());
        patientMap.put("fullName", patient.getFullName());
        patientMap.put("imageUrl", patient.getImageUrl());
        model.addAttribute("patient", patientMap);

        // Nạp lịch sử các ca khám (Timeline) của bệnh nhân này (Chỉ lấy Completed hoặc
        // Cancelled)
        List<ClinicalExamination> timeline = clinicalExaminationService.findByPatientId(selectedPatientId).stream()
                .filter(e -> "Completed".equalsIgnoreCase(e.getStatus()) || "Cancelled".equalsIgnoreCase(e.getStatus()))
                .collect(Collectors.toList());

        // Validate logic ngày ở Backend
        if (fromDate != null && toDate != null && !fromDate.trim().isEmpty() && !toDate.trim().isEmpty()) {
            if (fromDate.compareTo(toDate) > 0) {
                model.addAttribute("dateError", "Ngày kết thúc phải lớn hơn hoặc bằng ngày bắt đầu.");
                fromDate = null;
                toDate = null;
            }
        }

        // Thực hiện lọc danh sách theo khoảng ngày hợp lệ
        if (fromDate != null && !fromDate.trim().isEmpty()) {
            try {
                java.time.LocalDate start = java.time.LocalDate.parse(fromDate);
                timeline = timeline.stream()
                        .filter(e -> e.getExamDate() != null && !e.getExamDate().toLocalDate().isBefore(start))
                        .collect(Collectors.toList());
            } catch (Exception e) {
                // Ignore parsing errors
            }
        }
        if (toDate != null && !toDate.trim().isEmpty()) {
            try {
                java.time.LocalDate end = java.time.LocalDate.parse(toDate);
                timeline = timeline.stream()
                        .filter(e -> e.getExamDate() != null && !e.getExamDate().toLocalDate().isAfter(end))
                        .collect(Collectors.toList());
            } catch (Exception e) {
                // Ignore parsing errors
            }
        }

        // Pagination logic
        int totalElements = timeline.size();
        int pageSize = 5;
        int totalPages = (int) Math.ceil((double) totalElements / pageSize);
        if (totalPages == 0) {
            totalPages = 1;
        }
        if (page < 1) {
            page = 1;
        }
        if (page > totalPages) {
            page = totalPages;
        }
        int fromIndex = (page - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, totalElements);
        List<ClinicalExamination> pagedTimeline = Collections.emptyList();
        if (fromIndex < totalElements) {
            pagedTimeline = timeline.subList(fromIndex, toIndex);
        }

        List<Map<String, Object>> timelineDetailsList = new java.util.ArrayList<>();
        for (ClinicalExamination exam : pagedTimeline) {
            Map<String, Object> map = new java.util.HashMap<>();
            map.put("exam", exam);

            // Nạp triệu chứng liên quan
            List<ExamSymptom> symptoms = examSymptomRepository.findAll().stream()
                    .filter(s -> s.getId().getClinicalExamId().equals(exam.getClinicalExamId()))
                    .collect(Collectors.toList());
            map.put("symptoms", symptoms);

            // Nạp kết quả xét nghiệm liên quan
            List<LabResult> labResults = labResultRepository
                    .findByLabOrder_ClinicalExamination_ClinicalExamId(exam.getClinicalExamId());
            map.put("labResults", labResults);

            // Nạp chi tiết đơn thuốc
            Prescription prescription = prescriptionRepository
                    .findByClinicalExamination_ClinicalExamId(exam.getClinicalExamId()).orElse(null);
            if (prescription != null) {
                List<PrescriptionDetail> details = prescriptionDetailRepository
                        .findByPrescription_PrescriptionId(prescription.getPrescriptionId());
                map.put("prescriptionDetails", details);
            } else {
                map.put("prescriptionDetails", Collections.emptyList());
            }

            timelineDetailsList.add(map);
        }

        model.addAttribute("timelineDetails", timelineDetailsList);
        model.addAttribute("currentFromDate", fromDate);
        model.addAttribute("currentToDate", toDate);
        model.addAttribute("from", from);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalElements", totalElements);
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("patientId", selectedPatientId);

        return "doctor/patients";
    }

    @GetMapping("/history/detail/{examId}")
    public String patientHistoryDetailPage(
            @PathVariable("examId") String examId,
            @RequestParam(value = "from", required = false) String from,
            HttpSession session,
            Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !"DOC".equalsIgnoreCase(loggedInUser.getRole().getRoleId())) {
            return "redirect:/login";
        }
        model.addAttribute("loggedInUser", loggedInUser);

        String doctorId = loggedInUser.getUserId();
        Profile profile = profileService.findById(doctorId).orElse(null);
        model.addAttribute("doctorProfile", profile);

        ClinicalExamination exam = clinicalExaminationRepository.findById(examId)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Không tìm thấy ca khám này."));

        // Minimal patient info for header/sidebar
        Patient patient = exam.getPatient();
        java.util.Map<String, Object> patientMap = new java.util.HashMap<>();
        patientMap.put("userId", patient.getUserId());
        patientMap.put("fullName", patient.getFullName());
        patientMap.put("imageUrl", patient.getImageUrl());
        model.addAttribute("patient", patientMap);

        // Fetch exam related data
        java.util.Map<String, Object> td = new java.util.HashMap<>();
        td.put("exam", exam);

        List<ExamSymptom> symptoms = examSymptomRepository.findAll().stream()
                .filter(s -> s.getId().getClinicalExamId().equals(exam.getClinicalExamId()))
                .collect(Collectors.toList());
        td.put("symptoms", symptoms);

        List<LabResult> labResults = labResultRepository
                .findByLabOrder_ClinicalExamination_ClinicalExamId(exam.getClinicalExamId());
        td.put("labResults", labResults);

        List<PrescriptionDetail> prescriptionDetails = java.util.Collections.emptyList();
        Optional<Prescription> prescription = prescriptionRepository
                .findByClinicalExamination_ClinicalExamId(exam.getClinicalExamId());
        if (prescription.isPresent()) {
            prescriptionDetails = prescriptionDetailRepository
                    .findByPrescription_PrescriptionId(prescription.get().getPrescriptionId());
        }
        td.put("prescriptionDetails", prescriptionDetails);

        model.addAttribute("td", td);
        model.addAttribute("from", from != null ? from : "history");

        return "doctor/history_detail";
    }
}
