package com.quan.diabetes.controller.auth;

import com.quan.diabetes.config.SecurityConfig;
import com.quan.diabetes.entity.*;
import com.quan.diabetes.service.exam.ClinicalExaminationService;
import com.quan.diabetes.service.masterdata.RoleService;
import com.quan.diabetes.service.user.PatientRoutineService;
import com.quan.diabetes.service.user.PatientService;
import com.quan.diabetes.service.user.ProfileService;
import com.quan.diabetes.service.user.UserService;
import com.quan.diabetes.util.ParseUtil;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDateTime;
import java.util.*;

@Controller
public class AuthenticationController {

    private final UserService userService;
    private final RoleService roleService;
    private final PatientService patientService;
    private final ProfileService profileService;
    private final PatientRoutineService patientRoutineService;
    private final ClinicalExaminationService clinicalExaminationService;
    private final PasswordEncoder passwordEncoder;

    public AuthenticationController(UserService userService,
                                    RoleService roleService,
                                    PatientService patientService,
                                    ProfileService profileService,
                                    PatientRoutineService patientRoutineService,
                                    ClinicalExaminationService clinicalExaminationService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.roleService = roleService;
        this.patientService = patientService;
        this.profileService = profileService;
        this.patientRoutineService = patientRoutineService;
        this.clinicalExaminationService = clinicalExaminationService;
        this.passwordEncoder = passwordEncoder;
    }

    // ==================== GET ====================

    @GetMapping("/")
    public String home() {
        return "index";
    }


    @GetMapping("/login")
    // required = false; có nghĩa là không bắt buộc . Nếu trên URL
    public String loginPage(@RequestParam(required = false) String resetSuccess,
                            @RequestParam(required = false) String registerSuccess,
                            Model model) {
        if ("true".equals(resetSuccess)) {
            model.addAttribute("successMsg", "Đặt lại mật khẩu thành công! Vui lòng đăng nhập.");
        } else if ("true".equals(registerSuccess)) {
            model.addAttribute("successMsg", "Đăng ký tài khoản thành công! Vui lòng đăng nhập.");
        }
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        return "auth/register";
    }

    // Trang nhập OTP cho đăng ký
    @GetMapping("/register/otp")
    public String registerOtpPage(HttpSession session, Model model) {
        if (session.getAttribute("regData") == null) {
            return "redirect:/register";
        }
        return "auth/register-otp";
    }

    // ==================== POST ====================

