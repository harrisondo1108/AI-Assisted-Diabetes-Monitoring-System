package com.quan.diabetes.repository;

import com.quan.diabetes.entity.DoctorRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorRatingRepository extends JpaRepository<DoctorRating, Integer> {
    
    Optional<DoctorRating> findByClinicalExamination_ClinicalExamId(String clinicalExamId);
    
    List<DoctorRating> findByDoctor_UserId(String doctorId);
    
    Page<DoctorRating> findByDoctor_UserId(String doctorId, Pageable pageable);

    Page<DoctorRating> findByDoctor_UserIdAndRatingValue(String doctorId, int ratingValue, Pageable pageable);

    Long countByDoctor_UserId(String doctorId);
    
    @Query("SELECT AVG(cast(r.ratingValue as double)) FROM DoctorRating r WHERE r.doctor.userId = :doctorId")
    Double findAverageRatingByDoctorId(@Param("doctorId") String doctorId);
}
