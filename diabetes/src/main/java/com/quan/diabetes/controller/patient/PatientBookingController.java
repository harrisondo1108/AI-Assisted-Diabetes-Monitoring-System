package com.quan.diabetes.controller.patient;

import com.quan.diabetes.entity.ClinicalExamination;
import com.quan.diabetes.entity.Patient;
import com.quan.diabetes.entity.Profile;
import com.quan.diabetes.entity.User;
import com.quan.diabetes.repository.UserRepository;
import com.quan.diabetes.service.user.ProfileService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

@Controller
public class PatientBookingController extends BasePatientController {

    @Autowired
    private ProfileService profileService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/patient/booking")
    public String showBookingPage(Model model, HttpSession session) {
        Patient patient = addCommonData(model, session, "booking");
        if (patient == null) {
            return "redirect:/login";
        }

        // Lấy tất cả bác sĩ hoạt động
        List<Profile> activeDoctors = profileService.findTotalDoctor().stream()
                .filter(p -> p.getUser() != null && "Active".equalsIgnoreCase(p.getUser().getStatus()))
                .collect(Collectors.toList());
        model.addAttribute("doctors", activeDoctors);

        // Lấy danh sách lịch hẹn của bệnh nhân
        List<ClinicalExamination> examinations = clinicalExaminationService.findByPatientId(patient.getUserId());
        // Sắp xếp lịch hẹn theo thời gian mới nhất lên đầu
        examinations.sort((e1, e2) -> {
            if (e1.getExamDate() == null && e2.getExamDate() == null) return 0;
            if (e1.getExamDate() == null) return 1;
            if (e2.getExamDate() == null) return -1;
            return e2.getExamDate().compareTo(e1.getExamDate());
        });
        model.addAttribute("examinations", examinations);

        return "patient/booking";
    }

    @PostMapping("/patient/booking")
    public String bookAppointment(
            @RequestParam("doctorId") String doctorId,
            @RequestParam("examDate") String examDateStr,
            @RequestParam(value = "medicalHistory", required = false) String medicalHistory,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Patient patient = getCurrentPatient(session);
        if (patient == null) {
            return "redirect:/login";
        }

        try {
            // Parse ngày hẹn
            LocalDateTime examDate = LocalDateTime.parse(examDateStr);

            // Kiểm tra ngày khám không được ở quá khứ
            if (examDate.isBefore(LocalDateTime.now())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Ngày giờ khám không thể ở quá khứ.");
                return "redirect:/patient/booking";
            }

            Optional<User> doctorOpt = userRepository.findById(doctorId);
            if (doctorOpt.isEmpty() || !"DOC".equalsIgnoreCase(doctorOpt.get().getRole().getRoleId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Bác sĩ không hợp lệ.");
                return "redirect:/patient/booking";
            }

            ClinicalExamination exam = new ClinicalExamination();
            exam.setClinicalExamId("EXM-" + System.currentTimeMillis() + "-" + new Random().nextInt(1000));
            exam.setPatient(patient);
            exam.setDoctor(doctorOpt.get());
            exam.setExamDate(examDate);
            exam.setMedicalHistory(medicalHistory);
            exam.setStatus("Requested");

            clinicalExaminationService.create(exam);
            redirectAttributes.addFlashAttribute("successMessage", "Đặt lịch khám thành công!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi đặt lịch khám: " + e.getMessage());
        }

        return "redirect:/patient/booking";
    }

    @PostMapping("/patient/booking/cancel/{examId}")
    public String cancelAppointment(
            @PathVariable("examId") String examId,
            @RequestParam("cancelReason") String cancelReason,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Patient patient = getCurrentPatient(session);
        if (patient == null) {
            return "redirect:/login";
        }

        Optional<ClinicalExamination> examOpt = clinicalExaminationService.findById(examId);
        if (examOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lịch khám không tồn tại.");
            return "redirect:/patient/booking";
        }

        ClinicalExamination exam = examOpt.get();
        if (!exam.getPatient().getUserId().equals(patient.getUserId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn không có quyền hủy lịch khám này.");
            return "redirect:/patient/booking";
        }

        if (!"Pending".equalsIgnoreCase(exam.getStatus())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Chỉ có thể hủy lịch khám đang ở trạng thái Chờ khám.");
            return "redirect:/patient/booking";
        }

        if (cancelReason == null || cancelReason.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng nhập lý do hủy lịch.");
            return "redirect:/patient/booking";
        }

        exam.setStatus("Cancelled");
        exam.setCancelReason(cancelReason);
        clinicalExaminationService.update(examId, exam);

        redirectAttributes.addFlashAttribute("successMessage", "Hủy lịch khám thành công!");
        return "redirect:/patient/booking";
    }
}
