package com.quan.diabetes;

import com.quan.diabetes.entity.Role;
import com.quan.diabetes.entity.User;
import com.quan.diabetes.repository.RoleRepository;
import com.quan.diabetes.repository.UserRepository;
import com.quan.diabetes.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component

public class AdminInitializer implements CommandLineRunner {
    @Autowired
    private UserRepository accountRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private UserService userService;

    private final String PHONE_NUMBER = "0328938692";
    @Override
    public void run(String... args) {

        // Kiểm tra đã có tài khoản admin chưa
        if (accountRepository.existsByPhoneNumber(PHONE_NUMBER)) {
            return;
        }

        Role adminRole = roleRepository.findById("AD")
                .orElseThrow(() -> new RuntimeException("Admin role not found"));

        User admin = new User();
        admin.setUserId("AD000001");
        admin.setPhoneNumber(PHONE_NUMBER);
        admin.setPasswordHash("123456");
        admin.setRole(adminRole);

        userService.create(admin);

        System.out.println("Default admin account created.");
    }
}