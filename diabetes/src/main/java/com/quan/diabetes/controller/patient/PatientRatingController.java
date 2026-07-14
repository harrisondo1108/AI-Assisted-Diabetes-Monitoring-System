package com.quan.diabetes.controller.patient;

import com.quan.diabetes.entity.*;
import com.quan.diabetes.service.exam.DoctorRatingService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class PatientRatingController extends BasePatientController {

    private final DoctorRatingService doctorRatingService;

    @Autowired
    public PatientRatingController(DoctorRatingService doctorRatingService) {
        this.doctorRatingService = doctorRatingService;
    }

    @GetMapping("/patient/history/rate")
    public String rateDoctorPage(
            @RequestParam("examId") String examId,
            HttpSession session, Model model,
            RedirectAttributes redirectAttributes) {
        
        Patient patient = getCurrentPatient(session);
        if (patient == null) {
            return "redirect:/login";
        }
        
        addCommonData(model, session, "history");

        ClinicalExamination exam = clinicalExaminationService.findById(examId).orElse(null);
        if (exam == null || exam.getPatient() == null || !patient.getUserId().equals(exam.getPatient().getUserId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lượt khám không tồn tại hoặc không thuộc quyền sở hữu của bạn.");
            return "redirect:/patient/history";
        }

        if (!"completed".equalsIgnoreCase(exam.getStatus())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn chỉ có thể đánh giá lượt khám đã hoàn thành.");
            return "redirect:/patient/history";
        }

        if (doctorRatingService.getRatingByExamId(examId).isPresent()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lượt khám này đã được đánh giá trước đó.");
            return "redirect:/patient/history";
        }

        model.addAttribute("exam", exam);
        return "patient/rate-doctor";
    }

    @PostMapping("/patient/history/rate")
    public String rateDoctor(
            @RequestParam("examId") String examId,
            @RequestParam("ratingValue") Integer ratingValue,
            @RequestParam(value = "comment", required = false) String comment,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Patient patient = getCurrentPatient(session);
        if (patient == null) {
            return "redirect:/login";
        }

        if (ratingValue == null || ratingValue < 1 || ratingValue > 5) {
            redirectAttributes.addFlashAttribute("errorMessage", "Chỉ số đánh giá phải nằm trong khoảng từ 1 đến 5 sao.");
            return "redirect:/patient/history/rate?examId=" + examId;
        }

        ClinicalExamination exam = clinicalExaminationService.findById(examId).orElse(null);
        if (exam == null || exam.getPatient() == null || !patient.getUserId().equals(exam.getPatient().getUserId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lượt khám không tồn tại hoặc không thuộc quyền sở hữu của bạn.");
            return "redirect:/patient/history";
        }

        if (!"completed".equalsIgnoreCase(exam.getStatus())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn chỉ có thể đánh giá lượt khám đã hoàn thành.");
            return "redirect:/patient/history";
        }

        if (doctorRatingService.getRatingByExamId(examId).isPresent()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lượt khám này đã được đánh giá trước đó.");
            return "redirect:/patient/history";
        }

        try {
            DoctorRating rating = new DoctorRating();
            rating.setClinicalExamination(exam);
            rating.setPatient(patient);
            rating.setDoctor(exam.getDoctor());
            rating.setRatingValue(ratingValue);
            rating.setComment(comment != null ? comment.trim() : null);

            doctorRatingService.saveRating(rating);

            redirectAttributes.addFlashAttribute("successMessage", "Cảm ơn bạn đã gửi đánh giá cho bác sĩ!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra: " + e.getMessage());
            return "redirect:/patient/history/rate?examId=" + examId;
        }

        return "redirect:/patient/history";
    }
}
