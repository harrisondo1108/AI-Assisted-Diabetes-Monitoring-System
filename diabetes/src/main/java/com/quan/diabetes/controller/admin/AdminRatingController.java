package com.quan.diabetes.controller.admin;

import com.quan.diabetes.entity.DoctorRating;
import com.quan.diabetes.entity.Profile;
import com.quan.diabetes.service.exam.DoctorRatingService;
import com.quan.diabetes.service.user.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/ratings")
public class AdminRatingController {

    private final DoctorRatingService doctorRatingService;
    private final ProfileService profileService;

    @Autowired
    public AdminRatingController(DoctorRatingService doctorRatingService, ProfileService profileService) {
        this.doctorRatingService = doctorRatingService;
        this.profileService = profileService;
    }

    @GetMapping
    public String ratingsPage(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "5") int size,
            @RequestParam(name = "star", required = false) Integer star,
            @RequestParam(name = "doctorId", required = false) String doctorId,
            Model model) {

        // 1. Get all doctors for the filter dropdown
        List<Profile> allDoctors = profileService.findAll().stream()
                .filter(p -> p.getUser() != null && p.getUser().getRole() != null && "DOC".equalsIgnoreCase(p.getUser().getRole().getRoleId()))
                .collect(Collectors.toList());
        model.addAttribute("doctors", allDoctors);

        // 2. Fetch statistics based on filter (either single doctor or global)
        int ratingCount;
        Double averageRating;
        long star5, star4, star3, star2, star1;
        List<DoctorRating> allRatings;

        if (doctorId != null && !doctorId.trim().isEmpty()) {
            allRatings = doctorRatingService.getRatingsByDoctor(doctorId);
            averageRating = doctorRatingService.getAverageRatingForDoctor(doctorId);
        } else {
            allRatings = doctorRatingService.getAllRatings();
            averageRating = doctorRatingService.getAverageRatingForAll();
        }

        ratingCount = allRatings.size();
        star5 = allRatings.stream().filter(r -> r.getRatingValue() == 5).count();
        star4 = allRatings.stream().filter(r -> r.getRatingValue() == 4).count();
        star3 = allRatings.stream().filter(r -> r.getRatingValue() == 3).count();
        star2 = allRatings.stream().filter(r -> r.getRatingValue() == 2).count();
        star1 = allRatings.stream().filter(r -> r.getRatingValue() == 1).count();

        int star5Pct = ratingCount > 0 ? (int) Math.round((double) star5 / ratingCount * 100) : 0;
        int star4Pct = ratingCount > 0 ? (int) Math.round((double) star4 / ratingCount * 100) : 0;
        int star3Pct = ratingCount > 0 ? (int) Math.round((double) star3 / ratingCount * 100) : 0;
        int star2Pct = ratingCount > 0 ? (int) Math.round((double) star2 / ratingCount * 100) : 0;
        int star1Pct = ratingCount > 0 ? (int) Math.round((double) star1 / ratingCount * 100) : 0;

        // Paged ratings for detail feed, sorted by newest first
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<DoctorRating> ratingsPage;

        if (doctorId != null && !doctorId.trim().isEmpty()) {
            if (star != null && star >= 1 && star <= 5) {
                ratingsPage = doctorRatingService.getRatingsByDoctorAndStar(doctorId, star, pageable);
            } else {
                ratingsPage = doctorRatingService.getRatingsByDoctor(doctorId, pageable);
            }
        } else {
            if (star != null && star >= 1 && star <= 5) {
                ratingsPage = doctorRatingService.getRatingsByStar(star, pageable);
            } else {
                ratingsPage = doctorRatingService.getAllRatings(pageable);
            }
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
        model.addAttribute("selectedDoctorId", doctorId);

        return "Admin/ratings";
    }
}
