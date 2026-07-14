package com.quan.diabetes.controller.doctor;

import com.quan.diabetes.entity.DoctorRating;
import com.quan.diabetes.entity.User;
import com.quan.diabetes.service.exam.DoctorRatingService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/doctor")
public class DoctorRatingController {

    private final DoctorRatingService doctorRatingService;

    @Autowired
    public DoctorRatingController(DoctorRatingService doctorRatingService) {
        this.doctorRatingService = doctorRatingService;
    }

    @GetMapping("/ratings")
    public String ratingsPage(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "5") int size,
            @RequestParam(name = "star", required = false) Integer star,
            HttpSession session, Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !"DOC".equalsIgnoreCase(loggedInUser.getRole().getRoleId())) {
            return "redirect:/login";
        }

        String doctorId = loggedInUser.getUserId();
        List<DoctorRating> allRatings = doctorRatingService.getRatingsByDoctor(doctorId);
        Double averageRating = doctorRatingService.getAverageRatingForDoctor(doctorId);

        int ratingCount = allRatings.size();
        
        // Calculate stars percentages
        long star5 = allRatings.stream().filter(r -> r.getRatingValue() == 5).count();
        long star4 = allRatings.stream().filter(r -> r.getRatingValue() == 4).count();
        long star3 = allRatings.stream().filter(r -> r.getRatingValue() == 3).count();
        long star2 = allRatings.stream().filter(r -> r.getRatingValue() == 2).count();
        long star1 = allRatings.stream().filter(r -> r.getRatingValue() == 1).count();

        int star5Pct = ratingCount > 0 ? (int) Math.round((double) star5 / ratingCount * 100) : 0;
        int star4Pct = ratingCount > 0 ? (int) Math.round((double) star4 / ratingCount * 100) : 0;
        int star3Pct = ratingCount > 0 ? (int) Math.round((double) star3 / ratingCount * 100) : 0;
        int star2Pct = ratingCount > 0 ? (int) Math.round((double) star2 / ratingCount * 100) : 0;
        int star1Pct = ratingCount > 0 ? (int) Math.round((double) star1 / ratingCount * 100) : 0;

        // Paged ratings for detail feed, sorted by newest first
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<DoctorRating> ratingsPage;
        if (star != null && star >= 1 && star <= 5) {
            ratingsPage = doctorRatingService.getRatingsByDoctorAndStar(doctorId, star, pageable);
        } else {
            ratingsPage = doctorRatingService.getRatingsByDoctor(doctorId, pageable);
        }
        List<DoctorRating> ratings = ratingsPage.getContent();

        model.addAttribute("ratings", ratings);
        model.addAttribute("averageRating", averageRating);
        model.addAttribute("ratingCount", ratingCount);
        model.addAttribute("star5Pct", star5Pct);
        model.addAttribute("star4Pct", star4Pct);
        model.addAttribute("star3Pct", star3Pct);
        model.addAttribute("star2Pct", star2Pct);
        model.addAttribute("star1Pct", star1Pct);

        // Pagination attributes
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", ratingsPage.getTotalPages());
        model.addAttribute("totalItems", ratingsPage.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("selectedStar", star);

        return "doctor/ratings";
    }
}
