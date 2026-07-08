package com.quan.diabetes.controller.doctor;

import com.quan.diabetes.entity.*;
import com.quan.diabetes.repository.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.quan.diabetes.service.cloudinary.CloudinaryService;
import org.springframework.web.multipart.MultipartFile;

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
    private final CloudinaryService cloudinaryService;

    public DoctorProfileController(
            ProfileRepository profileRepository,
            RoomRepository roomRepository,
            ClinicalExaminationRepository clinicalExaminationRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            CloudinaryService cloudinaryService) {
        this.profileRepository = profileRepository;
        this.roomRepository = roomRepository;
        this.clinicalExaminationRepository = clinicalExaminationRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.cloudinaryService = cloudinaryService;
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
            @RequestParam(value = "avatarFile", required = false) MultipartFile avatarFile,
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
                redirectAttributes.addFlashAttribute("errorMsg", "Họ và tên bắt buộc phải điền.");
                return "redirect:/doctor/profile";
            }

            // Handle file upload to Cloudinary
            if (avatarFile != null && !avatarFile.isEmpty()) {
                try {
                    String imageUrl = cloudinaryService.uploadFile(avatarFile);
                    if (imageUrl != null) {
                        profile.setImageUrl(imageUrl);
                    }
                } catch (Exception e) {
                    redirectAttributes.addFlashAttribute("errorMsg", "Tải lên ảnh đại diện thất bại: " + e.getMessage());
                    return "redirect:/doctor/profile";
                }
            }

            if (fullName.trim().length() < 2 || fullName.trim().length() > 60) {
                redirectAttributes.addFlashAttribute("errorMsg", "Full Name must be between 2 and 60 characters.");
                return "redirect:/doctor/profile";
            }

            if (!fullName.trim().matches("^[A-Za-zÀ-ỹ\\s]+$")) {
                redirectAttributes.addFlashAttribute("errorMsg", "Full Name can only contain letters and spaces.");
                return "redirect:/doctor/profile";
            }

            if (address != null && address.trim().length() > 200) {
                redirectAttributes.addFlashAttribute("errorMsg", "Address cannot exceed 200 characters.");
                return "redirect:/doctor/profile";
            }

            if (specialty != null && specialty.trim().length() > 60) {
                redirectAttributes.addFlashAttribute("errorMsg", "Specialty cannot exceed 60 characters.");
                return "redirect:/doctor/profile";
            }

            if (fullName.trim().length() < 2 || fullName.trim().length() > 60) {
                redirectAttributes.addFlashAttribute("errorMsg", "Full Name must be between 2 and 60 characters.");
                return "redirect:/doctor/profile";
            }

            if (!fullName.trim().matches("^[A-Za-zÀ-ỹ\\s]+$")) {
                redirectAttributes.addFlashAttribute("errorMsg", "Full Name can only contain letters and spaces.");
                return "redirect:/doctor/profile";
            }

            if (address != null && address.trim().length() > 200) {
                redirectAttributes.addFlashAttribute("errorMsg", "Address cannot exceed 200 characters.");
                return "redirect:/doctor/profile";
            }

            if (specialty != null && specialty.trim().length() > 60) {
                redirectAttributes.addFlashAttribute("errorMsg", "Specialty cannot exceed 60 characters.");
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

            redirectAttributes.addFlashAttribute("successMsg", "Cập nhật thông tin hồ sơ thành công!");
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
            redirectAttributes.addFlashAttribute("errorMsg", "Tất cả các trường mật khẩu bắt buộc phải điền và không được để trống.");
            return "redirect:/doctor/profile";
        }

        // Fetch latest User details
        User user = userRepository.findById(doctorId).orElse(null);
        if (user == null) {
            redirectAttributes.addFlashAttribute("errorMsg", "Không tìm thấy tài khoản người dùng.");
            return "redirect:/doctor/profile";
        }

        // 2. Xác minh mật khẩu hiện tại bằng BCrypt matches
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            redirectAttributes.addFlashAttribute("errorMsg", "Mật khẩu hiện tại không chính xác.");
            return "redirect:/doctor/profile";
        }

        // 3. Kiểm tra mật khẩu mới và xác nhận mật khẩu mới phải giống nhau
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("errorMsg", "Mật khẩu mới và mật khẩu xác nhận không trùng khớp.");
            return "redirect:/doctor/profile";
        }

        // 4. Kiểm tra độ mạnh mật khẩu mới: Tối thiểu 6 ký tự
        if (newPassword.length() < 6) {
            redirectAttributes.addFlashAttribute("errorMsg", "Mật khẩu mới phải có độ dài tối thiểu 6 ký tự.");
            return "redirect:/doctor/profile";
        }

        // 5. Không cho phép mật khẩu mới trùng với mật khẩu hiện tại
        if (passwordEncoder.matches(newPassword, user.getPasswordHash())) {
            redirectAttributes.addFlashAttribute("errorMsg", "Mật khẩu mới không được trùng với mật khẩu hiện tại.");
            return "redirect:/doctor/profile";
        }

        // 6. Mã hóa mật khẩu mới bằng BCrypt và cập nhật vào CSDL
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Đồng bộ lại loggedInUser
        session.setAttribute("loggedInUser", user);

        redirectAttributes.addFlashAttribute("successMsg", "Đổi mật khẩu thành công.");
        return "redirect:/doctor/profile";
    }
}
