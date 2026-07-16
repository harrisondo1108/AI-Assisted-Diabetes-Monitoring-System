package com.quan.diabetes.controller.doctor;

import com.quan.diabetes.entity.*;
import com.quan.diabetes.repository.*;
import com.quan.diabetes.dto.doctor.DoctorProfileForm;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.quan.diabetes.service.cloudinary.CloudinaryService;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

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

        Profile profile = profileRepository.findById(doctorId).orElse(null);
        if (profile == null) {
            return "redirect:/doctor/queue";
        }

        model.addAttribute("doctorProfile", profile);
        model.addAttribute("rooms", roomRepository.findAll());

        return "doctor/profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(
            @Valid @ModelAttribute("profileForm") DoctorProfileForm form,
            BindingResult bindingResult,
            @RequestParam(value = "avatarFile", required = false) MultipartFile avatarFile,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !"DOC".equalsIgnoreCase(loggedInUser.getRole().getRoleId())) {
            return "redirect:/login";
        }

        String doctorId = loggedInUser.getUserId();

        if (bindingResult.hasErrors()) {
            String errorMsg = bindingResult.getFieldError().getDefaultMessage();
            redirectAttributes.addFlashAttribute("errorMsg", errorMsg);
            return "redirect:/doctor/profile";
        }

        Profile profile = profileRepository.findById(doctorId).orElse(null);
        if (profile != null) {
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

            profile.setFullName(form.getFullName().trim());
            profile.setAddress(form.getAddress() != null ? form.getAddress().trim() : "");
            profile.setSpecialty(form.getSpecialty() != null ? form.getSpecialty().trim() : "");
            profile.setGender(form.getGender());
            profile.setEmail(form.getEmail() != null ? form.getEmail().trim() : "");

            if (form.getDob() != null && !form.getDob().trim().isEmpty()) {
                try {
                    profile.setDob(LocalDate.parse(form.getDob().trim()));
                } catch (Exception e) {
                    // Ignore parsing errors
                }
            } else {
                profile.setDob(null);
            }

            Room room = roomRepository.findById(form.getRoomId()).orElse(null);
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

        // 4. Kiểm tra độ mạnh mật khẩu mới: sử dụng ParseUtil.isValidPassword
        if (!com.quan.diabetes.util.ParseUtil.isValidPassword(newPassword)) {
            redirectAttributes.addFlashAttribute("errorMsg", "Mật khẩu mới phải có độ dài tối thiểu 8 ký tự, chứa ít nhất 1 chữ hoa, 1 chữ thường, 1 chữ số và 1 ký tự đặc biệt (!@#$).");
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
