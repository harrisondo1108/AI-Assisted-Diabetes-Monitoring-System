package com.quan.diabetes.controller;

import com.quan.diabetes.dto.UserManagementDTO;
import com.quan.diabetes.service.AdminUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/api/users")
public class AdminUserRestController {

    private final AdminUserService userService;

    public AdminUserRestController(AdminUserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<UserManagementDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUserManagementDTOs());
    }

    @PostMapping
    public ResponseEntity<UserManagementDTO> createUser(@RequestBody UserManagementDTO dto) {
        UserManagementDTO created = userService.createUserManagementDTO(dto);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserManagementDTO> updateUser(@PathVariable String id, @RequestBody UserManagementDTO dto) {
        UserManagementDTO updated = userService.updateUserManagementDTO(id, dto);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}/toggle-lock")
    public ResponseEntity<Void> toggleLock(@PathVariable String id) {
        userService.toggleLock(id);
        return ResponseEntity.ok().build();
    }
}
