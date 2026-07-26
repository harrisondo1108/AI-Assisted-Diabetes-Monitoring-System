package com.quan.diabetes.repository;

import com.quan.diabetes.entity.Medication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface MedicationRepository extends JpaRepository<Medication, String> {

    boolean existsByMedicationNameIgnoreCase(String medicationName);

    // Search methods with pagination
    @Query("SELECT m FROM Medication m WHERE " +
            "LOWER(m.medicationName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(m.administrationRoute) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Medication> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT m FROM Medication m WHERE m.status = 'Active'")
    Page<Medication> findAllActive(Pageable pageable);

    @Query("SELECT m FROM Medication m WHERE m.status = 'Clocked'")
    Page<Medication> findAllClocked(Pageable pageable);

    Page<Medication> findByForm(String form, Pageable pageable);

    Page<Medication> findByAdministrationRoute(String route, Pageable pageable);

    Page<Medication> findByStatus(String status, Pageable pageable);

    @Query("SELECT m FROM Medication m WHERE " +
            "(:keyword = '' OR LOWER(m.medicationName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(m.administrationRoute) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(m.concentration) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "(:status = '' OR LOWER(m.status) = LOWER(:status)) AND " +
            "(:form = '' OR LOWER(m.form) = LOWER(:form) OR " +
            " (LOWER(:form) = 'viên nén' AND LOWER(m.form) = 'tablet') OR " +
            " (LOWER(:form) = 'viên nang' AND LOWER(m.form) = 'capsule') OR " +
            " (LOWER(:form) = 'thuốc tiêm' AND LOWER(m.form) = 'injection')) AND " +
            "(:route = '' OR LOWER(m.administrationRoute) = LOWER(:route) OR " +
            " (LOWER(:route) = 'đường uống' AND LOWER(m.administrationRoute) = 'oral') OR " +
            " (LOWER(:route) = 'tiêm dưới da' AND LOWER(m.administrationRoute) = 'subcutaneous') OR " +
            " (LOWER(:route) = 'tiêm tĩnh mạch' AND LOWER(m.administrationRoute) = 'intravenous') OR " +
            " (LOWER(:route) = 'tiêm bắp' AND LOWER(m.administrationRoute) = 'intramuscular'))")
    Page<Medication> filterMedications(@Param("keyword") String keyword,
                                       @Param("status") String status,
                                       @Param("form") String form,
                                       @Param("route") String route,
                                       Pageable pageable);

    // Non-paginated methods (for stats and other purposes)
    @Query("SELECT m FROM Medication m WHERE " +
            "LOWER(m.medicationName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(m.concentration) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(m.administrationRoute) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Medication> searchByKeywordList(@Param("keyword") String keyword);

    @Query("SELECT m FROM Medication m WHERE m.status = 'Active'")
    List<Medication> findAllActiveList();

    @Query("SELECT m FROM Medication m WHERE m.status = 'Clocked'")
    List<Medication> findAllClockedList();

    @Modifying
    @Transactional
    @Query("UPDATE Medication m SET m.status = :status WHERE m.medicationId = :id")
    void updateStatus(@Param("id") String id, @Param("status") String status);

    @Query("SELECT COUNT(m) FROM Medication m")
    long countTotalMedications();

    @Query("SELECT COUNT(m) FROM Medication m WHERE m.status = 'Active'")
    long countActiveMedications();

    @Query("SELECT COUNT(m) FROM Medication m WHERE m.status = 'Clocked'")
    long countClockedMedications();

    @Query("SELECT DISTINCT m.administrationRoute FROM Medication m WHERE m.administrationRoute IS NOT NULL AND m.administrationRoute != ''")
    List<String> findAllDistinctRoutes();
}