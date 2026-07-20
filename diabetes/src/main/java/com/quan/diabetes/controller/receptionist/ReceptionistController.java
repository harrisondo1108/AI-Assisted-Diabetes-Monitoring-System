package com.quan.diabetes.controller.receptionist;

import com.quan.diabetes.entity.ClinicalExamination;
import com.quan.diabetes.entity.Patient;
import com.quan.diabetes.entity.Profile;
import com.quan.diabetes.entity.User;
import com.quan.diabetes.repository.UserRepository;
import com.quan.diabetes.service.exam.ClinicalExaminationService;
import com.quan.diabetes.service.user.PatientService;
import com.quan.diabetes.service.user.ProfileService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/receptionist")
public class ReceptionistController {

    @Autowired
    private ClinicalExaminationService clinicalExaminationService;

    @Autowired
    private ProfileService profileService;

    @Autowired
    private PatientService patientService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/dashboard")
    public String dashboard(
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "status", required = false) String statusFilter,
            @RequestParam(name = "doctorId", required = false) String doctorFilterId,
            Model model,
            HttpSession session) {

        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !"REC".equalsIgnoreCase(loggedInUser.getRole().getRoleId())) {
            return "redirect:/login";
        }

        // Lấy tất cả các ca khám
        List<ClinicalExamination> allExams = clinicalExaminationService.findAll();

        // Thống kê số lượng (trước khi lọc)
        long totalCount = allExams.size();
        long pendingCount = allExams.stream().filter(e -> "Pending".equalsIgnoreCase(e.getStatus())).count();
        long inProgressCount = allExams.stream().filter(e -> "InProgress".equalsIgnoreCase(e.getStatus())).count();
        long completedCount = allExams.stream().filter(e -> "Completed".equalsIgnoreCase(e.getStatus())).count();
        long cancelledCount = allExams.stream().filter(e -> "Cancelled".equalsIgnoreCase(e.getStatus())).count();

        // Áp dụng bộ lọc tìm kiếm và trạng thái
        List<ClinicalExamination> filteredExams = allExams;

        if (search != null && !search.trim().isEmpty()) {
            final String query = search.trim().toLowerCase();
            filteredExams = filteredExams.stream()
                    .filter(e -> {
                        String name = e.getPatient().getFullName().toLowerCase();
                        String phone = e.getPatient().getPhoneNumber() != null ? e.getPatient().getPhoneNumber() : "";
                        return name.contains(query) || phone.contains(query);
                    })
                    .collect(Collectors.toList());
        }

        if (statusFilter != null && !statusFilter.trim().isEmpty() && !"all".equalsIgnoreCase(statusFilter)) {
            filteredExams = filteredExams.stream()
                    .filter(e -> statusFilter.equalsIgnoreCase(e.getStatus()))
                    .collect(Collectors.toList());
        }

        if (doctorFilterId != null && !doctorFilterId.trim().isEmpty() && !"all".equalsIgnoreCase(doctorFilterId)) {
            filteredExams = filteredExams.stream()
                    .filter(e -> doctorFilterId.equals(e.getDoctor().getUserId()))
                    .collect(Collectors.toList());
        }

        // Sắp xếp lịch hẹn theo thời gian mới nhất lên đầu
        filteredExams.sort((e1, e2) -> {
            if (e1.getExamDate() == null && e2.getExamDate() == null) return 0;
            if (e1.getExamDate() == null) return 1;
            if (e2.getExamDate() == null) return -1;
            return e2.getExamDate().compareTo(e1.getExamDate());
        });

        // Lấy tất cả bác sĩ hoạt động để làm bộ lọc/chọn lựa
        List<Profile> activeDoctors = profileService.findTotalDoctor().stream()
                .filter(p -> p.getUser() != null && "Active".equalsIgnoreCase(p.getUser().getStatus()))
                .collect(Collectors.toList());

        model.addAttribute("examinations", filteredExams);
        model.addAttribute("doctors", activeDoctors);
        model.addAttribute("currentSearch", search);
        model.addAttribute("currentStatus", statusFilter);
        model.addAttribute("currentDoctorId", doctorFilterId);

        model.addAttribute("totalCount", totalCount);
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("inProgressCount", inProgressCount);
        model.addAttribute("completedCount", completedCount);
        model.addAttribute("cancelledCount", cancelledCount);

        return "receptionist/dashboard";
    }

    @PostMapping("/booking/checkin/{examId}")
    public String checkinPatient(@PathVariable("examId") String examId, RedirectAttributes redirectAttributes) {
        Optional<ClinicalExamination> examOpt = clinicalExaminationService.findById(examId);
        if (examOpt.isPresent()) {
            ClinicalExamination exam = examOpt.get();
            if ("Pending".equalsIgnoreCase(exam.getStatus())) {
                exam.setStatus("InProgress");
                exam.setExamDate(LocalDateTime.now());
                clinicalExaminationService.update(examId, exam);
                redirectAttributes.addFlashAttribute("successMessage", "Đã tiếp nhận bệnh nhân vào phòng khám!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Trạng thái lịch hẹn không hợp lệ để tiếp nhận.");
            }
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Lịch khám không tồn tại.");
        }
        return "redirect:/receptionist/dashboard";
    }

    @PostMapping("/booking/cancel/{examId}")
    public String cancelAppointment(
            @PathVariable("examId") String examId,
            @RequestParam("cancelReason") String reason,
            RedirectAttributes redirectAttributes) {
        Optional<ClinicalExamination> examOpt = clinicalExaminationService.findById(examId);
        if (examOpt.isPresent()) {
            ClinicalExamination exam = examOpt.get();
            if ("Pending".equalsIgnoreCase(exam.getStatus()) || "InProgress".equalsIgnoreCase(exam.getStatus()) || "Requested".equalsIgnoreCase(exam.getStatus())) {
                exam.setStatus("Cancelled");
                exam.setCancelReason(reason);
                clinicalExaminationService.update(examId, exam);
                redirectAttributes.addFlashAttribute("successMessage", "Hủy lịch khám thành công!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Không thể hủy lịch khám đã hoàn thành.");
            }
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Lịch khám không tồn tại.");
        }
        return "redirect:/receptionist/dashboard";
    }

    @PostMapping("/booking/update/{examId}")
    public String updateAppointment(
            @PathVariable("examId") String examId,
            @RequestParam("doctorId") String doctorId,
            @RequestParam("examDate") String examDateStr,
            RedirectAttributes redirectAttributes) {

        Optional<ClinicalExamination> examOpt = clinicalExaminationService.findById(examId);
        if (examOpt.isPresent()) {
            ClinicalExamination exam = examOpt.get();
            if ("Pending".equalsIgnoreCase(exam.getStatus())) {
                try {
                    LocalDateTime examDate = LocalDateTime.parse(examDateStr);
                    if (examDate.toLocalDate().isBefore(java.time.LocalDate.now())) {
                        redirectAttributes.addFlashAttribute("errorMessage", "Ngày giờ hẹn mới không thể ở quá khứ.");
                        return "redirect:/receptionist/dashboard";
                    }

                    Optional<User> doctorOpt = userRepository.findById(doctorId);
                    if (doctorOpt.isPresent() && "DOC".equalsIgnoreCase(doctorOpt.get().getRole().getRoleId())) {
                        exam.setDoctor(doctorOpt.get());
                        exam.setExamDate(examDate);
                        clinicalExaminationService.update(examId, exam);
                        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật lịch khám thành công!");
                    } else {
                        redirectAttributes.addFlashAttribute("errorMessage", "Bác sĩ không hợp lệ.");
                    }
                } catch (Exception e) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Lỗi định dạng ngày hẹn: " + e.getMessage());
                }
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Chỉ có thể cập nhật lịch hẹn ở trạng thái Chờ khám.");
            }
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Lịch khám không tồn tại.");
        }
        return "redirect:/receptionist/dashboard";
    }

    @GetMapping("/booking")
    public String showBookingForm(Model model, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !"REC".equalsIgnoreCase(loggedInUser.getRole().getRoleId())) {
            return "redirect:/login";
        }

        // Lấy tất cả bệnh nhân
        List<Patient> patients = patientService.findAll();
        // Lấy tất cả bác sĩ hoạt động
        List<Profile> activeDoctors = profileService.findTotalDoctor().stream()
                .filter(p -> p.getUser() != null && "Active".equalsIgnoreCase(p.getUser().getStatus()))
                .collect(Collectors.toList());

        model.addAttribute("patients", patients);
        model.addAttribute("doctors", activeDoctors);

        return "receptionist/booking";
    }

    @PostMapping("/booking")
    public String bookAppointment(
            @RequestParam("patientId") String patientId,
            @RequestParam("doctorId") String doctorId,
            @RequestParam("examDate") String examDateStr,
            @RequestParam(value = "medicalHistory", required = false) String medicalHistory,
            RedirectAttributes redirectAttributes) {

        try {
            LocalDateTime examDate = LocalDateTime.parse(examDateStr);
            if (examDate.toLocalDate().isBefore(java.time.LocalDate.now())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Ngày giờ hẹn không thể ở quá khứ.");
                return "redirect:/receptionist/booking";
            }

            Optional<Patient> patientOpt = patientService.findById(patientId);
            Optional<User> doctorOpt = userRepository.findById(doctorId);

            if (patientOpt.isPresent() && doctorOpt.isPresent() && "DOC".equalsIgnoreCase(doctorOpt.get().getRole().getRoleId())) {
                ClinicalExamination exam = new ClinicalExamination();
                exam.setClinicalExamId("EXM-" + System.currentTimeMillis() + "-" + new Random().nextInt(1000));
                exam.setPatient(patientOpt.get());
                exam.setDoctor(doctorOpt.get());
                exam.setExamDate(examDate);
                exam.setMedicalHistory(medicalHistory);
                exam.setStatus("Pending");

                clinicalExaminationService.create(exam);
                redirectAttributes.addFlashAttribute("successMessage", "Đặt lịch khám tại quầy thành công!");
                return "redirect:/receptionist/dashboard";
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Bệnh nhân hoặc bác sĩ không hợp lệ.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi đặt lịch khám: " + e.getMessage());
        }

        return "redirect:/receptionist/booking";
    }
}
