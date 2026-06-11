package com.quan.diabetes.controller.doctor;

import com.quan.diabetes.dto.ClinicalExamForm;
import com.quan.diabetes.entity.*;
import com.quan.diabetes.repository.*;
import com.quan.diabetes.service.ClinicalExaminationService;
import com.quan.diabetes.service.PatientService;
import com.quan.diabetes.service.ProfileService;
import com.quan.diabetes.service.PatientRoutineService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/doctor")
public class DoctorExamineController {

    private final ClinicalExaminationService clinicalExaminationService;
    private final PatientService patientService;
    private final ProfileService profileService;
    private final SymptomsCatalogRepository symptomsCatalogRepository;
    private final LabTestCatalogRepository labTestCatalogRepository;
    private final MedicationRepository medicationRepository;
    private final ClinicalExaminationRepository clinicalExaminationRepository;
    private final LabResultRepository labResultRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final PrescriptionDetailRepository prescriptionDetailRepository;
    private final TreatmentPlanRepository treatmentPlanRepository;
    private final ExamSymptomRepository examSymptomRepository;
    private final PatientRoutineService patientRoutineService;

    public DoctorExamineController(
            ClinicalExaminationService clinicalExaminationService,
            PatientService patientService,
            ProfileService profileService,
            SymptomsCatalogRepository symptomsCatalogRepository,
            LabTestCatalogRepository labTestCatalogRepository,
            MedicationRepository medicationRepository,
            ClinicalExaminationRepository clinicalExaminationRepository,
            LabResultRepository labResultRepository,
            PrescriptionRepository prescriptionRepository,
            PrescriptionDetailRepository prescriptionDetailRepository,
            TreatmentPlanRepository treatmentPlanRepository,
            ExamSymptomRepository examSymptomRepository,
            PatientRoutineService patientRoutineService) {
        this.clinicalExaminationService = clinicalExaminationService;
        this.patientService = patientService;
        this.profileService = profileService;
        this.symptomsCatalogRepository = symptomsCatalogRepository;
        this.labTestCatalogRepository = labTestCatalogRepository;
        this.medicationRepository = medicationRepository;
        this.clinicalExaminationRepository = clinicalExaminationRepository;
        this.labResultRepository = labResultRepository;
        this.prescriptionRepository = prescriptionRepository;
        this.prescriptionDetailRepository = prescriptionDetailRepository;
        this.treatmentPlanRepository = treatmentPlanRepository;
        this.examSymptomRepository = examSymptomRepository;
        this.patientRoutineService = patientRoutineService;
    }

