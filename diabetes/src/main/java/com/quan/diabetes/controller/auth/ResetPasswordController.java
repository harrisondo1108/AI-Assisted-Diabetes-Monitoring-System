package com.quan.diabetes.controller.auth;

import com.quan.diabetes.entity.User;
import com.quan.diabetes.service.user.UserService;
import com.quan.diabetes.service.notification.SmsService;
import com.quan.diabetes.service.notification.EmailService;
import com.quan.diabetes.service.systemlog.SystemLogService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.Random;

@Controller
public class ResetPasswordController {
    private static final Logger logger = LoggerFactory.getLogger(ResetPasswordController.class);
    private final UserService userService;
    private final SmsService smsService;
    private final SystemLogService systemLogService;

    @org.springframework.beans.factory.annotation.Autowired
    private EmailService emailService;

    public ResetPasswordController(UserService userService, SmsService smsService, SystemLogService systemLogService) {
        this.userService = userService;
        this.smsService = smsService;
        this.systemLogService = systemLogService;
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
            model.addAttribute("errorMsg", "Số điện thoại không tồn tại trên hệ thống.");
            return "auth/forgot-phone";
        }

        String otp = String.valueOf(100000 + new Random().nextInt(900000));
        session.setAttribute("resetPhoneNumber", phoneNumber);
        session.setAttribute("resetOtp", otp);
        session.setAttribute("otpExpiredAt", LocalDateTime.now().plusMinutes(5));
        session.setAttribute("otpVerified", false);

        System.out.println("OTP reset password: " + otp);
        logger.info("OTP reset password: {} for phone {}", otp, phoneNumber);

        // Gửi OTP qua SMS thực tế
        System.out.println("Gửi SMS tới số " + phoneNumber + ": Mã OTP của bạn là " + otp);
        smsService.sendOtp(phoneNumber, otp);

        String userEmail = null;
        if (user.getPatient() != null) {
            userEmail = user.getPatient().getEmail();
        } else if (user.getProfile() != null) {
            userEmail = user.getProfile().getEmail();
        }
        String recipientEmail = (userEmail != null && !userEmail.trim().isEmpty()) ? userEmail.trim() : "lequan13112005@gmail.com";
        emailService.sendSimpleEmail(recipientEmail, "Mã OTP Khôi phục mật khẩu", "Mã OTP của bạn là: " + otp);

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
            model.addAttribute("errorMsg", "Mã OTP đã hết hạn.");
            return "auth/forgot-otp";
        }

        if (!sessionOtp.equals(otp)) {
            model.addAttribute("errorMsg", "Mã OTP không chính xác.");
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

        if (!com.quan.diabetes.util.ParseUtil.isValidPassword(newPassword)) {
            systemLogService.saveLog(null, "RESET_PASSWORD", "Account", null, "Đặt lại mật khẩu thất bại (Mật khẩu yếu) cho SĐT: " + session.getAttribute("resetPhoneNumber"), null, null, "FAILED");
            model.addAttribute("errorMsg", "Mật khẩu phải chứa ít nhất 8 ký tự, bao gồm chữ hoa, chữ thường, ít nhất một chữ số và ký tự đặc biệt (!@#$).");
            return "auth/reset_pass";
        }

        if (!newPassword.equals(confirmPassword)) {
            systemLogService.saveLog(null, "RESET_PASSWORD", "Account", null, "Đặt lại mật khẩu thất bại (Xác nhận mật khẩu không khớp) cho SĐT: " + session.getAttribute("resetPhoneNumber"), null, null, "FAILED");
            model.addAttribute("errorMsg", "Mật khẩu xác nhận không khớp.");
            return "auth/reset_pass";
        }

        String phoneNumber = (String) session.getAttribute("resetPhoneNumber");
        User user = userService.findByPhoneNumber(phoneNumber).orElse(null);
        if (user == null) {
            systemLogService.saveLog(null, "RESET_PASSWORD", "Account", null, "Đặt lại mật khẩu thất bại (Không tìm thấy tài khoản) cho SĐT: " + phoneNumber, null, null, "FAILED");
            model.addAttribute("errorMsg", "Không tìm thấy tài khoản.");
            return "auth/reset_pass";
        }

        User oldUser = new User();
        oldUser.setUserId(user.getUserId());
        oldUser.setPhoneNumber(user.getPhoneNumber());
        oldUser.setPasswordHash(user.getPasswordHash());
        oldUser.setRole(user.getRole());
        oldUser.setStatus(user.getStatus());

        user.setPasswordHash(newPassword);
        userService.update(user.getUserId(), user);
        session.invalidate();
        
        systemLogService.saveLogWithObject(user.getUserId(), "RESET_PASSWORD", "Account", user.getUserId(), "Reset mật khẩu", oldUser, user, "SUCCESS");

        return "redirect:/login?resetSuccess=true";
    }
}
