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
            return "redirect:/doctor/dashboard";
        }

        Patient patient = patientService.findById(selectedPatientId).orElse(null);
        if (patient == null) {
            return "redirect:/doctor/dashboard";
        }
        model.addAttribute("patient", patient);

        // Nạp lịch sử các ca khám (Timeline) của bệnh nhân này (Chỉ lấy Completed hoặc Cancelled)
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

        model.addAttribute("timeline", timeline);
        model.addAttribute("currentFromDate", fromDate);
        model.addAttribute("currentToDate", toDate);
        model.addAttribute("from", from);

        return "doctor/patients";
    }

    @GetMapping("/history/view-exam/{examId}")
    public String viewTimelineExamDetail(@PathVariable("examId") String examId, Model model) {
        ClinicalExamination exam = clinicalExaminationService.findById(examId).orElse(null);
        if (exam == null) {
            return "doctor/patients :: timelineDetail";
        }

        model.addAttribute("exam", exam);

        // Nạp triệu chứng liên quan
        List<ExamSymptom> symptoms = examSymptomRepository.findAll().stream()
                .filter(s -> s.getId().getClinicalExamId().equals(examId))
                .collect(Collectors.toList());
        model.addAttribute("symptoms", symptoms);

        // Nạp kết quả xét nghiệm liên quan
        List<LabResult> labResults = labResultRepository.findByLabOrder_ClinicalExamination_ClinicalExamId(examId);
        model.addAttribute("labResults", labResults);

        // Nạp chi tiết đơn thuốc
        Prescription prescription = prescriptionRepository.findByClinicalExamination_ClinicalExamId(examId).orElse(null);
        if (prescription != null) {
            List<PrescriptionDetail> details = prescriptionDetailRepository.findByPrescription_PrescriptionId(prescription.getPrescriptionId());
            model.addAttribute("prescriptionDetails", details);
        } else {
            model.addAttribute("prescriptionDetails", Collections.emptyList());
        }

        return "doctor/patients :: timelineDetail";
    }
}
