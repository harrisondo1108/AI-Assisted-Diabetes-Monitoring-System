package com.quan.diabetes.controller.patient;

import com.quan.diabetes.entity.*;
import com.quan.diabetes.service.exam.DoctorRatingService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
public class PatientRatingController extends BasePatientController {

    private final DoctorRatingService doctorRatingService;

    @Autowired
    public PatientRatingController(DoctorRatingService doctorRatingService) {
        this.doctorRatingService = doctorRatingService;
    }

    @PostMapping("/patient/history/rate")
    @ResponseBody
    public Map<String, Object> rateDoctor(
            @RequestParam("examId") String examId,
            @RequestParam("ratingValue") Integer ratingValue,
            @RequestParam(value = "comment", required = false) String comment,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        Patient patient = getCurrentPatient(session);
        if (patient == null) {
            response.put("success", false);
            response.put("message", "Vui lòng đăng nhập để thực hiện đánh giá.");
            return response;
        }

        if (ratingValue == null || ratingValue < 1 || ratingValue > 5) {
            response.put("success", false);
            response.put("message", "Chỉ số đánh giá phải nằm trong khoảng từ 1 đến 5 sao.");
            return response;
        }

        ClinicalExamination exam = clinicalExaminationService.findById(examId).orElse(null);
        if (exam == null || exam.getPatient() == null || !patient.getUserId().equals(exam.getPatient().getUserId())) {
            response.put("success", false);
            response.put("message", "Lượt khám không tồn tại hoặc không thuộc quyền sở hữu của bạn.");
            return response;
        }

        if (!"completed".equalsIgnoreCase(exam.getStatus())) {
            response.put("success", false);
            response.put("message", "Bạn chỉ có thể đánh giá lượt khám đã hoàn thành.");
            return response;
        }

        if (doctorRatingService.getRatingByExamId(examId).isPresent()) {
            response.put("success", false);
            response.put("message", "Lượt khám này đã được đánh giá trước đó.");
            return response;
        }

        try {
            DoctorRating rating = new DoctorRating();
            rating.setClinicalExamination(exam);
            rating.setPatient(patient);
            rating.setDoctor(exam.getDoctor());
            rating.setRatingValue(ratingValue);
            rating.setComment(comment != null ? comment.trim() : null);

            doctorRatingService.saveRating(rating);

            response.put("success", true);
            response.put("message", "Cảm ơn bạn đã gửi đánh giá cho bác sĩ!");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Có lỗi xảy ra: " + e.getMessage());
        }

        return response;
    }
}
