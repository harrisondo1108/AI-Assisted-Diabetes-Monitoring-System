package com.quan.diabetes.controller.doctor;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/doctor")
public class DoctorController {

    @GetMapping("/dashboard")
    public String dashboard() {
        return "doctor/dashboard";
    }

    @GetMapping("/examine")
    public String examine() {
        return "doctor/examine";
    }

    @GetMapping("/examine/patients")
    public String patients() {
        return "doctor/patients";
    }
}
