package com.quan.diabetes.controller.doctor;

import com.quan.diabetes.entity.*;
import com.quan.diabetes.repository.*;
import com.quan.diabetes.service.exam.ClinicalExaminationService;
import com.quan.diabetes.service.user.ProfileService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/doctor")
public class DoctorDashboardController {

    private final ClinicalExaminationService clinicalExaminationService;
    private final ProfileService profileService;
    private final ClinicalExaminationRepository clinicalExaminationRepository;

    public DoctorDashboardController(
            ClinicalExaminationService clinicalExaminationService,
            ProfileService profileService,
            ClinicalExaminationRepository clinicalExaminationRepository) {
        this.clinicalExaminationService = clinicalExaminationService;
        this.profileService = profileService;
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

        List<ClinicalExamination> allExams = clinicalExaminationService.findAll();

        // 1. Pending Requests (Yêu cầu chờ duyệt)
        List<ClinicalExamination> requestedExams = allExams.stream()
                .filter(e -> "Requested".equalsIgnoreCase(e.getStatus()))
                .sorted((e1, e2) -> e2.getExamDate().compareTo(e1.getExamDate()))
                .collect(Collectors.toList());

        // 2. Queue (Đang chờ khám)
        long todayQueueCount = allExams.stream()
                .filter(e -> ("Pending".equalsIgnoreCase(e.getStatus()) || "InProgress".equalsIgnoreCase(e.getStatus()))
                        && (e.getDoctor() == null || doctorId.equalsIgnoreCase(e.getDoctor().getUserId())))
                .count();

        // 3. Upcoming Follow-ups in next 7 days (Lịch tái khám sắp đến)
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime sevenDaysLater = now.plusDays(7);
        List<ClinicalExamination> upcomingFollowUps = clinicalExaminationRepository
                .findByDoctor_UserIdAndNextAppointmentBetweenOrderByNextAppointmentAsc(
                        doctorId, now, sevenDaysLater);

        model.addAttribute("requestedExams", requestedExams);
        model.addAttribute("pendingRequestsCount", requestedExams.size());
        model.addAttribute("todayQueueCount", todayQueueCount);
        model.addAttribute("upcomingFollowUps", upcomingFollowUps);
        model.addAttribute("upcomingFollowUpsCount", upcomingFollowUps.size());

        return "doctor/dashboard";
    }
}
