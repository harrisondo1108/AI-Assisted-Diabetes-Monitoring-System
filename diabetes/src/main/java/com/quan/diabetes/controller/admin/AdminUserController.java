package com.quan.diabetes.controller.admin;

import com.quan.diabetes.dto.user.UserManagementDTO;
import com.quan.diabetes.service.user.AdminUserService;
import com.quan.diabetes.service.masterdata.RoomService;
import jakarta.validation.Valid;
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

    public AdminUserController(AdminUserService adminUserService, RoomService roomService) {
        this.adminUserService = adminUserService;
        this.roomService = roomService;
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
            if (userDto.getPassword() == null || userDto.getPassword().trim().length() < 6) {
                result.rejectValue("password", "error.password", "Mật khẩu cho tài khoản mới phải có ít nhất 6 ký tự");
            }
        } else {
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
            List<UserManagementDTO> users = adminUserService.getAllUserManagementDTOs("all", "");
            model.addAttribute("users", users);
            model.addAttribute("currentRole", "all");
            model.addAttribute("currentSearch", "");
            model.addAttribute("rooms", roomService.findAll());

            long totalPatients = users.stream()
                    .filter(u -> "PAT".equalsIgnoreCase(u.getRole()) || "patient".equalsIgnoreCase(u.getRole()))
                    .count();
            long totalDoctors = users.stream()
                    .filter(u -> "DOC".equalsIgnoreCase(u.getRole()) || "doctor".equalsIgnoreCase(u.getRole())).count();
            model.addAttribute("totalUsers", users.size());
            model.addAttribute("totalPatients", totalPatients);
            model.addAttribute("totalDoctors", totalDoctors);

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
                redirectAttributes.addFlashAttribute("successMessage", "Updated User Successfully");
            } catch (Exception e) {
                adminUserService.createUserManagementDTO(userDto);
                redirectAttributes.addFlashAttribute("successMessage", "Created User Successfully");
            }
        } else {
            adminUserService.createUserManagementDTO(userDto);
            // 3. Thêm thông báo thành công cho trường hợp Tạo mới
            redirectAttributes.addFlashAttribute("successMessage", "Created User Successfully");
        }

        return "redirect:/admin/users";
    }

    @PostMapping("/toggle-lock/{id}")
    public String toggleLock(@PathVariable("id") String id, RedirectAttributes redirectAttributes) {
        adminUserService.toggleLock(id);
        try {
            UserManagementDTO user = adminUserService.getUserManagementDTOById(id);
            String status = user.getStatus();
            String msg = "Account " + user.getFullName() + " has been " + ("Active".equalsIgnoreCase(status) ? "unlocked" : "locked") + " successfully";
            redirectAttributes.addFlashAttribute("successMessage", msg);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("successMessage", "Account status updated successfully");
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