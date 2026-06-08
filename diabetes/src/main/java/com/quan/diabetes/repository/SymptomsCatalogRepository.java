package com.quan.diabetes.repository;

import com.quan.diabetes.entity.SymptomsCatalog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SymptomsCatalogRepository extends JpaRepository<SymptomsCatalog, String> {

    Page<SymptomsCatalog> findByStatus(Boolean status, Pageable pageable);

    @Query("SELECT s FROM SymptomsCatalog s WHERE " +
            "(:keyword IS NULL OR LOWER(s.symptomId) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(s.symptomName) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "(:status IS NULL OR s.status = :status)")
    Page<SymptomsCatalog> searchByKeywordAndStatus(@Param("keyword") String keyword,
                                                   @Param("status") Boolean status,
                                                   Pageable pageable);

    boolean existsBySymptomNameIgnoreCase(String symptomName);
}