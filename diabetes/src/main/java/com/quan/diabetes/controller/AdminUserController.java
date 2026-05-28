package com.quan.diabetes.controller;

import com.quan.diabetes.dto.UserManagementDTO;
import com.quan.diabetes.service.AdminUserService;
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

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
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
        return "admin/user-management";
    }

    @PostMapping("/save")
    public String saveUser(@ModelAttribute("user") @Valid UserManagementDTO userDto,
                           BindingResult result) {
        System.out.println("================================================");
        System.out.println(userDto);
        System.out.println("================================================");
        if (result.hasErrors()) {
            // Validation errors can be handled here; for now we redirect back with error feedback (to be enhanced if needed)
            return "redirect:/admin/users?error=validation";
        }
        
        if (userDto.getUserId() != null && !userDto.getUserId().trim().isEmpty() && !userDto.getUserId().startsWith("USR-")) {
            // If it has a known ID (not the auto-generated client-side one which might start with USR- or be empty)
            // Wait, our frontend JS sets `userId` to `USR-<timestamp>` if empty.
            // Let's check if the user exists to decide on update vs create.
            try {
                // Quick check if user exists
                adminUserService.getUserManagementDTOById(userDto.getUserId());
                adminUserService.updateUserManagementDTO(userDto.getUserId(), userDto);
            } catch (Exception e) {
                // Not found, so create
                adminUserService.createUserManagementDTO(userDto);
            }
        } else {
            adminUserService.createUserManagementDTO(userDto);
            System.out.println("================================================");
            System.out.println(userDto);
            System.out.println("================================================");
        }
        
        return "redirect:/admin/users";
    }

    @PostMapping("/toggle-lock/{id}")
    public String toggleLock(@PathVariable String id) {
        adminUserService.toggleLock(id);
        return "redirect:/admin/users";
    }


}
