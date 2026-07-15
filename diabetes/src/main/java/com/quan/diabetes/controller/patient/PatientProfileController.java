package com.quan.diabetes.controller.patient;

import com.quan.diabetes.entity.*;

import com.quan.diabetes.service.user.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.quan.diabetes.service.cloudinary.CloudinaryService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import java.util.Map;
import java.util.HashMap;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Controller
public class PatientProfileController extends BasePatientController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CloudinaryService cloudinaryService;

    @GetMapping("/patient/profile")
    public String profile(Model model, HttpSession session,
                          @RequestParam(value = "successMessage", required = false) String successMessage,
                          @RequestParam(value = "errorMessage", required = false) String errorMessage) {
        User currentUser = getCurrentUser(session);
        Patient patient = getCurrentPatient(session);

        PatientRoutine routine = null;

        if (patient != null && patient.getUserId() != null) {
            routine = patientRoutineService.findById(patient.getUserId()).orElse(null);
        }

        if (successMessage != null) {
            model.addAttribute("successMessage", successMessage);
        }
        if (errorMessage != null) {
            model.addAttribute("errorMessage", errorMessage);
        }

        model.addAttribute("activeMenu", "profile");
        model.addAttribute("patient", patient);
        model.addAttribute("routine", routine);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("patientCode", patient != null ? patient.getUserId() : "");
        model.addAttribute("pageRole", "Patient Portal");
        model.addAttribute("isCreateMode", patient == null);
        model.addAttribute("userPhone", currentUser != null ? currentUser.getPhoneNumber() : "");

        return "patient/profile";
    }

    @PostMapping("/patient/profile/save")
    public String saveProfile(@RequestParam("fullName") String fullName,
                              @RequestParam("phoneNumber") String phoneNumber,
                              @RequestParam(value = "address", required = false) String address,
                              @RequestParam(value = "dob", required = false)
                              @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dob,
                              @RequestParam(value = "gender", required = false) Boolean gender,
                              @RequestParam(value = "height", required = false) Integer height,
                              @RequestParam(value = "weight", required = false) BigDecimal weight,
                              @RequestParam(value = "bloodgroup", required = false) String bloodgroup,
                              @RequestParam(value = "permanentMedicalHistory", required = false) String permanentMedicalHistory,
                              @RequestParam(value = "allergyNotes", required = false) String allergyNotes,
                              @RequestParam(value = "supervisorName", required = false) String supervisorName,
                              @RequestParam(value = "supervisorPhone", required = false) String supervisorPhone,
                              @RequestParam(value = "email", required = false) String email,
                              @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {

        User currentUser = getCurrentUser(session);

        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn phải đăng nhập trước.");
            return "redirect:/login";
        }

        if (fullName == null || fullName.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Yêu cầu nhập họ và tên.");
            return "redirect:/patient/profile";
        }

        if (fullName.trim().length() < 2 || fullName.trim().length() > 60) {
            redirectAttributes.addFlashAttribute("errorMessage", "Họ và tên phải từ 2 đến 60 ký tự.");
            return "redirect:/patient/profile";
        }

        if (!fullName.trim().matches("^[A-Za-zÀ-ỹ\\s]+$")) {
            redirectAttributes.addFlashAttribute("errorMessage", "Họ và tên chỉ được chứa chữ cái và khoảng trắng.");
            return "redirect:/patient/profile";
        }

        if (address != null && address.trim().length() > 200) {
            redirectAttributes.addFlashAttribute("errorMessage", "Địa chỉ không được vượt quá 200 ký tự.");
            return "redirect:/patient/profile";
        }

        if (bloodgroup != null && !bloodgroup.trim().isEmpty()) {
            if (!java.util.List.of("O+", "O-", "A+", "A-", "B+", "B-", "AB+", "AB-").contains(bloodgroup.trim().toUpperCase())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Nhóm máu không hợp lệ.");
                return "redirect:/patient/profile";
            }
        }

        if (supervisorName != null && !supervisorName.trim().isEmpty()) {
            if (supervisorName.trim().length() > 90) {
                redirectAttributes.addFlashAttribute("errorMessage", "Tên người giám hộ không được vượt quá 90 ký tự.");
                return "redirect:/patient/profile";
            }
            if (!supervisorName.trim().matches("^[A-Za-zÀ-ỹ\\s]+$")) {
                redirectAttributes.addFlashAttribute("errorMessage", "Tên người giám hộ chỉ được chứa chữ cái và khoảng trắng.");
                return "redirect:/patient/profile";
            }
        }

        if (supervisorPhone != null && !supervisorPhone.trim().isEmpty()) {
            if (!supervisorPhone.trim().matches("^[0-9]{10,15}$")) {
                redirectAttributes.addFlashAttribute("errorMessage", "Số điện thoại người giám hộ phải chứa từ 10 đến 15 chữ số.");
                return "redirect:/patient/profile";
            }
        }

        if (email != null && !email.trim().isEmpty()) {
            if (email.trim().length() > 100) {
                redirectAttributes.addFlashAttribute("errorMessage", "Email không được vượt quá 100 ký tự.");
                return "redirect:/patient/profile";
            }
            if (!email.trim().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                redirectAttributes.addFlashAttribute("errorMessage", "Địa chỉ email không hợp lệ.");
                return "redirect:/patient/profile";
            }
        }

        if (height != null && (height < 50 || height > 250)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Chiều cao phải từ 50 đến 250 cm.");
            return "redirect:/patient/profile";
        }

        if (weight != null && (weight.compareTo(java.math.BigDecimal.ONE) < 0 || weight.compareTo(new java.math.BigDecimal("300")) > 0)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Cân nặng phải từ 1 đến 300 kg.");
            return "redirect:/patient/profile";
        }

        String normalizedPhone = currentUser.getPhoneNumber();
        String userId = currentUser.getUserId();

        Patient patient = patientService.findById(userId).orElse(new Patient());

        patient.setUserId(userId);
        patient.setUser(currentUser);
        patient.setFullName(fullName.trim());
        patient.setPhoneNumber(normalizedPhone);
        patient.setAddress(clean(address));
        patient.setDob(dob);
        patient.setGender(gender);
        patient.setHeight(height);
        patient.setWeight(weight);
        patient.setBloodgroup(clean(bloodgroup));
        patient.setPermanentMedicalHistory(clean(permanentMedicalHistory));
        patient.setAllergyNotes(clean(allergyNotes));
        patient.setSupervisorName(clean(supervisorName));
        patient.setSupervisorPhone(clean(supervisorPhone));
        patient.setEmail(clean(email));

        if (imageFile != null && !imageFile.isEmpty()) {
            if (imageFile.getSize() > 2 * 1024 * 1024) {
                redirectAttributes.addFlashAttribute("errorMessage", "Ảnh đại diện không được vượt quá 2MB.");
                return "redirect:/patient/profile";
            }
            String contentType = imageFile.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                redirectAttributes.addFlashAttribute("errorMessage", "Định dạng tệp không hợp lệ. Chỉ chấp nhận các tệp ảnh.");
                return "redirect:/patient/profile";
            }
            try {
                String imageUrl = cloudinaryService.uploadImage(imageFile);
                if (imageUrl != null) {
                    patient.setImageUrl(imageUrl);
                }
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("errorMessage", "Không thể tải lên ảnh đại diện: " + e.getMessage());
                return "redirect:/patient/profile";
            }
        }
        if (patientService.existsById(userId)) {
            patientService.update(userId, patient);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật hồ sơ thành công.");
        } else {
            patientService.create(patient);
            redirectAttributes.addFlashAttribute("successMessage", "Tạo hồ sơ thành công.");
        }

        currentUser.setPhoneNumber(normalizedPhone);
        userService.update(currentUser.getUserId(), currentUser);

        session.setAttribute("loggedInUser", currentUser);
        session.setAttribute("userProfile", patient);

        return "redirect:/patient/profile";
    }

    @PostMapping("/patient/profile/routine/save")
    public String saveRoutine(@RequestParam(value = "breakfastTime", required = false)
                              @DateTimeFormat(pattern = "HH:mm") LocalTime breakfastTime,
                              @RequestParam(value = "lunchTime", required = false)
                              @DateTimeFormat(pattern = "HH:mm") LocalTime lunchTime,
                              @RequestParam(value = "dinnerTime", required = false)
                              @DateTimeFormat(pattern = "HH:mm") LocalTime dinnerTime,
                              @RequestParam(value = "wakeUpTime", required = false)
                              @DateTimeFormat(pattern = "HH:mm") LocalTime wakeUpTime,
                              @RequestParam(value = "sleepTime", required = false)
                              @DateTimeFormat(pattern = "HH:mm") LocalTime sleepTime,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {

        User currentUser = getCurrentUser(session);

        if (currentUser == null || currentUser.getUserId() == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn phải đăng nhập trước.");
            return "redirect:/login";
        }

        String userId = currentUser.getUserId();

        if (!patientService.existsById(userId)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng tạo hồ sơ của bạn trước.");
            return "redirect:/patient/profile";
        }

        PatientRoutine routine = patientRoutineService
                .findById(userId)
                .orElse(new PatientRoutine());

        routine.setUserId(userId);
        routine.setWakeUpTime(wakeUpTime != null ? wakeUpTime : LocalTime.of(6, 0));
        routine.setBreakfastTime(breakfastTime != null ? breakfastTime : LocalTime.of(7, 0));
        routine.setLunchTime(lunchTime != null ? lunchTime : LocalTime.of(12, 0));
        routine.setDinnerTime(dinnerTime != null ? dinnerTime : LocalTime.of(18, 0));
        routine.setSleepTime(sleepTime != null ? sleepTime : LocalTime.of(22, 0));

        if (patientRoutineService.existsById(userId)) {
            patientRoutineService.update(userId, routine);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật lịch sinh hoạt thành công.");
        } else {
            patientRoutineService.create(routine);
            redirectAttributes.addFlashAttribute("successMessage", "Tạo lịch sinh hoạt thành công.");
        }

        return "redirect:/patient/profile";
    }

    @PostMapping("/patient/profile/routine/delete")
    public String deleteRoutine(HttpSession session,
                                RedirectAttributes redirectAttributes) {
        Patient patient = getCurrentPatient(session);

        if (patient == null || patient.getUserId() == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy hồ sơ bệnh nhân.");
            return "redirect:/patient/profile";
        }

        if (patientRoutineService.existsById(patient.getUserId())) {
            patientRoutineService.deleteById(patient.getUserId());
            redirectAttributes.addFlashAttribute("successMessage", "Xóa lịch sinh hoạt thành công.");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Lịch sinh hoạt không tồn tại.");
        }

        return "redirect:/patient/profile";
    }

    @PostMapping("/patient/profile/change-password")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> changePassword(
            @RequestParam("currentPassword") String currentPassword,
            @RequestParam("newPassword") String newPassword,
            @RequestParam("confirmPassword") String confirmPassword,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();
        User currentUser = getCurrentUser(session);
        
        if (currentUser == null) {
            response.put("success", false);
            response.put("message", "Bạn phải đăng nhập trước.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        if (!"PAT".equalsIgnoreCase(currentUser.getRole().getRoleId())) {
            response.put("success", false);
            response.put("message", "Từ chối truy cập. Chỉ bệnh nhân mới được thay đổi mật khẩu ở đây.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }

        if (currentPassword == null || currentPassword.trim().isEmpty()
                || newPassword == null || newPassword.trim().isEmpty()
                || confirmPassword == null || confirmPassword.trim().isEmpty()) {
            response.put("success", false);
            response.put("message", "Tất cả các trường mật khẩu đều bắt buộc.");
            return ResponseEntity.badRequest().body(response);
        }

        User user = userService.findById(currentUser.getUserId()).orElse(null);
        if (user == null) {
            response.put("success", false);
            response.put("message", "Không tìm thấy tài khoản người dùng.");
            return ResponseEntity.badRequest().body(response);
        }

        if (!passwordEncoder.matches(currentPassword.trim(), user.getPasswordHash())) {
            response.put("success", false);
            response.put("message", "Mật khẩu hiện tại không đúng.");
            return ResponseEntity.badRequest().body(response);
        }

        if (!com.quan.diabetes.util.ParseUtil.isValidPassword(newPassword.trim())) {
            response.put("success", false);
            response.put("message", "Mật khẩu mới phải chứa ít nhất 8 ký tự, bao gồm chữ hoa, chữ thường, ít nhất một chữ số và ký tự đặc biệt (!@#$).");
            return ResponseEntity.badRequest().body(response);
        }

        if (!newPassword.trim().equals(confirmPassword.trim())) {
            response.put("success", false);
            response.put("message", "Mật khẩu mới và xác nhận mật khẩu không khớp.");
            return ResponseEntity.badRequest().body(response);
        }

        if (passwordEncoder.matches(newPassword.trim(), user.getPasswordHash())) {
            response.put("success", false);
            response.put("message", "Mật khẩu mới không được trùng với mật khẩu hiện tại.");
            return ResponseEntity.badRequest().body(response);
        }

        user.setPasswordHash(newPassword.trim());
        User updatedUser = userService.update(user.getUserId(), user);
        session.setAttribute("loggedInUser", updatedUser);

        response.put("success", true);
        response.put("message", "Thay đổi mật khẩu thành công.");
        return ResponseEntity.ok(response);
    }
}