    @GetMapping("/examine")
    public String examinePage(
            @RequestParam(value = "patientId", required = false) String patientId,
            @RequestParam(value = "viewOnly", required = false) Boolean viewOnlyParam,
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
        if (viewOnlyParam != null) {
            session.setAttribute("examineViewOnly", viewOnlyParam ? "true" : "false");
        }

        // Lấy selectedPatientId từ session
        String selectedPatientId = (String) session.getAttribute("selectedPatientId");
        if (selectedPatientId == null) {
            return "redirect:/doctor/dashboard";
        }

        Patient patient = patientService.findById(selectedPatientId).orElse(null);
        if (patient == null) {
            return "redirect:/doctor/dashboard";
        }
        model.addAttribute("patient", patient);
        model.addAttribute("examForm", new ClinicalExamForm());

        // Lấy thông tin Routine của bệnh nhân
        model.addAttribute("routine", patientRoutineService.findById(selectedPatientId).orElse(null));

        // Lấy danh mục triệu chứng (Active)
        List<SymptomsCatalog> symptomsCatalog = symptomsCatalogRepository.findAll().stream()
                .filter(s -> s.getStatus() == null || s.getStatus())
                .collect(Collectors.toList());
        model.addAttribute("symptomsCatalog", symptomsCatalog);

        // Lấy danh mục xét nghiệm (Active)
        List<LabTestCatalog> labTestsCatalog = labTestCatalogRepository.findAll().stream()
                .filter(l -> l.getStatus() == null || l.getStatus())
                .collect(Collectors.toList());
        model.addAttribute("labTestsCatalog", labTestsCatalog);

        // Lấy danh mục thuốc (Active) cho ô tìm kiếm autocomplete
        List<Medication> medications = medicationRepository.findAllActiveList();
        model.addAttribute("medications", medications);

        // Kiểm tra xem ca khám có đang ở trạng thái Pending hoặc InProgress hay không
        ClinicalExamination activeExam = clinicalExaminationRepository
                .findFirstByPatient_UserIdAndDoctor_UserIdAndStatusIn(
                        selectedPatientId, doctorId, List.of("Pending", "InProgress"))
                .orElse(null);
        model.addAttribute("activeExam", activeExam);

        boolean viewOnly = "true".equals(session.getAttribute("examineViewOnly"));
        model.addAttribute("viewOnly", viewOnly);

        // Nếu là chế độ chỉ xem, nạp thông tin ca khám cũ
        if (viewOnly) {
            // Lấy ca khám Completed/Cancelled gần nhất
            ClinicalExamination lastExam = clinicalExaminationRepository.findByPatient_UserIdOrderByExamDateDesc(selectedPatientId).stream()
                    .filter(e -> "Completed".equalsIgnoreCase(e.getStatus()) || "Cancelled".equalsIgnoreCase(e.getStatus()))
                    .findFirst()
                    .orElse(null);

            if (lastExam != null) {
                model.addAttribute("lastExam", lastExam);

                // Nạp triệu chứng đã khám
                List<ExamSymptom> chosenSymptoms = examSymptomRepository.findAll().stream()
                        .filter(s -> lastExam.getClinicalExamId().equals(s.getId().getClinicalExamId()))
                        .collect(Collectors.toList());
                List<String> chosenSymptomIds = chosenSymptoms.stream()
                        .map(s -> s.getSymptom().getSymptomId())
                        .collect(Collectors.toList());
                model.addAttribute("chosenSymptomIds", chosenSymptomIds);

                // Nạp kết quả xét nghiệm liên quan
                List<LabResult> labResults = labResultRepository.findByLabOrder_ClinicalExamination_ClinicalExamId(lastExam.getClinicalExamId());
                model.addAttribute("lastExamLabResults", labResults);

                // Nạp chi tiết đơn thuốc
                Prescription prescription = prescriptionRepository.findByClinicalExamination_ClinicalExamId(lastExam.getClinicalExamId()).orElse(null);
                if (prescription != null) {
                    List<PrescriptionDetail> details = prescriptionDetailRepository.findByPrescription_PrescriptionId(prescription.getPrescriptionId());
                    model.addAttribute("lastExamPrescriptionDetails", details);
                } else {
                    model.addAttribute("lastExamPrescriptionDetails", Collections.emptyList());
                }
            }
        }

        return "doctor/examine";
    }

    @PostMapping("/examine/{patientId}/start")
    public String startExam(@PathVariable("patientId") String patientId, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !"DOC".equalsIgnoreCase(loggedInUser.getRole().getRoleId())) {
            return "redirect:/login";
        }

        clinicalExaminationService.startExamination(patientId, loggedInUser.getUserId());
        session.setAttribute("selectedPatientId", patientId);
        session.setAttribute("examineViewOnly", "false");
        return "redirect:/doctor/examine";
    }

    @PostMapping("/examine/{patientId}/cancel")
    public String cancelExam(
            @PathVariable("patientId") String patientId,
            @RequestParam("reason") String reason,
            HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !"DOC".equalsIgnoreCase(loggedInUser.getRole().getRoleId())) {
            return "redirect:/login";
        }

        clinicalExaminationService.cancelExamination(patientId, reason, loggedInUser.getUserId());
        return "redirect:/doctor/dashboard";
    }

    @PostMapping("/examine/{patientId}/submit")
    public String submitExam(
            @PathVariable("patientId") String patientId,
            @ModelAttribute("examForm") ClinicalExamForm form,
            HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !"DOC".equalsIgnoreCase(loggedInUser.getRole().getRoleId())) {
            return "redirect:/login";
        }

        clinicalExaminationService.submitExamination(patientId, form, loggedInUser.getUserId());
        return "redirect:/doctor/dashboard";
    }
}
