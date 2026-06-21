package com.quan.diabetes.repository;

import com.quan.diabetes.entity.LabTestCatalog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LabTestCatalogRepository extends JpaRepository<LabTestCatalog, String> {

    boolean existsByTestName(String testName);

    boolean existsByTestNameAndLabTestIdNot(String testName, String labTestId);
}
