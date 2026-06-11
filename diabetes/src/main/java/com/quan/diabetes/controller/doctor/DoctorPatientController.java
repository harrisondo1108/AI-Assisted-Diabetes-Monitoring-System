package com.quan.diabetes.controller.doctor;

import com.quan.diabetes.entity.*;
import com.quan.diabetes.repository.ExamSymptomRepository;
import com.quan.diabetes.repository.LabResultRepository;
import com.quan.diabetes.repository.PrescriptionDetailRepository;
import com.quan.diabetes.repository.PrescriptionRepository;
import com.quan.diabetes.service.ClinicalExaminationService;
import com.quan.diabetes.service.PatientService;
import com.quan.diabetes.service.ProfileService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/doctor")
public class DoctorPatientController {

    private final ClinicalExaminationService clinicalExaminationService;
    private final PatientService patientService;
    private final ProfileService profileService;
    private final LabResultRepository labResultRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final PrescriptionDetailRepository prescriptionDetailRepository;
    private final ExamSymptomRepository examSymptomRepository;

    public DoctorPatientController(
            ClinicalExaminationService clinicalExaminationService,
            PatientService patientService,
            ProfileService profileService,
            LabResultRepository labResultRepository,
            PrescriptionRepository prescriptionRepository,
            PrescriptionDetailRepository prescriptionDetailRepository,
            ExamSymptomRepository examSymptomRepository) {
        this.clinicalExaminationService = clinicalExaminationService;
        this.patientService = patientService;
        this.profileService = profileService;
        this.labResultRepository = labResultRepository;
        this.prescriptionRepository = prescriptionRepository;
        this.prescriptionDetailRepository = prescriptionDetailRepository;
        this.examSymptomRepository = examSymptomRepository;
    }

    @GetMapping("/examine/patients")
    public String patientHistoryPage(
            @RequestParam(value = "patientId", required = false) String patientId,
            HttpSession session,
            Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !"DOC".equalsIgnoreCase(loggedInUser.getRole().getRoleId())) {
            return "redirect:/login";
        }

        String doctorId = loggedInUser.getUserId();
        Profile profile = profileService.findById(doctorId).orElse(null);
        model.addAttribute("doctorProfile", profile);

        if (patientId != null) {
            session.setAttribute("selectedPatientId", patientId);
        }

        String selectedPatientId = (String) session.getAttribute("selectedPatientId");
        if (selectedPatientId == null) {
            return "redirect:/doctor/dashboard";
        }

        Patient patient = patientService.findById(selectedPatientId).orElse(null);
        if (patient == null) {
            return "redirect:/doctor/dashboard";
        }
        model.addAttribute("patient", patient);

        // Nạp lịch sử các ca khám (Timeline) của bệnh nhân này
        List<ClinicalExamination> timeline = clinicalExaminationService.findByPatientId(selectedPatientId);
        model.addAttribute("timeline", timeline);

        // Nạp chỉ số glucose 6 tháng gần nhất để vẽ biểu đồ Canvas
        List<LabResult> glucoseResults = labResultRepository
                .findByLabOrder_ClinicalExamination_Patient_UserIdAndLabTest_TestNameContainingIgnoreCaseOrderByLabOrder_ClinicalExamination_ExamDateAsc(
                        selectedPatientId, "Fasting Blood Glucose");

        List<BigDecimal> glucoseTrend = glucoseResults.stream()
                .map(LabResult::getResultValue)
                .collect(Collectors.toList());

        // Nếu chưa có xét nghiệm thực tế trong DB, nạp dữ liệu mặc định để vẽ biểu đồ
        if (glucoseTrend.isEmpty()) {
            glucoseTrend = Arrays.asList(
                    BigDecimal.valueOf(6.8), BigDecimal.valueOf(7.2), BigDecimal.valueOf(6.5),
                    BigDecimal.valueOf(7.0), BigDecimal.valueOf(6.2), BigDecimal.valueOf(5.8)
            );
        }
        model.addAttribute("glucoseTrend", glucoseTrend);

        return "doctor/patients";
    }

    @GetMapping("/examine/patients/view-exam/{examId}")
    public String viewTimelineExamDetail(@PathVariable("examId") String examId, Model model) {
        ClinicalExamination exam = clinicalExaminationService.findById(examId).orElse(null);
        if (exam == null) {
            return "doctor/patients :: timelineDetail";
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

        return "doctor/patients :: timelineDetail";
    }
}
