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
            @RequestParam(name = "doctorName", required = false) String doctorName,
            Model model) {

        // 1. Get all doctors for optional reference
        List<Profile> allDoctors = profileService.findAll().stream()
                .filter(p -> p.getUser() != null && p.getUser().getRole() != null && "DOC".equalsIgnoreCase(p.getUser().getRole().getRoleId()))
                .collect(Collectors.toList());
        model.addAttribute("doctors", allDoctors);

        // 2. Fetch all ratings to filter by doctorName or doctorId
        List<DoctorRating> rawRatings = doctorRatingService.getAllRatings();
        List<DoctorRating> doctorFilteredRatings;

        if (doctorName != null && !doctorName.trim().isEmpty()) {
            String query = doctorName.trim().toLowerCase();
            doctorFilteredRatings = rawRatings.stream()
                    .filter(r -> r.getDoctor() != null && r.getDoctor().getProfile() != null 
                            && r.getDoctor().getProfile().getFullName() != null
                            && r.getDoctor().getProfile().getFullName().toLowerCase().contains(query))
                    .collect(Collectors.toList());
        } else if (doctorId != null && !doctorId.trim().isEmpty()) {
            doctorFilteredRatings = rawRatings.stream()
                    .filter(r -> r.getDoctor() != null && doctorId.equalsIgnoreCase(r.getDoctor().getUserId()))
                    .collect(Collectors.toList());
        } else {
            doctorFilteredRatings = rawRatings;
        }

        // Calculate statistics based on doctor filter
        int ratingCount = doctorFilteredRatings.size();
        Double averageRating = ratingCount > 0 
                ? doctorFilteredRatings.stream().mapToInt(DoctorRating::getRatingValue).average().orElse(0.0) 
                : 0.0;

        long star5 = doctorFilteredRatings.stream().filter(r -> r.getRatingValue() == 5).count();
        long star4 = doctorFilteredRatings.stream().filter(r -> r.getRatingValue() == 4).count();
        long star3 = doctorFilteredRatings.stream().filter(r -> r.getRatingValue() == 3).count();
        long star2 = doctorFilteredRatings.stream().filter(r -> r.getRatingValue() == 2).count();
        long star1 = doctorFilteredRatings.stream().filter(r -> r.getRatingValue() == 1).count();

        int star5Pct = ratingCount > 0 ? (int) Math.round((double) star5 / ratingCount * 100) : 0;
        int star4Pct = ratingCount > 0 ? (int) Math.round((double) star4 / ratingCount * 100) : 0;
        int star3Pct = ratingCount > 0 ? (int) Math.round((double) star3 / ratingCount * 100) : 0;
        int star2Pct = ratingCount > 0 ? (int) Math.round((double) star2 / ratingCount * 100) : 0;
        int star1Pct = ratingCount > 0 ? (int) Math.round((double) star1 / ratingCount * 100) : 0;

        // Apply star filter
        List<DoctorRating> finalFilteredRatings = doctorFilteredRatings;
        if (star != null && star >= 1 && star <= 5) {
            finalFilteredRatings = doctorFilteredRatings.stream()
                    .filter(r -> r.getRatingValue() == star)
                    .collect(Collectors.toList());
        }

        // Sort by newest first
        finalFilteredRatings.sort((a, b) -> {
            if (a.getCreatedAt() == null || b.getCreatedAt() == null) return 0;
            return b.getCreatedAt().compareTo(a.getCreatedAt());
        });

        // In-memory pagination
        int totalItems = finalFilteredRatings.size();
        int totalPages = totalItems > 0 ? (int) Math.ceil((double) totalItems / size) : 0;
        int fromIndex = Math.min(page * size, totalItems);
        int toIndex = Math.min(fromIndex + size, totalItems);
        List<DoctorRating> ratings = finalFilteredRatings.subList(fromIndex, toIndex);

        model.addAttribute("ratings", ratings);
        model.addAttribute("averageRating", averageRating);
        model.addAttribute("ratingCount", ratingCount);
        model.addAttribute("star5Pct", star5Pct);
        model.addAttribute("star4Pct", star4Pct);
        model.addAttribute("star3Pct", star3Pct);
        model.addAttribute("star2Pct", star2Pct);
        model.addAttribute("star1Pct", star1Pct);

        // Pagination & Filter attributes
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalItems", totalItems);
        model.addAttribute("pageSize", size);
        model.addAttribute("selectedStar", star);
        model.addAttribute("selectedDoctorId", doctorId);
        model.addAttribute("selectedDoctorName", doctorName);

        return "Admin/ratings";
    }
}