    @PostMapping("/login")
    public String login(@RequestParam String phoneNumber,
                        @RequestParam String password,
                        HttpSession session,
                        Model model) {
        Optional<User> userOptional = userService.findByUsernameAndPassword(phoneNumber, password);

        if (userOptional.isEmpty()) {
            model.addAttribute("errorMsg", "Tên tài khoản hoặc mật khẩu không chính xác");
            return "auth/login";
        }
        User user = userOptional.get();
        // Sau khi xác thực thành công (user hợp lệ)
        List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + user.getRole().getRoleId())
        );
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(user.getPhoneNumber(), null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authToken);

        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext());

        session.setAttribute("loggedInUser", user);

        switch (user.getRole().getRoleId()) {
            case "PAT" -> {
                Patient patient = patientService.findById(user.getUserId()).orElse(null);
                session.setAttribute("userProfile", patient);
                return "redirect:/patient/dashboard";
            }
            case "DOC" -> {
                Profile profile = profileService.findById(user.getUserId()).orElse(null);
                session.setAttribute("userProfile", profile);
                return "redirect:/doctor/dashboard";
            }
            case "AD" -> {
                return "redirect:/admin/dashboard";
            }
            default -> {
                session.removeAttribute("loggedInUser");
                model.addAttribute("errorMsg", "Vai trò tài khoản của bạn không được nhận diện.");
                return "auth/login";
            }
        }
    }

    // Bước 1: Xử lý form đăng ký, lưu tạm và gửi OTP
    @PostMapping("/register")
    public String registerStep1(@RequestParam String roleId,
                                @RequestParam String fullName,
                                @RequestParam String phoneNumber,
                                @RequestParam String password,
                                @RequestParam(required = false) String dob,
                                @RequestParam(required = false) String gender,
                                @RequestParam(required = false) String bloodGroup,
                                @RequestParam(required = false) String height,
                                @RequestParam(required = false) String weight,
                                @RequestParam(required = false) String address,
                                @RequestParam(required = false) String medicalHistory,
                                @RequestParam(required = false) String allergyNotes,
                                @RequestParam(required = false) String specialty,
                                @RequestParam(required = false) String licenseNumber,
                                Model model,
                                HttpSession session) {

        // Validate required fields
        if (ParseUtil.isBlank(roleId) || ParseUtil.isBlank(fullName) || ParseUtil.isBlank(phoneNumber) || ParseUtil.isBlank(password)) {
            model.addAttribute("errorMsg", "Vui lòng nhập đầy đủ các thông tin bắt buộc.");
            return "auth/register";
        }

        // Kiểm tra số điện thoại đã tồn tại chưa (tránh đăng ký trùng)
        if (userService.findByPhoneNumber(phoneNumber).isPresent()) {
            model.addAttribute("errorMsg", "Số điện thoại đã tồn tại trên hệ thống.");
            return "auth/register";
        }

        // Kiểm tra role tồn tại
        Role role = roleService.findById(roleId).orElse(null);
        if (role == null) {
            model.addAttribute("errorMsg", "Vai trò không tồn tại.");
            return "auth/register";
        }

        // Lưu toàn bộ thông tin đăng ký vào session (dùng Map)
        Map<String, Object> regData = new HashMap<>();
        regData.put("roleId", roleId);
        regData.put("fullName", fullName);
        regData.put("phoneNumber", phoneNumber);
        regData.put("password", password);
        regData.put("dob", dob);
        regData.put("gender", gender);
        regData.put("bloodGroup", bloodGroup);
        regData.put("height", height);
        regData.put("weight", weight);
        regData.put("address", address);
        regData.put("medicalHistory", medicalHistory);
        regData.put("allergyNotes", allergyNotes);
        regData.put("specialty", specialty);
        regData.put("licenseNumber", licenseNumber);
        session.setAttribute("regData", regData);

        // Tạo OTP
        String otp = String.valueOf(100000 + new Random().nextInt(900000));
        session.setAttribute("regOtp", otp);
        session.setAttribute("regOtpExpiredAt", LocalDateTime.now().plusMinutes(5));
        session.setAttribute("regOtpVerified", false);

        // Mô phỏng gửi OTP (in ra console)
        System.out.println("OTP đăng ký cho số " + phoneNumber + ": " + otp);

        return "redirect:/register/otp";
    }

    // Bước 2: Xác thực OTP
    @PostMapping("/register/verify-otp")
    public String verifyRegisterOtp(@RequestParam String otp,
                                    HttpSession session,
                                    Model model) {
        Map<String, Object> regData = (Map<String, Object>) session.getAttribute("regData");
        String sessionOtp = (String) session.getAttribute("regOtp");
        LocalDateTime expiredAt = (LocalDateTime) session.getAttribute("regOtpExpiredAt");

        if (regData == null || sessionOtp == null || expiredAt == null) {
            model.addAttribute("errorMsg", "Phiên đăng ký đã hết hạn. Vui lòng đăng ký lại.");
            return "auth/register";
        }

        if (LocalDateTime.now().isAfter(expiredAt)) {
            model.addAttribute("errorMsg", "Mã OTP đã hết hạn. Vui lòng yêu cầu mã mới.");
            return "auth/register-otp";
        }

        if (!sessionOtp.equals(otp)) {
            model.addAttribute("errorMsg", "Mã OTP không chính xác");
            return "auth/register-otp";
        }

        // OTP hợp lệ -> tiến hành tạo user thật
        try {
            createUserFromRegData(regData);
            // Xóa các attribute session liên quan đến đăng ký
            session.removeAttribute("regData");
            session.removeAttribute("regOtp");
            session.removeAttribute("regOtpExpiredAt");
            session.removeAttribute("regOtpVerified");
            return "redirect:/login?registerSuccess=true";
        } catch (Exception e) {
            model.addAttribute("errorMsg", "Đăng ký thất bại: " + e.getMessage());
            return "auth/register";
        }
    }

    // Resend OTP cho đăng ký
    @PostMapping("/register/resend-otp")
    @ResponseBody
    public String resendRegisterOtp(HttpSession session) {
        Map<String, Object> regData = (Map<String, Object>) session.getAttribute("regData");
        if (regData == null) {
            return "Phiên đăng ký đã hết hạn. Vui lòng đăng ký lại.";
        }
        String phoneNumber = (String) regData.get("phoneNumber");
        String newOtp = String.valueOf(100000 + new Random().nextInt(900000));
        session.setAttribute("regOtp", newOtp);
        session.setAttribute("regOtpExpiredAt", LocalDateTime.now().plusMinutes(5));
        session.setAttribute("regOtpVerified", false);
        System.out.println("Resend OTP đăng ký cho số " + phoneNumber + ": " + newOtp);
        return "OK";
    }

    // Phương thức tạo user từ dữ liệu tạm (đã được kiểm tra phone chưa tồn tại trước đó,
    // nhưng cần kiểm tra lại phòng trường hợp có người đăng ký cùng số trong lúc chờ OTP)
    @Transactional
    public void createUserFromRegData(Map<String, Object> regData) {
        String roleId = (String) regData.get("roleId");
        String fullName = (String) regData.get("fullName");
        String phoneNumber = (String) regData.get("phoneNumber");
        String password = (String) regData.get("password");
        String dob = (String) regData.get("dob");
        String gender = (String) regData.get("gender");
        String bloodGroup = (String) regData.get("bloodGroup");
        String height = (String) regData.get("height");
        String weight = (String) regData.get("weight");
        String address = (String) regData.get("address");
        String medicalHistory = (String) regData.get("medicalHistory");
        String allergyNotes = (String) regData.get("allergyNotes");
        String specialty = (String) regData.get("specialty");

        // Kiểm tra lại phone number chưa tồn tại (tránh race condition)
        if (userService.findByPhoneNumber(phoneNumber).isPresent()) {
            throw new RuntimeException("Số điện thoại đã tồn tại. Vui lòng chọn số khác.");
        }

        Role role = roleService.findById(roleId).orElseThrow(() -> new RuntimeException("Không tìm thấy vai trò"));
        String userId = userService.getNewID(roleId);

        User user = new User();
        user.setUserId(userId);
        user.setPhoneNumber(phoneNumber);
        user.setPasswordHash(password);
        user.setRole(role);
        user = userService.create(user);

        if ("PAT".equalsIgnoreCase(roleId)) {
            Patient patient = new Patient();
            patient.setUserId(userId);
            patient.setUser(user);
            patient.setFullName(fullName);
            patient.setPhoneNumber(phoneNumber);
            patient.setDob(ParseUtil.parseDate(dob));
            patient.setGender(ParseUtil.parseGender(gender));
            patient.setBloodgroup(ParseUtil.parseString(bloodGroup));
            patient.setHeight(ParseUtil.parseInteger(height));
            patient.setWeight(ParseUtil.parseBigDecimal(weight));
            patient.setAddress(ParseUtil.parseString(address));
            patient.setPermanentMedicalHistory(ParseUtil.parseString(medicalHistory));
            patient.setAllergyNotes(ParseUtil.parseString(allergyNotes));
            patientService.create(patient);
            // Tạo patientRoutine
            PatientRoutine patientRoutine = new PatientRoutine();
            patientRoutine.setUserId(userId);
            patientRoutineService.create(patientRoutine);
            clinicalExaminationService.createAutoPendingExamination(patient.getUserId());
        } else {
            Profile profile = new Profile();
            profile.setUserId(userId);
            profile.setUser(user);
            profile.setFullName(fullName);
            profile.setPhoneNumber(phoneNumber);
            profile.setSpecialty(specialty);
            profileService.create(profile);
        }
    }

}