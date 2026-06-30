package com.quan.diabetes.controller.doctor;

import com.quan.diabetes.entity.*;
import com.quan.diabetes.repository.*;
import com.quan.diabetes.service.exam.ClinicalExaminationService;
import com.quan.diabetes.service.user.ProfileService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/doctor")
public class DoctorDashboardController {

    private final ClinicalExaminationService clinicalExaminationService;
    private final ProfileService profileService;
    private final LabResultRepository labResultRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final PrescriptionDetailRepository prescriptionDetailRepository;
    private final ExamSymptomRepository examSymptomRepository;
    private final ClinicalExaminationRepository clinicalExaminationRepository;

    public DoctorDashboardController(
            ClinicalExaminationService clinicalExaminationService,
            ProfileService profileService,
            LabResultRepository labResultRepository,
            PrescriptionRepository prescriptionRepository,
            PrescriptionDetailRepository prescriptionDetailRepository,
            ExamSymptomRepository examSymptomRepository,
            ClinicalExaminationRepository clinicalExaminationRepository) {
        this.clinicalExaminationService = clinicalExaminationService;
        this.profileService = profileService;
        this.labResultRepository = labResultRepository;
        this.prescriptionRepository = prescriptionRepository;
        this.prescriptionDetailRepository = prescriptionDetailRepository;
        this.examSymptomRepository = examSymptomRepository;
        this.clinicalExaminationRepository = clinicalExaminationRepository;
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !"DOC".equalsIgnoreCase(loggedInUser.getRole().getRoleId())) {
            return "redirect:/login";
        }

        String doctorId = loggedInUser.getUserId();
        Profile profile = profileService.findById(doctorId).orElse(null);
        if (profile != null) {
            model.addAttribute("doctorProfile", profile);
            session.setAttribute("userProfile", profile);
        }

        // Check if there is an in-progress exam for this doctor in the database
        Optional<ClinicalExamination> activeExam = clinicalExaminationRepository
                .findFirstByDoctor_UserIdAndStatus(doctorId, "InProgress");
        if (activeExam.isPresent()) {
            return "redirect:/doctor/examine?patientId=" + activeExam.get().getPatient().getUserId() + "&warning=in-progress";
        }

        // Lấy tất cả ca khám của bác sĩ
        List<ClinicalExamination> allExams = clinicalExaminationService.findByDoctorId(doctorId);
        LocalDate today = LocalDate.now();

        // Lọc danh sách ca khám hôm nay
        List<ClinicalExamination> todayQueue = allExams.stream()
                .filter(e -> e.getExamDate() != null && e.getExamDate().toLocalDate().isEqual(today))
                .collect(Collectors.toList());

        // Tính toán các metrics
        long pending = todayQueue.stream().filter(e -> "Pending".equalsIgnoreCase(e.getStatus())).count();
        long inProgress = todayQueue.stream().filter(e -> "InProgress".equalsIgnoreCase(e.getStatus())).count();
        long completed = todayQueue.stream().filter(e -> "Completed".equalsIgnoreCase(e.getStatus())).count();

        model.addAttribute("todayQueue", todayQueue);
        model.addAttribute("queueCount", pending + inProgress);
        model.addAttribute("completedCount", completed);
        long alertCount = labResultRepository.countByLabOrder_ClinicalExamination_Doctor_UserIdAndFlag(doctorId, "HIGH");
        model.addAttribute("alertCount", alertCount);

        return "doctor/dashboard";
    }

    @GetMapping("/dashboard/view-exam/{examId}")
    public String viewExamDetail(@PathVariable("examId") String examId, Model model) {
        ClinicalExamination exam = clinicalExaminationService.findById(examId).orElse(null);
        if (exam == null) {
            return "doctor/dashboard :: examDetail";
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

        return "doctor/dashboard :: examDetail";
    }
}
