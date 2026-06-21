package com.quan.diabetes.controller.doctor;

import com.quan.diabetes.entity.*;
import com.quan.diabetes.repository.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;

@Controller
@RequestMapping("/doctor")
public class DoctorProfileController {

    private final ProfileRepository profileRepository;
    private final RoomRepository roomRepository;
    private final ClinicalExaminationRepository clinicalExaminationRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DoctorProfileController(
            ProfileRepository profileRepository,
            RoomRepository roomRepository,
            ClinicalExaminationRepository clinicalExaminationRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.profileRepository = profileRepository;
        this.roomRepository = roomRepository;
        this.clinicalExaminationRepository = clinicalExaminationRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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

    @PostMapping("/profile/change-password")
    public String changePassword(
            @RequestParam("currentPassword") String currentPassword,
            @RequestParam("newPassword") String newPassword,
            @RequestParam("confirmPassword") String confirmPassword,
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

        // 1. Kiểm tra không được để trống
        if (currentPassword == null || currentPassword.trim().isEmpty() ||
            newPassword == null || newPassword.trim().isEmpty() ||
            confirmPassword == null || confirmPassword.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMsg", "All fields are required and cannot be empty.");
            return "redirect:/doctor/profile";
        }

        // Fetch latest User details
        User user = userRepository.findById(doctorId).orElse(null);
        if (user == null) {
            redirectAttributes.addFlashAttribute("errorMsg", "User not found.");
            return "redirect:/doctor/profile";
        }

        // 2. Xác minh mật khẩu hiện tại bằng BCrypt matches
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            redirectAttributes.addFlashAttribute("errorMsg", "Current password is incorrect.");
            return "redirect:/doctor/profile";
        }

        // 3. Kiểm tra mật khẩu mới và xác nhận mật khẩu mới phải giống nhau
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("errorMsg", "New password and confirmation password do not match.");
            return "redirect:/doctor/profile";
        }

        // 4. Kiểm tra độ mạnh mật khẩu mới: Tối thiểu 6 ký tự
        if (newPassword.length() < 6) {
            redirectAttributes.addFlashAttribute("errorMsg", "New password must be at least 6 characters.");
            return "redirect:/doctor/profile";
        }

        // 5. Không cho phép mật khẩu mới trùng với mật khẩu hiện tại
        if (passwordEncoder.matches(newPassword, user.getPasswordHash())) {
            redirectAttributes.addFlashAttribute("errorMsg", "New password cannot be the same as current password.");
            return "redirect:/doctor/profile";
        }

        // 6. Mã hóa mật khẩu mới bằng BCrypt và cập nhật vào CSDL
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Đồng bộ lại loggedInUser
        session.setAttribute("loggedInUser", user);

        redirectAttributes.addFlashAttribute("successMsg", "Password changed successfully.");
        return "redirect:/doctor/profile";
    }
}
