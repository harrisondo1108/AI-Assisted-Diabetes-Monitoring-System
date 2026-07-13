package com.quan.diabetes.service.exam.impl;

import com.quan.diabetes.dto.doctor.DoctorRatingView;
import com.quan.diabetes.entity.DoctorRating;
import com.quan.diabetes.entity.Profile;
import com.quan.diabetes.repository.DoctorRatingRepository;
import com.quan.diabetes.repository.ProfileRepository;
import com.quan.diabetes.service.exam.DoctorRatingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class DoctorRatingServiceImpl implements DoctorRatingService {

    private final DoctorRatingRepository doctorRatingRepository;
    private final ProfileRepository profileRepository;

    @Autowired
    public DoctorRatingServiceImpl(DoctorRatingRepository doctorRatingRepository, ProfileRepository profileRepository) {
        this.doctorRatingRepository = doctorRatingRepository;
        this.profileRepository = profileRepository;
    }

    @Override
    public void saveRating(DoctorRating rating) {
        doctorRatingRepository.save(rating);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DoctorRating> getRatingByExamId(String examId) {
        return doctorRatingRepository.findByClinicalExamination_ClinicalExamId(examId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DoctorRating> getRatingsByDoctor(String doctorId) {
        return doctorRatingRepository.findByDoctor_UserId(doctorId);
    }

    @Override
    @Transactional(readOnly = true)
    public Double getAverageRatingForDoctor(String doctorId) {
        Double avg = doctorRatingRepository.findAverageRatingByDoctorId(doctorId);
        return avg != null ? avg : 0.0;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, DoctorRating> getRatingsForExams(List<String> examIds) {
        Map<String, DoctorRating> ratingsMap = new HashMap<>();
        if (examIds != null && !examIds.isEmpty()) {
            for (String examId : examIds) {
                doctorRatingRepository.findByClinicalExamination_ClinicalExamId(examId)
                        .ifPresent(rating -> ratingsMap.put(examId, rating));
            }
        }
        return ratingsMap;
    }

    @Override
    @Transactional(readOnly = true)
    public List<DoctorRatingView> getTopRatedDoctors(int limit) {
        List<Profile> doctorProfiles = profileRepository.findAll().stream()
                .filter(p -> p.getUser() != null && p.getUser().getRole() != null && "DOC".equalsIgnoreCase(p.getUser().getRole().getRoleId()))
                .collect(Collectors.toList());

        return doctorProfiles.stream()
                .map(profile -> {
                    String doctorId = profile.getUserId();
                    Double avg = doctorRatingRepository.findAverageRatingByDoctorId(doctorId);
                    double averageRating = avg != null ? avg : 0.0;
                    long count = doctorRatingRepository.countByDoctor_UserId(doctorId);
                    String roomName = (profile.getRoom() != null) ? profile.getRoom().getRoomName() : "Phòng nội tiết";

                    return new DoctorRatingView(
                            doctorId,
                            profile.getFullName(),
                            profile.getSpecialty(),
                            roomName,
                            profile.getImageUrl(),
                            averageRating,
                            count
                    );
                })
                .sorted((d1, d2) -> {
                    int cmp = d2.getAverageRating().compareTo(d1.getAverageRating());
                    if (cmp != 0) return cmp;
                    return d2.getRatingCount().compareTo(d1.getRatingCount());
                })
                .limit(limit)
                .collect(Collectors.toList());
    }
}
