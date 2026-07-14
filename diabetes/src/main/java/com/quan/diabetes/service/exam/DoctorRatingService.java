package com.quan.diabetes.service.exam;

import com.quan.diabetes.dto.doctor.DoctorRatingView;
import com.quan.diabetes.entity.DoctorRating;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface DoctorRatingService {
    void saveRating(DoctorRating rating);
    Optional<DoctorRating> getRatingByExamId(String examId);
    List<DoctorRating> getRatingsByDoctor(String doctorId);
    Double getAverageRatingForDoctor(String doctorId);
    Map<String, DoctorRating> getRatingsForExams(List<String> examIds);
    List<DoctorRatingView> getTopRatedDoctors(int limit);
}
