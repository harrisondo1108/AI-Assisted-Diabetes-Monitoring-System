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
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

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

        // Removed the check that blocked doctors from seeing the queue if they had an InProgress exam.

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

    @PostMapping("/queue/{patientId}/cancel")
    @ResponseBody
    public ResponseEntity<?> cancelFromQueue(@PathVariable String patientId,
                                             @RequestParam("cancelReason") String cancelReason,
                                             HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !"DOC".equalsIgnoreCase(loggedInUser.getRole().getRoleId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("success", false, "message", "Bạn không có quyền thực hiện hành động này."));
        }
        if (cancelReason == null || cancelReason.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Lý do hủy không được để trống."));
        }
        try {
            clinicalExaminationService.cancelExamination(patientId, cancelReason.trim(), loggedInUser.getUserId());
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}
