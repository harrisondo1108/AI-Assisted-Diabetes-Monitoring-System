package com.quan.diabetes.controller.doctor;

import com.quan.diabetes.entity.*;
import com.quan.diabetes.repository.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.Optional;

@Controller
@RequestMapping("/doctor")
public class DoctorProfileController {

    private final ProfileRepository profileRepository;
    private final RoomRepository roomRepository;
    private final ClinicalExaminationRepository clinicalExaminationRepository;

    public DoctorProfileController(
            ProfileRepository profileRepository,
            RoomRepository roomRepository,
            ClinicalExaminationRepository clinicalExaminationRepository) {
        this.profileRepository = profileRepository;
        this.roomRepository = roomRepository;
        this.clinicalExaminationRepository = clinicalExaminationRepository;
    }

    @GetMapping("/profile")
    public String profilePage(HttpSession session, Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !"DOC".equalsIgnoreCase(loggedInUser.getRole().getRoleId())) {
            return "redirect:/login";
        }

        String doctorId = loggedInUser.getUserId();

        // Check active examination lock
        Optional<ClinicalExamination> activeExam = clinicalExaminationRepository
                .findFirstByDoctor_UserIdAndStatus(doctorId, "InProgress");
        if (activeExam.isPresent()) {
            return "redirect:/doctor/examine?patientId=" + activeExam.get().getPatient().getUserId() + "&warning=in-progress";
        }

        Profile profile = profileRepository.findById(doctorId).orElse(null);
        if (profile == null) {
            return "redirect:/doctor/dashboard";
        }

        model.addAttribute("doctorProfile", profile);
        model.addAttribute("rooms", roomRepository.findAll());

        return "doctor/profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(
            @RequestParam("fullName") String fullName,
            @RequestParam(value = "dob", required = false) String dobStr,
            @RequestParam("gender") Boolean gender,
            @RequestParam("specialty") String specialty,
            @RequestParam("address") String address,
            @RequestParam("roomId") Integer roomId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !"DOC".equalsIgnoreCase(loggedInUser.getRole().getRoleId())) {
            return "redirect:/login";
        }

        String doctorId = loggedInUser.getUserId();

        // Check active examination lock
        Optional<ClinicalExamination> activeExam = clinicalExaminationRepository
                .findFirstByDoctor_UserIdAndStatus(doctorId, "InProgress");
        if (activeExam.isPresent()) {
            return "redirect:/doctor/examine?patientId=" + activeExam.get().getPatient().getUserId() + "&warning=in-progress";
        }

        Profile profile = profileRepository.findById(doctorId).orElse(null);
        if (profile != null) {
            // Validate inputs
            if (fullName == null || fullName.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMsg", "Full Name is required.");
                return "redirect:/doctor/profile";
            }

            profile.setFullName(fullName.trim());
            profile.setAddress(address != null ? address.trim() : "");
            profile.setSpecialty(specialty != null ? specialty.trim() : "");
            profile.setGender(gender);

            if (dobStr != null && !dobStr.trim().isEmpty()) {
                try {
                    profile.setDob(LocalDate.parse(dobStr));
                } catch (Exception e) {
                    // Ignore parsing errors
                }
            } else {
                profile.setDob(null);
            }

            Room room = roomRepository.findById(roomId).orElse(null);
            if (room != null) {
                profile.setRoom(room);
            }

            profileRepository.save(profile);

            // Update session userProfile so changes reflect immediately in the header
            session.setAttribute("userProfile", profile);

            redirectAttributes.addFlashAttribute("successMsg", "Profile updated successfully!");
        }

        return "redirect:/doctor/profile";
    }
}
