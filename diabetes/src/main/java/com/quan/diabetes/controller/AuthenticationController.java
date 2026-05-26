package com.quan.diabetes.controller;

import com.quan.diabetes.entity.Patient;
import com.quan.diabetes.entity.Profile;
import com.quan.diabetes.entity.Role;
import com.quan.diabetes.entity.User;
import com.quan.diabetes.service.PatientService;
import com.quan.diabetes.service.ProfileService;
import com.quan.diabetes.service.RoleService;
import com.quan.diabetes.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@Controller
public class AuthenticationController {

    private final UserService userService;
    private final RoleService roleService;
    private final PatientService patientService;
    private final ProfileService profileService;

    public AuthenticationController(UserService userService,
                                    RoleService roleService,
                                    PatientService patientService,
                                    ProfileService profileService) {
        this.userService = userService;
        this.roleService = roleService;
        this.patientService = patientService;
        this.profileService = profileService;
    }

    // ==================== GET ====================

    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String resetSuccess,
                            @RequestParam(required = false) String registerSuccess,
                            Model model) {
        if ("true".equals(resetSuccess)) {
            model.addAttribute("successMsg", "Đặt lại mật khẩu thành công! Vui lòng đăng nhập.");
        } else if ("true".equals(registerSuccess)) {
            model.addAttribute("successMsg", "Đăng ký thành công! Vui lòng đăng nhập.");
        }
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        return "auth/register";
    }

    // ==================== POST ====================

    @PostMapping("/login")
    public String login(@RequestParam String phoneNumber,
                        @RequestParam String password,
                        HttpSession session,
                        Model model) {
        Optional<User> userOptional = userService.findByUsernameAndPassword(phoneNumber, password);

        if (userOptional.isEmpty()) {
            model.addAttribute("errorMsg", "Tài khoản hoặc mật khẩu không chính xác");
            return "auth/login";
        }

        session.setAttribute("loggedInUser", userOptional.get());
        return "redirect:/dashboard";
    }

    @PostMapping("/register")
    public String register(@RequestParam String roleId,
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
                           Model model) {

        if (isBlank(roleId) || isBlank(fullName) || isBlank(phoneNumber) || isBlank(password)) {
            model.addAttribute("errorMsg", "Vui lòng nhập đầy đủ thông tin bắt buộc");
            return "auth/register";
        }

        if (userService.findByPhoneNumber(phoneNumber).isPresent()) {
            model.addAttribute("errorMsg", "Số điện thoại đã tồn tại trong hệ thống");
            return "auth/register";
        }

        Role role = roleService.findById(roleId).orElse(null);
        if (role == null) {
            model.addAttribute("errorMsg", "Vai trò không tồn tại");
            return "auth/register";
        }
        String userId = getNewID(roleId);

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
            patient.setDob(parseDate(dob));
            patient.setGender(parseGender(gender));
            patient.setBloodgroup(parseString(bloodGroup));
            patient.setHeight(parseInteger(height));
            patient.setWeight(parseBigDecimal(weight));
            patient.setAddress(parseString(address));
            patient.setPermanentMedicalHistory(parseString(medicalHistory));
            patient.setAllergyNotes(parseString(allergyNotes));
            patientService.create(patient);
        } else {
            Profile profile = new Profile();
            profile.setUser(user);
            profile.setFullName(fullName);
            profile.setPhoneNumber(phoneNumber);
            profile.setSpecialty(specialty);
            profileService.create(profile);
        }

        return "redirect:/login?registerSuccess=true";
    }

    // ==================== Helpers ====================

    private String getNewID(String roleId) {
        String userId = null;
        switch (roleId){
            case "PAT":{
                    do{
                        String number = "00000" + new Random().nextInt(1000000);
                        userId = "P" + number.substring(number.length() - 6);
                    }while(userService.existsById(userId));
            }
        }
        return userId;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private LocalDate parseDate(String value) {
        return isBlank(value) ? null : LocalDate.parse(value);
    }

    private Boolean parseGender(String value) {
        return isBlank(value) ? null : "1".equals(value);
    }

    private String parseString(String value){
        return isBlank(value) ? null : value.trim();
    }

    private Integer parseInteger(String value) {
        return isBlank(value) ? null : Integer.parseInt(value);
    }

    private BigDecimal parseBigDecimal(String value) {
        return isBlank(value) ? null : new BigDecimal(value);
    }
}
