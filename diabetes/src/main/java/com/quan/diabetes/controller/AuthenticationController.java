package com.quan.diabetes.controller;

import com.quan.diabetes.entity.User;
import com.quan.diabetes.repository.UserRepository;
import com.quan.diabetes.service.UserService;
import com.quan.diabetes.service.impl.UserServiceImpl;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
public class AuthenticationController {

    private final UserService userService;

    public AuthenticationController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String login(){
        return "auth/login";
    }
    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password){
        Optional<User> userOptional = userService.findByUsernameAndPassword(username, password);
        if(userOptional.isPresent()){
            User use = userOptional.get();
            return "auth/register";
        }else {
            System.out.println("User not found");
            return "dashboard/index";
        }
    }
}
