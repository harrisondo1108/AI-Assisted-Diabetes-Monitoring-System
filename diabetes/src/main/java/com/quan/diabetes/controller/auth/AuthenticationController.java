package com.quan.diabetes.controller.auth;

import com.quan.diabetes.entity.*;
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
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@Controller
public class AuthenticationController {

    private final UserService userService;
    private final RoleService roleService;
    private final PatientService patientService;
    private final ProfileService profileService;
    private final PatientRoutineService patientRoutineService;

    public AuthenticationController(UserService userService,
                                    RoleService roleService,
                                    PatientService patientService,
                                    ProfileService profileService,
                                    PatientRoutineService patientRoutineService) {
        this.userService = userService;
        this.roleService = roleService;
        this.patientService = patientService;
        this.profileService = profileService;
        this.patientRoutineService = patientRoutineService;
    }

    // ==================== GET ====================

    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String resetSuccess,
                            @RequestParam(required = false) String registerSuccess,
                            Model model) {
        if ("true".equals(resetSuccess)) {
            model.addAttribute("successMsg", "Password reset successful! Please log in.");
        } else if ("true".equals(registerSuccess)) {
            model.addAttribute("successMsg", "Registration successful! Please log in.");
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
            model.addAttribute("errorMsg", "Incorrect account or password");
            return "auth/login";
        }
        User user = userOptional.get();
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
                return "redirect:/admin/dashboard";
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
                model.addAttribute("errorMsg", "Your account role is not recognized.");
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
            model.addAttribute("errorMsg", "Please enter all required information.");
            return "auth/register";
        }

        // Kiểm tra số điện thoại đã tồn tại chưa (tránh đăng ký trùng)
        if (userService.findByPhoneNumber(phoneNumber).isPresent()) {
            model.addAttribute("errorMsg", "The phone number already exists in the system.");
            return "auth/register";
        }

        // Kiểm tra role tồn tại
        Role role = roleService.findById(roleId).orElse(null);
        if (role == null) {
            model.addAttribute("errorMsg", "The role does not exist.");
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
            model.addAttribute("errorMsg", "Registration session expired. Please register again.");
            return "auth/register";
        }

        if (LocalDateTime.now().isAfter(expiredAt)) {
            model.addAttribute("errorMsg", "OTP code has expired. Please request a new one.");
            return "auth/register-otp";
        }

        if (!sessionOtp.equals(otp)) {
            model.addAttribute("errorMsg", "Incorrect OTP code");
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
            model.addAttribute("errorMsg", "Registration failed: " + e.getMessage());
            return "auth/register";
        }
    }

    // Resend OTP cho đăng ký
    @PostMapping("/register/resend-otp")
    @ResponseBody
    public String resendRegisterOtp(HttpSession session) {
        Map<String, Object> regData = (Map<String, Object>) session.getAttribute("regData");
        if (regData == null) {
            return "Registration session expired. Please register again.";
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
    private void createUserFromRegData(Map<String, Object> regData) {
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
            throw new RuntimeException("Phone number already exists. Please use another number.");
        }

        Role role = roleService.findById(roleId).orElseThrow(() -> new RuntimeException("Role not found"));
        String userId = userService.getNewID(roleId);

        User user = new User();
        user.setUserId(userId);
        user.setPhoneNumber(phoneNumber);
        user.setPasswordHash(password);
        user.setRole(role);
        user = userService.create(user);

        if ("PAT".equalsIgnoreCase(roleId)) {
            Patient patient = new Patient();
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
            patientRoutine.setPatient(patient);
            patientRoutineService.create(patientRoutine);
        } else {
            Profile profile = new Profile();
            profile.setUser(user);
            profile.setFullName(fullName);
            profile.setPhoneNumber(phoneNumber);
            profile.setSpecialty(specialty);
            profileService.create(profile);
        }
    }
}