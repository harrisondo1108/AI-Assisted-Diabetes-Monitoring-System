package com.quan.diabetes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.quan.diabetes.entity.Medication;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MedicationRepository extends JpaRepository<Medication, String> {
    // Tìm kiếm theo từ khóa
    @Query("SELECT m FROM Medication m WHERE " +
            "LOWER(m.medicationName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(m.concentration) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(m.administrationRoute) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Medication> searchByKeyword(@Param("keyword") String keyword);

    // Lấy tất cả các route duy nhất
    @Query("SELECT DISTINCT m.administrationRoute FROM Medication m WHERE m.administrationRoute IS NOT NULL AND m.administrationRoute != ''")
    List<String> findAllDistinctRoutes();
}

