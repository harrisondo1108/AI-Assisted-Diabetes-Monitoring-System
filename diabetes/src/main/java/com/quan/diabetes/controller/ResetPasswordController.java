package com.quan.diabetes.controller;

import com.quan.diabetes.entity.User;
import com.quan.diabetes.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Random;

@Controller
public class ResetPasswordController {

    private final UserService userService;

    public ResetPasswordController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "auth/forgot-phone";
    }

    @PostMapping("/forgot-password/send-otp")
    public String sendOtp(@RequestParam("phoneNumber") String phoneNumber,
                          HttpSession session) {

        User user = userService
                .findByPhoneNumber(phoneNumber)
                .orElse(null);

        if (user == null) {
            return "redirect:/forgot-password?error=notfound";
        }

        String otp = String.valueOf(
                100000 + new Random().nextInt(900000)
        );

        session.setAttribute("resetPhoneNumber", phoneNumber);
        session.setAttribute("resetOtp", otp);
        session.setAttribute(
                "otpExpiredAt",
                LocalDateTime.now().plusMinutes(5)
        );

        session.setAttribute("otpVerified", false);

        System.out.println("OTP reset password: " + otp);

        return "redirect:/forgot-password/otp";
    }

    @GetMapping("/forgot-password/otp")
    public String otpPage(HttpSession session) {

        String phoneNumber =
                (String) session.getAttribute("resetPhoneNumber");

        if (phoneNumber == null) {
            return "redirect:/forgot-password";
        }

        return "auth/forgot-otp";
    }

    @PostMapping("/forgot-password/verify-otp")
    public String verifyOtp(@RequestParam("otp") String otp,
                            HttpSession session) {

        String sessionOtp =
                (String) session.getAttribute("resetOtp");

        LocalDateTime expiredAt =
                (LocalDateTime) session.getAttribute("otpExpiredAt");

        if (sessionOtp == null || expiredAt == null) {
            return "redirect:/forgot-password";
        }

        if (LocalDateTime.now().isAfter(expiredAt)) {
            return "redirect:/forgot-password/otp?error=expired";
        }

        if (!sessionOtp.equals(otp)) {
            return "redirect:/forgot-password/otp?error=invalid";
        }

        session.setAttribute("otpVerified", true);

        return "redirect:/forgot-password/reset";
    }

    @GetMapping("/forgot-password/reset")
    public String resetPasswordPage(HttpSession session) {

        Boolean verified =
                (Boolean) session.getAttribute("otpVerified");

        if (verified == null || !verified) {
            return "redirect:/forgot-password";
        }

        return "auth/reset_pass";
    }

    @PostMapping("/forgot-password/reset")
    public String resetPassword(@RequestParam("newPassword") String newPassword,
                                @RequestParam("confirmPassword") String confirmPassword,
                                HttpSession session) {

        Boolean verified =
                (Boolean) session.getAttribute("otpVerified");

        if (verified == null || !verified) {
            return "redirect:/forgot-password";
        }

        if (!newPassword.equals(confirmPassword)) {
            return "redirect:/forgot-password/reset?error=notmatch";
        }

        String phoneNumber =
                (String) session.getAttribute("resetPhoneNumber");

        User user = userService
                .findByPhoneNumber(phoneNumber)
                .orElse(null);

        if (user == null) {
            return "redirect:/forgot-password/reset?error=failed";
        }

        user.setPasswordHash(newPassword);

        userService.update(user.getUserId(), user);

        session.invalidate();

        return "redirect:/login?resetSuccess=true";
    }
}