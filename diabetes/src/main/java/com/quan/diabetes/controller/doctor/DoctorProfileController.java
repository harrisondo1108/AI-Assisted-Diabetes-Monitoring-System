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
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;

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

    @GetMapping({"/profile", "/profile/"})
    public String profilePage(HttpSession session, Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !"DOC".equalsIgnoreCase(loggedInUser.getRole().getRoleId())) {
            return "redirect:/login";
        }

        String doctorId = loggedInUser.getUserId();

        // Removed active examination lock based on user request

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

        // Removed active examination lock based on user request

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

            if (form.getRoomId() != null) {
                Room room = roomRepository.findById(form.getRoomId()).orElse(null);
                profile.setRoom(room);
            } else {
                profile.setRoom(null);
            }

            profileRepository.save(profile);

            // Update session userProfile so changes reflect immediately in the header
            session.setAttribute("userProfile", profile);

            redirectAttributes.addFlashAttribute("successMsg", "Cập nhật thông tin hồ sơ thành công!");
        }

        return "redirect:/doctor/profile";
    }

    @PostMapping("/profile/change-password")
    @ResponseBody
    public org.springframework.http.ResponseEntity<Map<String, Object>> changePassword(
            @RequestParam("currentPassword") String currentPassword,
            @RequestParam("newPassword") String newPassword,
            @RequestParam("confirmPassword") String confirmPassword,
            HttpSession session) {

        Map<String, Object> response = new java.util.HashMap<>();

        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !"DOC".equalsIgnoreCase(loggedInUser.getRole().getRoleId())) {
            response.put("success", false);
            response.put("message", "Phiên làm việc đã hết hạn. Vui lòng đăng nhập lại.");
            return org.springframework.http.ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED).body(response);
        }

        String doctorId = loggedInUser.getUserId();

        // 1. Kiểm tra không được để trống
        if (currentPassword == null || currentPassword.trim().isEmpty() ||
            newPassword == null || newPassword.trim().isEmpty() ||
            confirmPassword == null || confirmPassword.trim().isEmpty()) {
            response.put("success", false);
            response.put("message", "Tất cả các trường mật khẩu bắt buộc phải điền và không được để trống.");
            return org.springframework.http.ResponseEntity.badRequest().body(response);
        }

        // Fetch latest User details
        User user = userRepository.findById(doctorId).orElse(null);
        if (user == null) {
            response.put("success", false);
            response.put("message", "Không tìm thấy tài khoản người dùng.");
            return org.springframework.http.ResponseEntity.badRequest().body(response);
        }

        // 2. Xác minh mật khẩu hiện tại bằng BCrypt matches
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            response.put("success", false);
            response.put("message", "Mật khẩu hiện tại không chính xác.");
            return org.springframework.http.ResponseEntity.badRequest().body(response);
        }

        // 3. Kiểm tra mật khẩu mới và xác nhận mật khẩu mới trùng khớp
        if (!newPassword.equals(confirmPassword)) {
            response.put("success", false);
            response.put("message", "Mật khẩu mới và mật khẩu xác nhận không trùng khớp.");
            return org.springframework.http.ResponseEntity.badRequest().body(response);
        }

        // 4. Kiểm tra độ mạnh mật khẩu mới: sử dụng ParseUtil.isValidPassword
        if (!com.quan.diabetes.util.ParseUtil.isValidPassword(newPassword)) {
            response.put("success", false);
            response.put("message", "Mật khẩu mới phải có độ dài tối thiểu 8 ký tự, chứa ít nhất 1 chữ hoa, 1 chữ thường, 1 chữ số và 1 ký tự đặc biệt (!@#$).");
            return org.springframework.http.ResponseEntity.badRequest().body(response);
        }

        // 5. Không cho phép mật khẩu mới trùng với mật khẩu hiện tại
        if (passwordEncoder.matches(newPassword, user.getPasswordHash())) {
            response.put("success", false);
            response.put("message", "Mật khẩu mới không được trùng với mật khẩu hiện tại.");
            return org.springframework.http.ResponseEntity.badRequest().body(response);
        }

        // 6. Mã hóa mật khẩu mới bằng BCrypt và cập nhật vào CSDL
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Đồng bộ lại loggedInUser trong session để giữ nguyên session đăng nhập
        session.setAttribute("loggedInUser", user);

        response.put("success", true);
        response.put("message", "Đổi mật khẩu thành công!");
        return org.springframework.http.ResponseEntity.ok(response);
    }
}
