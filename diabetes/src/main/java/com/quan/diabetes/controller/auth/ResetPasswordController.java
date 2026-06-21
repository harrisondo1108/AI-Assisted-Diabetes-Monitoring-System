package com.quan.diabetes.controller.auth;

import com.quan.diabetes.entity.User;
import com.quan.diabetes.service.user.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.Random;

@Controller
public class ResetPasswordController {

    private final UserService userService;

    public ResetPasswordController(UserService userService) {
        this.userService = userService;
    }

    // ==================== GET ====================

    @GetMapping("/forgot-password")
    public String forgotPhonePage() {
        return "auth/forgot-phone";
    }

    @GetMapping("/forgot-password/otp")
    public String forgotOtpPage(HttpSession session) {
        if (session.getAttribute("resetPhoneNumber") == null) {
            return "redirect:/forgot-password";
        }
        return "auth/forgot-otp";
    }

    @GetMapping("/forgot-password/reset")
    public String resetPasswordPage(HttpSession session) {
        Boolean verified = (Boolean) session.getAttribute("otpVerified");
        if (verified == null || !verified) {
            return "redirect:/forgot-password";
        }
        return "auth/reset_pass";
    }

    // ==================== POST ====================

    @PostMapping("/forgot-password/send-otp")
    public String sendOtp(@RequestParam String phoneNumber,
                          HttpSession session,
                          Model model) {
        User user = userService.findByPhoneNumber(phoneNumber).orElse(null);

        if (user == null) {
            model.addAttribute("errorMsg", "The phone number does not exist in the system.");
            return "auth/forgot-phone";
        }

        String otp = String.valueOf(100000 + new Random().nextInt(900000));
        session.setAttribute("resetPhoneNumber", phoneNumber);
        session.setAttribute("resetOtp", otp);
        session.setAttribute("otpExpiredAt", LocalDateTime.now().plusMinutes(5));
        session.setAttribute("otpVerified", false);

        System.out.println("OTP reset password: " + otp);

        return "redirect:/forgot-password/otp";
    }

    @PostMapping("/forgot-password/verify-otp")
    public String verifyOtp(@RequestParam String otp,
                            HttpSession session,
                            Model model) {
        String sessionOtp = (String) session.getAttribute("resetOtp");
        LocalDateTime expiredAt = (LocalDateTime) session.getAttribute("otpExpiredAt");

        if (sessionOtp == null || expiredAt == null) {
            model.addAttribute("errorMsg", "Phiên xác thực không hợp lệ");
            return "auth/forgot-otp";
        }

        if (LocalDateTime.now().isAfter(expiredAt)) {
            model.addAttribute("errorMsg", "The OTP code has expired.");
            return "auth/forgot-otp";
        }

        if (!sessionOtp.equals(otp)) {
            model.addAttribute("errorMsg", "Incorrect OTP code");
            return "auth/forgot-otp";
        }

        session.setAttribute("otpVerified", true);
        return "redirect:/forgot-password/reset";
    }

    @PostMapping("/forgot-password/reset")
    public String resetPassword(@RequestParam String newPassword,
                                @RequestParam String confirmPassword,
                                HttpSession session,
                                Model model) {
        Boolean verified = (Boolean) session.getAttribute("otpVerified");
        if (verified == null || !verified) {
            return "redirect:/forgot-password";
        }

        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("errorMsg", "The verification password does not match.");
            return "auth/reset_pass";
        }

        String phoneNumber = (String) session.getAttribute("resetPhoneNumber");
        User user = userService.findByPhoneNumber(phoneNumber).orElse(null);
        if (user == null) {
            model.addAttribute("errorMsg", "Account not found");
            return "auth/reset_pass";
        }

        user.setPasswordHash(newPassword);
        userService.update(user.getUserId(), user);
        session.invalidate();

        return "redirect:/login?resetSuccess=true";
    }
}
