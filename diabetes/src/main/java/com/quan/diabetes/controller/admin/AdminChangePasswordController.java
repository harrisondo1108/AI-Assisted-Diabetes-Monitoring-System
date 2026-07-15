package com.quan.diabetes.controller.admin;

import com.quan.diabetes.entity.User;
import com.quan.diabetes.service.user.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Controller
@RequestMapping("/admin/change-password")
public class AdminChangePasswordController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public AdminChangePasswordController(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public String showChangePasswordPage(Model model) {
        return "Admin/change-password";
    }

    @PostMapping
    @Transactional
    public String changePassword(@RequestParam("currentPassword") String currentPassword,
                                 @RequestParam("newPassword") String newPassword,
                                 @RequestParam("confirmPassword") String confirmPassword,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {

        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        // Fetch the fresh user from database
        Optional<User> userOpt = userService.findById(loggedInUser.getUserId());
        if (userOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy tài khoản người dùng.");
            return "redirect:/admin/change-password";
        }

        User user = userOpt.get();

        // 1. Verify current password matches
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Mật khẩu hiện tại không chính xác.");
            return "redirect:/admin/change-password";
        }

        // 2. Validate new password strength
        if (newPassword.length() < 8) {
            redirectAttributes.addFlashAttribute("errorMessage", "Mật khẩu mới phải chứa ít nhất 8 ký tự.");
            return "redirect:/admin/change-password";
        }
        if (!newPassword.matches(".*[a-z].*") || !newPassword.matches(".*[A-Z].*")) {
            redirectAttributes.addFlashAttribute("errorMessage", "Mật khẩu mới phải bao gồm cả chữ hoa và chữ thường.");
            return "redirect:/admin/change-password";
        }
        if (!newPassword.matches(".*\\d.*")) {
            redirectAttributes.addFlashAttribute("errorMessage", "Mật khẩu mới phải chứa ít nhất một chữ số.");
            return "redirect:/admin/change-password";
        }
        if (!newPassword.matches(".*[!@#\\$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?~].*")) {
            redirectAttributes.addFlashAttribute("errorMessage", "Mật khẩu mới phải chứa ký tự đặc biệt (!@#$).");
            return "redirect:/admin/change-password";
        }

        // 3. Verify new passwords match
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Mật khẩu xác nhận không khớp.");
            return "redirect:/admin/change-password";
        }

        // 4. Update password
        userService.changePassword(user.getUserId(), newPassword);

        // Update user in session
        userService.findById(user.getUserId()).ifPresent(updatedUser -> {
            session.setAttribute("loggedInUser", updatedUser);
        });

        redirectAttributes.addFlashAttribute("successMessage", "Đổi mật khẩu thành công!");
        return "redirect:/admin/change-password";
    }
}
