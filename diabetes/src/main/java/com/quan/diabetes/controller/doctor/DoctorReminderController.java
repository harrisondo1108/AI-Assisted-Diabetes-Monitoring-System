package com.quan.diabetes.controller.doctor;

import com.quan.diabetes.entity.Reminder;
import com.quan.diabetes.entity.ClinicalExamination;
import com.quan.diabetes.entity.Patient;
import com.quan.diabetes.entity.Profile;
import com.quan.diabetes.entity.User;
import com.quan.diabetes.service.ai.ReminderService;
import com.quan.diabetes.service.exam.ClinicalExaminationService;
import com.quan.diabetes.service.user.ProfileService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/doctor/reminders")
public class DoctorReminderController {

    private final ReminderService reminderService;
    private final ProfileService profileService;
    private final ClinicalExaminationService clinicalExaminationService;

    public DoctorReminderController(
            ReminderService reminderService,
            ProfileService profileService,
            ClinicalExaminationService clinicalExaminationService) {
        this.reminderService = reminderService;
        this.profileService = profileService;
        this.clinicalExaminationService = clinicalExaminationService;
    }

    private boolean checkDoctorSession(HttpSession session, Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !"DOC".equalsIgnoreCase(loggedInUser.getRole().getRoleId())) {
            return false;
        }

        String doctorId = loggedInUser.getUserId();
        Profile profile = profileService.findById(doctorId).orElse(null);
        if (profile != null) {
            model.addAttribute("doctorProfile", profile);
            session.setAttribute("userProfile", profile);
        }
        return true;
    }

    @GetMapping
    public String remindersList(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "search", defaultValue = "") String search,
            HttpSession session,
            Model model) {
        if (!checkDoctorSession(session, model)) {
            return "redirect:/login";
        }

        List<Patient> patientsWithReminders = reminderService.getPatientsWithRemindersToday();

        // Lọc tìm kiếm
        if (search != null && !search.trim().isEmpty()) {
            String query = search.trim().toLowerCase();
            patientsWithReminders = patientsWithReminders.stream()
                    .filter(p -> (p.getFullName() != null && p.getFullName().toLowerCase().contains(query))
                            || (p.getUserId() != null && p.getUserId().toLowerCase().contains(query)))
                    .collect(Collectors.toList());
        }

        // Tạo danh sách DTO chứa thêm examDate
        List<PatientReminderView> viewList = new ArrayList<>();
        for (Patient patient : patientsWithReminders) {
            List<ClinicalExamination> exams = clinicalExaminationService.findByPatientId(patient.getUserId());
            LocalDateTime latestExamDate = exams.stream()
                    .map(ClinicalExamination::getExamDate)
                    .filter(Objects::nonNull)
                    .max(LocalDateTime::compareTo)
                    .orElse(null);
            viewList.add(new PatientReminderView(patient, latestExamDate));
        }

        // Phân trang
        int pageSize = 10;
        int totalElements = viewList.size();
        int totalPages = (int) Math.ceil((double) totalElements / pageSize);
        if (totalPages == 0) totalPages = 1;

        if (page < 1) page = 1;
        if (page > totalPages) page = totalPages;

        int fromIndex = (page - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, totalElements);
        List<PatientReminderView> pagedList = viewList.isEmpty() ? new ArrayList<>() : viewList.subList(fromIndex, toIndex);

        model.addAttribute("patientsList", pagedList);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("currentSearch", search);

        return "doctor/doctor_reminders_list";
    }

    @GetMapping("/{patientId}")
    public String patientRemindersDetail(
            @PathVariable("patientId") String patientId,
            HttpSession session,
            Model model) {
        if (!checkDoctorSession(session, model)) {
            return "redirect:/login";
        }

        List<Reminder> patientReminders = reminderService.getPatientRemindersToday(patientId);
        model.addAttribute("reminders", patientReminders);
        
        if (!patientReminders.isEmpty()) {
            model.addAttribute("patient", patientReminders.get(0).getPatient());
        }

        return "doctor/doctor_reminders_detail";
    }

    public static class PatientReminderView {
        private Patient patient;
        private LocalDateTime latestExamDate;

        public PatientReminderView(Patient patient, LocalDateTime latestExamDate) {
            this.patient = patient;
            this.latestExamDate = latestExamDate;
        }

        public Patient getPatient() {
            return patient;
        }

        public LocalDateTime getLatestExamDate() {
            return latestExamDate;
        }
    }
}
