package com.quan.diabetes.controller.doctor;

import com.quan.diabetes.entity.*;
import com.quan.diabetes.repository.*;


import com.quan.diabetes.service.exam.ClinicalExaminationService;
import com.quan.diabetes.service.user.PatientService;
import com.quan.diabetes.service.user.ProfileService;
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
    private final ClinicalExaminationRepository clinicalExaminationRepository;

    public DoctorPatientController(
            ClinicalExaminationService clinicalExaminationService,
            PatientService patientService,
            ProfileService profileService,
            LabResultRepository labResultRepository,
            PrescriptionRepository prescriptionRepository,
            PrescriptionDetailRepository prescriptionDetailRepository,
            ExamSymptomRepository examSymptomRepository,
            ClinicalExaminationRepository clinicalExaminationRepository) {
        this.clinicalExaminationService = clinicalExaminationService;
        this.patientService = patientService;
        this.profileService = profileService;
        this.labResultRepository = labResultRepository;
        this.prescriptionRepository = prescriptionRepository;
        this.prescriptionDetailRepository = prescriptionDetailRepository;
        this.examSymptomRepository = examSymptomRepository;
        this.clinicalExaminationRepository = clinicalExaminationRepository;
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

        // Check if there is an in-progress exam for this doctor in the database
        Optional<ClinicalExamination> activeExam = clinicalExaminationRepository
                .findFirstByDoctor_UserIdAndStatus(doctorId, "InProgress");
        model.addAttribute("hasActiveExam", activeExam.isPresent());

        if (activeExam.isPresent()) {
            patientId = activeExam.get().getPatient().getUserId();
            session.setAttribute("selectedPatientId", patientId);
        } else {
            if (patientId != null) {
                session.setAttribute("selectedPatientId", patientId);
            }
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

        // Nạp lịch sử các ca khám (Timeline) của bệnh nhân này (Chỉ lấy Completed hoặc Cancelled)
        List<ClinicalExamination> timeline = clinicalExaminationService.findByPatientId(selectedPatientId).stream()
                .filter(e -> "Completed".equalsIgnoreCase(e.getStatus()) || "Cancelled".equalsIgnoreCase(e.getStatus()))
                .collect(Collectors.toList());
        model.addAttribute("timeline", timeline);

        // Nạp chỉ số glucose các ca khám để vẽ biểu đồ Canvas
        List<LabResult> glucoseResults = labResultRepository
                .findByLabOrder_ClinicalExamination_Patient_UserIdAndLabTest_TestNameContainingIgnoreCaseOrderByLabOrder_ClinicalExamination_ExamDateAsc(
                        selectedPatientId, "Đường huyết lúc đói");

        // Lấy tối đa 4 kết quả gần nhất, nếu không đủ 4 kết quả thì truyền danh sách rỗng (không vẽ biểu đồ)
        List<Map<String, Object>> glucoseTrend = new ArrayList<>();
        if (glucoseResults.size() >= 4) {
            List<LabResult> recentResults = glucoseResults.subList(glucoseResults.size() - 4, glucoseResults.size());
            for (LabResult res : recentResults) {
                Map<String, Object> map = new HashMap<>();
                map.put("val", res.getResultValue());
                String dateStr = res.getLabOrder().getClinicalExamination().getExamDate() != null
                        ? res.getLabOrder().getClinicalExamination().getExamDate().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM"))
                        : "";
                map.put("date", dateStr);
                glucoseTrend.add(map);
            }
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
