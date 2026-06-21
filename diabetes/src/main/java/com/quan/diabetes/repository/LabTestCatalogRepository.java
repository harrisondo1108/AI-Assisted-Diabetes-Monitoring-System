package com.quan.diabetes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.quan.diabetes.entity.LabTestCatalog;

public interface LabTestCatalogRepository extends JpaRepository<LabTestCatalog, String> {
}

