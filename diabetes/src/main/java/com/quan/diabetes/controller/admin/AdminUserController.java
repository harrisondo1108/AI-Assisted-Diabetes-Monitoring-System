package com.quan.diabetes.controller.admin;

import com.quan.diabetes.dto.user.UserManagementDTO;
import com.quan.diabetes.service.user.AdminUserService;
import com.quan.diabetes.service.masterdata.RoomService;
import jakarta.validation.Valid;
import com.quan.diabetes.service.systemlog.SystemLogService;
import com.quan.diabetes.util.ParseUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/users")
public class AdminUserController {

    private final AdminUserService adminUserService;
    private final RoomService roomService;
    private final SystemLogService systemLogService;

    public AdminUserController(AdminUserService adminUserService, RoomService roomService, SystemLogService systemLogService) {
        this.adminUserService = adminUserService;
        this.roomService = roomService;
        this.systemLogService = systemLogService;
    }

    @GetMapping
    public String listUsers(
            @RequestParam(name = "role", defaultValue = "all") String role,
            @RequestParam(name = "search", defaultValue = "") String search,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "7") int size,
            Model model) {
        org.springframework.data.domain.Page<UserManagementDTO> userPage = adminUserService
                .getPagedUserManagementDTOs(role, search, page, size);
        List<UserManagementDTO> users = adminUserService.getAllUserManagementDTOs(role, search);
        model.addAttribute("users", userPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", userPage.getTotalPages());
        model.addAttribute("totalItems", userPage.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("currentRole", role);
        model.addAttribute("currentSearch", search);
        model.addAttribute("rooms", roomService.findAll());
        model.addAttribute("user", new UserManagementDTO());

        // Thống kê tổng số lượng: luôn tính trên toàn bộ dữ liệu, không bị ảnh hưởng
        // bởi bộ lọc hiện tại
        List<UserManagementDTO> allUsers = adminUserService.getAllUserManagementDTOs("all", "");
        long totalPatients = allUsers.stream()
                .filter(u -> "PAT".equalsIgnoreCase(u.getRole()) || "patient".equalsIgnoreCase(u.getRole())).count();
        long totalDoctors = allUsers.stream()
                .filter(u -> "DOC".equalsIgnoreCase(u.getRole()) || "doctor".equalsIgnoreCase(u.getRole())).count();
        model.addAttribute("totalUsers", allUsers.size());
        model.addAttribute("totalPatients", totalPatients);
        model.addAttribute("totalDoctors", totalDoctors);

        return "admin/user-management";
    }

    @PostMapping("/save")
    public String saveUser(@ModelAttribute("user") @Valid UserManagementDTO userDto,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) { // 1. Thêm tham số này

        boolean isCreateMode = userDto.getUserId() == null || userDto.getUserId().trim().isEmpty();
        if (isCreateMode) {
            if (userDto.getRole() == null || (!"DOC".equalsIgnoreCase(userDto.getRole()) && !"doctor".equalsIgnoreCase(userDto.getRole()))) {
                result.rejectValue("role", "error.role", "Admin chỉ được phép tạo tài khoản Bác sĩ (Doctor)");
            }
            if (userDto.getPassword() == null || userDto.getPassword().trim().isEmpty()) {
                result.rejectValue("password", "error.password", "Mật khẩu không được để trống");
            } else if (!ParseUtil.isValidPassword(userDto.getPassword())) {
                result.rejectValue("password", "error.password", "Mật khẩu phải từ 8 ký tự trở lên, bao gồm chữ hoa, chữ thường, số và ký tự đặc biệt (!@#$)");
            }
        } else {
            if (userDto.getPassword() != null && !userDto.getPassword().trim().isEmpty() && !ParseUtil.isValidPassword(userDto.getPassword())) {
                result.rejectValue("password", "error.password", "Mật khẩu phải từ 8 ký tự trở lên, bao gồm chữ hoa, chữ thường, số và ký tự đặc biệt (!@#$)");
            }
            try {
                UserManagementDTO existingUser = adminUserService.getUserManagementDTOById(userDto.getUserId());
                if ("PAT".equalsIgnoreCase(existingUser.getRole()) || "patient".equalsIgnoreCase(existingUser.getRole())) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Admin không được phép chỉnh sửa tài khoản Bệnh nhân (Patient)");
                    return "redirect:/admin/users";
                }
            } catch (Exception e) {
                // Ignore entity not found, saveUser will fail gracefully below
            }
        }

        // Check if phone number is already taken
        if (adminUserService.isPhoneTaken(userDto.getAccountPhone(), userDto.getUserId())) {
            result.rejectValue("accountPhone", "error.accountPhone",
                    "Số điện thoại này đã được sử dụng bởi tài khoản khác");
        }

        if (result.hasErrors()) {
            if (isCreateMode) {
                systemLogService.saveLogWithObject(null, "CREATE", "Account", null, "Thêm tài khoản mới thất bại (Lỗi xác thực)", null, userDto, "FAILED");
            } else {
                systemLogService.saveLogWithObject(null, "UPDATE", "Account", userDto.getUserId(), "Cập nhật tài khoản thất bại (Lỗi xác thực)", null, userDto, "FAILED");
            }

            org.springframework.data.domain.Page<UserManagementDTO> userPage = adminUserService
                    .getPagedUserManagementDTOs("all", "", 0, 7);
            model.addAttribute("users", userPage.getContent());
            model.addAttribute("currentPage", 0);
            model.addAttribute("totalPages", userPage.getTotalPages());
            model.addAttribute("totalItems", userPage.getTotalElements());
            model.addAttribute("pageSize", 7);
            model.addAttribute("currentRole", "all");
            model.addAttribute("currentSearch", "");
            model.addAttribute("rooms", roomService.findAll());

            List<UserManagementDTO> allUsers = adminUserService.getAllUserManagementDTOs("all", "");
            long totalPatients = allUsers.stream()
                    .filter(u -> "PAT".equalsIgnoreCase(u.getRole()) || "patient".equalsIgnoreCase(u.getRole()))
                    .count();
            long totalDoctors = allUsers.stream()
                    .filter(u -> "DOC".equalsIgnoreCase(u.getRole()) || "doctor".equalsIgnoreCase(u.getRole())).count();
            model.addAttribute("totalUsers", allUsers.size());
            model.addAttribute("totalPatients", totalPatients);
            model.addAttribute("totalDoctors", totalDoctors);

            // Trả về userDto đã submit để Thymeleaf render lỗi chi tiết trên form
            model.addAttribute("user", userDto);
            model.addAttribute("serverValidationError", true);
            model.addAttribute("isCreateMode", isCreateMode);

            return "admin/user-management";
        }

        // Xử lý lưu dữ liệu
        if (!isCreateMode) {
            try {
                adminUserService.getUserManagementDTOById(userDto.getUserId());
                adminUserService.updateUserManagementDTO(userDto.getUserId(), userDto);
                // 2. Thêm thông báo thành công cho trường hợp Cập nhật
                redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thông tin người dùng thành công");
            } catch (Exception e) {
                adminUserService.createUserManagementDTO(userDto);
                redirectAttributes.addFlashAttribute("successMessage", "Thêm mới người dùng thành công");
            }
        } else {
            adminUserService.createUserManagementDTO(userDto);
            // 3. Thêm thông báo thành công cho trường hợp Tạo mới
            redirectAttributes.addFlashAttribute("successMessage", "Thêm mới người dùng thành công");
        }

        return "redirect:/admin/users";
    }

    @PostMapping("/toggle-lock/{id}")
    public String toggleLock(@PathVariable("id") String id, RedirectAttributes redirectAttributes) {
        adminUserService.toggleLock(id);
        try {
            UserManagementDTO user = adminUserService.getUserManagementDTOById(id);
            String status = user.getStatus();
            String msg = "Tài khoản " + user.getFullName() + " đã được " + ("Active".equalsIgnoreCase(status) ? "mở khóa" : "khóa") + " thành công";
            redirectAttributes.addFlashAttribute("successMessage", msg);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật trạng thái tài khoản thành công");
        }
        return "redirect:/admin/users";
    }

    @GetMapping("/check-phone")
    @ResponseBody
    public java.util.Map<String, Boolean> checkPhone(
            @RequestParam("phone") String phone,
            @RequestParam(name = "userId", required = false) String userId) {
        boolean isTaken = adminUserService.isPhoneTaken(phone, userId);
        java.util.Map<String, Boolean> response = new java.util.HashMap<>();
        response.put("available", !isTaken);
        return response;
    }

    @GetMapping("/list")
    @ResponseBody
    public java.util.Map<String, Object> listUsersJson(
            @RequestParam(name = "role", defaultValue = "all") String role,
            @RequestParam(name = "search", defaultValue = "") String search,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "7") int size) {
        org.springframework.data.domain.Page<UserManagementDTO> userPage = adminUserService
                .getPagedUserManagementDTOs(role, search, page, size);

        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("content", userPage.getContent());
        response.put("currentPage", userPage.getNumber());
        response.put("totalPages", userPage.getTotalPages());
        response.put("pageSize", userPage.getSize());
        return response;
    }
}