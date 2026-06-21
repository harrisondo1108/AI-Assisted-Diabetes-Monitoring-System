package com.quan.diabetes.repository;

import com.quan.diabetes.entity.MedicationTiming;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MedicationTimingRepository
        extends JpaRepository<MedicationTiming, Integer> {

    /**
     * Trả về danh sách các MedicationTiming liên quan tới một phiên khám (clinicalExamId).
     * Sử dụng DISTINCT để loại trừ trùng lặp khi một timing được sử dụng cho nhiều prescriptionDetail.
     */
    @Query("SELECT DISTINCT mt FROM PrescriptionTiming pt "
            + "JOIN pt.timing mt "
            + "JOIN pt.prescriptionDetail pd "
            + "JOIN pd.prescription p "
            + "WHERE p.clinicalExamination.clinicalExamId = :clinicalExamId")
    List<MedicationTiming> findMedicationTimingsByClinicalExamId(
            @Param("clinicalExamId") String clinicalExamId
    );
    Optional<MedicationTiming> findByTimingName(String timingName);

}
