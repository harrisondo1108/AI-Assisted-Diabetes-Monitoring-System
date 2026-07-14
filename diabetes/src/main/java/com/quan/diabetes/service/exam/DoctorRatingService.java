package com.quan.diabetes.service.exam;

import com.quan.diabetes.dto.doctor.DoctorRatingView;
import com.quan.diabetes.entity.DoctorRating;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface DoctorRatingService {
    void saveRating(DoctorRating rating);
    Optional<DoctorRating> getRatingByExamId(String examId);
    List<DoctorRating> getRatingsByDoctor(String doctorId);
    Page<DoctorRating> getRatingsByDoctor(String doctorId, Pageable pageable);
    Page<DoctorRating> getRatingsByDoctorAndStar(String doctorId, int ratingValue, Pageable pageable);
    Double getAverageRatingForDoctor(String doctorId);
    Map<String, DoctorRating> getRatingsForExams(List<String> examIds);
    List<DoctorRatingView> getTopRatedDoctors(int limit);
}
