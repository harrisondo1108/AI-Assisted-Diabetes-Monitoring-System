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
public class DoctorQueueController {

    private final ClinicalExaminationService clinicalExaminationService;
    private final ProfileService profileService;
    private final ClinicalExaminationRepository clinicalExaminationRepository;

    public DoctorQueueController(
            ClinicalExaminationService clinicalExaminationService,
            ProfileService profileService,
            ClinicalExaminationRepository clinicalExaminationRepository) {
        this.clinicalExaminationService = clinicalExaminationService;
        this.profileService = profileService;
        this.clinicalExaminationRepository = clinicalExaminationRepository;
    }

    @GetMapping("/queue")
    public String queue(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "status", defaultValue = "all") String status,
            @RequestParam(value = "search", defaultValue = "") String search,
            HttpSession session,
            Model model) {
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
            return "redirect:/doctor/examine?patientId=" + activeExam.get().getPatient().getUserId()
                    + "&warning=in-progress";
        }

        // Lấy tất cả ca khám của bác sĩ
        List<ClinicalExamination> allExams = clinicalExaminationService.findByDoctorId(doctorId);
        LocalDate today = LocalDate.now();

        // Lọc danh sách ca khám hôm nay
        List<ClinicalExamination> todayQueue = allExams.stream()
                .filter(e -> e.getExamDate() != null && e.getExamDate().toLocalDate().isEqual(today)
                        && !"Requested".equalsIgnoreCase(e.getStatus()))
                .collect(Collectors.toList());

        // Lọc theo bộ lọc status và search
        List<ClinicalExamination> filteredQueue = todayQueue.stream()
                .filter(e -> {
                    // Lọc status
                    if (!"all".equalsIgnoreCase(status) && !status.equalsIgnoreCase(e.getStatus())) {
                        return false;
                    }
                    // Lọc tìm kiếm
                    if (search != null && !search.trim().isEmpty()) {
                        String patientName = e.getPatient().getFullName() != null
                                ? e.getPatient().getFullName().toLowerCase()
                                : "";
                        String patientId = e.getPatient().getUserId() != null ? e.getPatient().getUserId().toLowerCase()
                                : "";
                        String query = search.trim().toLowerCase();
                        return patientName.contains(query) || patientId.contains(query);
                    }
                    return true;
                })
                .collect(Collectors.toList());

        // Phân trang
        int pageSize = 8;
        int totalElements = filteredQueue.size();
        int totalPages = (int) Math.ceil((double) totalElements / pageSize);
        if (totalPages == 0) {
            totalPages = 1;
        }

        if (page < 1)
            page = 1;
        if (page > totalPages)
            page = totalPages;

        int fromIndex = (page - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, totalElements);
        List<ClinicalExamination> pagedQueue = Collections.emptyList();
        if (totalElements > 0) {
            pagedQueue = filteredQueue.subList(fromIndex, toIndex);
        }

        model.addAttribute("todayQueue", pagedQueue);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("currentStatus", status);
        model.addAttribute("currentSearch", search);

        return "doctor/queue";
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
                exam.setStatus("Pending");
                exam.setExamDate(LocalDateTime.now()); // Set date to today so it appears in today's queue
                clinicalExaminationRepository.save(exam);
                redirectAttributes.addFlashAttribute("successMessage",
                        "Đã duyệt yêu cầu khám của bệnh nhân " + exam.getPatient().getFullName() + ".");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Yêu cầu khám này đã được xử lý từ trước.");
            }
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy yêu cầu khám.");
        }

        return "redirect:/doctor/queue";
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
                exam.setStatus("Cancelled");
                exam.setCancelReason("Bác sĩ từ chối yêu cầu khám");
                clinicalExaminationRepository.save(exam);
                redirectAttributes.addFlashAttribute("successMessage",
                        "Đã từ chối yêu cầu khám của bệnh nhân " + exam.getPatient().getFullName() + ".");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Yêu cầu khám này đã được xử lý từ trước.");
            }
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy yêu cầu khám.");
        }

        return "redirect:/doctor/queue";
    }
}
