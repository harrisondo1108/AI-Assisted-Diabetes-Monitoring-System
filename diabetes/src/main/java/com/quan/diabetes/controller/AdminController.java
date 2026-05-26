package com.quan.diabetes.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @GetMapping({"", "/"})
    public String adminHome() {
        return "redirect:/admin/users";
    }

    @GetMapping("/users")
    public String userManagement() {
        return "admin/user-management";
    }
}
