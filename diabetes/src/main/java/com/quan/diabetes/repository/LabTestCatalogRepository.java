package com.quan.diabetes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.quan.diabetes.entity.LabTestCatalog;
import org.springframework.stereotype.Repository;

@Repository
public interface LabTestCatalogRepository extends JpaRepository<LabTestCatalog, String> {

    boolean existsByTestName(String testName);

    boolean existsByTestNameAndLabTestIdNot(String testName, String labTestId);
}

