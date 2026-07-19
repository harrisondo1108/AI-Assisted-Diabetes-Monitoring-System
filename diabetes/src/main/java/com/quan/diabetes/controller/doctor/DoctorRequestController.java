package com.quan.diabetes.controller.doctor;

import com.quan.diabetes.entity.ClinicalExamination;
import com.quan.diabetes.entity.Profile;
import com.quan.diabetes.entity.User;
import com.quan.diabetes.repository.ClinicalExaminationRepository;
import com.quan.diabetes.service.exam.ClinicalExaminationService;
import com.quan.diabetes.service.user.ProfileService;
import com.quan.diabetes.service.systemlog.SystemLogService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/doctor")
public class DoctorRequestController {

    private final ClinicalExaminationService clinicalExaminationService;
    private final ProfileService profileService;
    private final ClinicalExaminationRepository clinicalExaminationRepository;
    private final SystemLogService systemLogService;

    public DoctorRequestController(
            ClinicalExaminationService clinicalExaminationService,
            ProfileService profileService,
            ClinicalExaminationRepository clinicalExaminationRepository,
            SystemLogService systemLogService) {
        this.clinicalExaminationService = clinicalExaminationService;
        this.profileService = profileService;
        this.clinicalExaminationRepository = clinicalExaminationRepository;
        this.systemLogService = systemLogService;
    }

    @GetMapping("/requests")
    public String requestsPage(HttpSession session, Model model) {
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

        List<ClinicalExamination> allExams = clinicalExaminationService.findByDoctorId(doctorId);
        List<ClinicalExamination> requestedExams = allExams.stream()
                .filter(e -> "Requested".equalsIgnoreCase(e.getStatus()))
                .sorted((e1, e2) -> e2.getExamDate().compareTo(e1.getExamDate()))
                .collect(Collectors.toList());

        model.addAttribute("requestedExams", requestedExams);
        return "doctor/requests";
    }

    @PostMapping("/request/approve/{examId}")
    public String approveRequest(
            @PathVariable("examId") String examId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !"DOC".equalsIgnoreCase(loggedInUser.getRole().getRoleId())) {
            return "redirect:/login";
        }

        Optional<ClinicalExamination> examOpt = clinicalExaminationRepository.findById(examId);
        if (examOpt.isPresent()) {
            ClinicalExamination exam = examOpt.get();
            if ("Requested".equalsIgnoreCase(exam.getStatus())) {
                ClinicalExamination oldExam = createLogExam(exam);
                
                exam.setStatus("Pending");
                exam.setExamDate(LocalDateTime.now()); // Set date to today so it appears in today's queue
                clinicalExaminationRepository.save(exam);
                
                systemLogService.saveLogWithObject(loggedInUser.getUserId(), "APPROVE_MEDICAL_REQUEST", "MedicalRequest", exam.getClinicalExamId(), "Bác sĩ duyệt yêu cầu khám", oldExam, createLogExam(exam), "SUCCESS");
                
                redirectAttributes.addFlashAttribute("successMessage",
                        "Đã duyệt yêu cầu khám của bệnh nhân " + exam.getPatient().getFullName() + ".");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Yêu cầu khám này đã được xử lý từ trước.");
            }
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy yêu cầu khám.");
        }

        return "redirect:/doctor/requests";
    }

    @PostMapping("/request/reject/{examId}")
    public String rejectRequest(
            @PathVariable("examId") String examId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !"DOC".equalsIgnoreCase(loggedInUser.getRole().getRoleId())) {
            return "redirect:/login";
        }

        Optional<ClinicalExamination> examOpt = clinicalExaminationRepository.findById(examId);
        if (examOpt.isPresent()) {
            ClinicalExamination exam = examOpt.get();
            if ("Requested".equalsIgnoreCase(exam.getStatus())) {
                ClinicalExamination oldExam = createLogExam(exam);
                
                exam.setStatus("Cancelled");
                exam.setCancelReason("Bác sĩ từ chối yêu cầu khám");
                clinicalExaminationRepository.save(exam);
                
                systemLogService.saveLogWithObject(loggedInUser.getUserId(), "REJECT_MEDICAL_REQUEST", "MedicalRequest", exam.getClinicalExamId(), "Bác sĩ từ chối yêu cầu khám", oldExam, createLogExam(exam), "SUCCESS");
                
                redirectAttributes.addFlashAttribute("successMessage",
                        "Đã từ chối yêu cầu khám của bệnh nhân " + exam.getPatient().getFullName() + ".");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Yêu cầu khám này đã được xử lý từ trước.");
            }
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy yêu cầu khám.");
        }

        return "redirect:/doctor/requests";
    }

    private ClinicalExamination createLogExam(ClinicalExamination exam) {
        if (exam == null) return null;
        ClinicalExamination logExam = new ClinicalExamination();
        logExam.setClinicalExamId(exam.getClinicalExamId());
        logExam.setStatus(exam.getStatus());
        logExam.setMedicalHistory(exam.getMedicalHistory());
        logExam.setDiagnosisNote(exam.getDiagnosisNote());
        logExam.setCancelReason(exam.getCancelReason());
        logExam.setNextAppointment(exam.getNextAppointment());
        logExam.setExamDate(exam.getExamDate());
        return logExam;
    }
}
