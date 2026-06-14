package com.quan.diabetes.controller.auth;

import com.quan.diabetes.entity.*;
import com.quan.diabetes.service.*;
import com.quan.diabetes.util.ParseUtil;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Controller
public class AuthenticationController {

    private final UserService userService;
    private final RoleService roleService;
    private final PatientService patientService;
    private final ProfileService profileService;
    private final PatientRoutineService patientRoutineService;
    private final ClinicalExaminationService clinicalExaminationService;

    public AuthenticationController(UserService userService,
                                    RoleService roleService,
                                    PatientService patientService,
                                    ProfileService profileService,
                                    PatientRoutineService patientRoutineService,
                                    ClinicalExaminationService clinicalExaminationService) {
        this.userService = userService;
        this.roleService = roleService;
        this.patientService = patientService;
        this.profileService = profileService;
        this.patientRoutineService = patientRoutineService;
        this.clinicalExaminationService = clinicalExaminationService;
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

    @Transactional
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
                           Model model,
                           HttpSession session) {

        if (ParseUtil.isBlank(roleId) || ParseUtil.isBlank(fullName) || ParseUtil.isBlank(phoneNumber) || ParseUtil.isBlank(password)) {
            model.addAttribute("errorMsg", "Please enter all required information.");
            return "auth/register";
        }

        if (userService.findByPhoneNumber(phoneNumber).isPresent()) {
            model.addAttribute("errorMsg", "The phone number already exists in the system.");
            return "auth/register";
        }

        Role role = roleService.findById(roleId).orElse(null);
        if (role == null) {
            model.addAttribute("errorMsg", "The role does not exist.");
            return "auth/register";
        }
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
            // khai patientRoutine
            PatientRoutine patientRoutine = new PatientRoutine();
            patientRoutine.setPatient(patient);
            patientRoutineService.create(patientRoutine);
            clinicalExaminationService.createAutoPendingExamination(patient.getUserId());

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


}
