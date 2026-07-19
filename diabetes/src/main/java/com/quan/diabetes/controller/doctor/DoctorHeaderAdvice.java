package com.quan.diabetes.controller.doctor;

import com.quan.diabetes.entity.ClinicalExamination;
import com.quan.diabetes.entity.User;
import com.quan.diabetes.service.exam.ClinicalExaminationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@ControllerAdvice(assignableTypes = {
    DoctorDashboardController.class,
    DoctorQueueController.class,
    DoctorReminderController.class,
    DoctorRequestController.class,
    DoctorProfileController.class,
    DoctorPatientController.class,
    DoctorExamineController.class
})
public class DoctorHeaderAdvice {

    private final ClinicalExaminationService clinicalExaminationService;

    @Autowired
    public DoctorHeaderAdvice(ClinicalExaminationService clinicalExaminationService) {
        this.clinicalExaminationService = clinicalExaminationService;
    }

    @ModelAttribute("requestedExams")
    public List<ClinicalExamination> addRequestedExamsToModel(HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !"DOC".equalsIgnoreCase(loggedInUser.getRole().getRoleId())) {
            return Collections.emptyList();
        }

        String doctorId = loggedInUser.getUserId();
        List<ClinicalExamination> allExams = clinicalExaminationService.findByDoctorId(doctorId);
        
        return allExams.stream()
                .filter(e -> "Requested".equalsIgnoreCase(e.getStatus()))
                .sorted((e1, e2) -> e2.getExamDate().compareTo(e1.getExamDate()))
                .collect(Collectors.toList());
    }
}
