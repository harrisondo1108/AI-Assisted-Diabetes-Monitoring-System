package com.quan.diabetes.controller;

import com.quan.diabetes.dto.UserManagementDTO;
import com.quan.diabetes.service.AdminUserService;
import com.quan.diabetes.service.RoomService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
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
            Model model) {
        List<UserManagementDTO> users = adminUserService.getAllUserManagementDTOs(role, search);
        model.addAttribute("users", users);
        model.addAttribute("currentRole", role);
        model.addAttribute("currentSearch", search);
        model.addAttribute("rooms", roomService.findAll());
        model.addAttribute("user", new UserManagementDTO());

        // Thống kê tổng số lượng: luôn tính trên toàn bộ dữ liệu, không bị ảnh hưởng bởi bộ lọc hiện tại
        List<UserManagementDTO> allUsers = adminUserService.getAllUserManagementDTOs("all", "");
        long totalPatients = allUsers.stream().filter(u -> "PAT".equalsIgnoreCase(u.getRole()) || "patient".equalsIgnoreCase(u.getRole())).count();
        long totalDoctors  = allUsers.stream().filter(u -> "DOC".equalsIgnoreCase(u.getRole()) || "doctor".equalsIgnoreCase(u.getRole())).count();
        model.addAttribute("totalUsers",    allUsers.size());
        model.addAttribute("totalPatients", totalPatients);
        model.addAttribute("totalDoctors",  totalDoctors);

        return "admin/user-management";
    }

    @PostMapping("/save")
    public String saveUser(@ModelAttribute("user") @Valid UserManagementDTO userDto,
                           BindingResult result,
                           Model model) {
        boolean isCreateMode = userDto.getUserId() == null || userDto.getUserId().trim().isEmpty();
        if (isCreateMode) {
            if (userDto.getPassword() == null || userDto.getPassword().trim().length() < 6) {
                result.rejectValue("password", "error.password", "Mật khẩu cho tài khoản mới phải có ít nhất 6 ký tự");
            }
        }

        if (result.hasErrors()) {
            List<UserManagementDTO> users = adminUserService.getAllUserManagementDTOs("all", "");
            model.addAttribute("users", users);
            model.addAttribute("currentRole", "all");
            model.addAttribute("currentSearch", "");
            model.addAttribute("rooms", roomService.findAll());

            long totalPatients = users.stream().filter(u -> "PAT".equalsIgnoreCase(u.getRole()) || "patient".equalsIgnoreCase(u.getRole())).count();
            long totalDoctors  = users.stream().filter(u -> "DOC".equalsIgnoreCase(u.getRole()) || "doctor".equalsIgnoreCase(u.getRole())).count();
            model.addAttribute("totalUsers",    users.size());
            model.addAttribute("totalPatients", totalPatients);
            model.addAttribute("totalDoctors",  totalDoctors);

            model.addAttribute("serverValidationError", true);
            model.addAttribute("isCreateMode", isCreateMode);

            return "admin/user-management";
        }

        System.out.println(userDto);
        if (!isCreateMode) {
            try {
                // Quick check if user exists
                adminUserService.getUserManagementDTOById(userDto.getUserId());
                adminUserService.updateUserManagementDTO(userDto.getUserId(), userDto);
            } catch (Exception e) {
                adminUserService.createUserManagementDTO(userDto);
            }
        } else {
            adminUserService.createUserManagementDTO(userDto);
        }

        return "redirect:/admin/users";
    }

    @PostMapping("/toggle-lock/{id}")
    public String toggleLock(@PathVariable String id) {
        adminUserService.toggleLock(id);
        return "redirect:/admin/users";
    }


}