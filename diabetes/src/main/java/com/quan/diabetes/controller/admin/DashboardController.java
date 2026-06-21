package com.quan.diabetes.controller.admin;

import com.quan.diabetes.service.user.PatientService;
import com.quan.diabetes.service.user.ProfileService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class DashboardController {
    private final PatientService patientService;
    private final ProfileService doctorService;

    public DashboardController(PatientService patientService, ProfileService doctorService) {
        this.patientService = patientService;
        this.doctorService = doctorService;
    }
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("totalPatient",  patientService.findAll().size());
        model.addAttribute("totalDoctor",  doctorService.findTotalDoctor().size());

        return "admin/dashboard";
    }
}
