package com.quan.diabetes.service.lab;

import com.quan.diabetes.entity.LabTestCatalog;
import java.util.List;
import java.util.Optional;

public interface LabTestCatalogService {

    List<LabTestCatalog> findAll();

    Optional<LabTestCatalog> findById(String id);

    LabTestCatalog create(LabTestCatalog entity);

    LabTestCatalog update(String id, LabTestCatalog entity);

    void deleteById(String id);

    boolean existsById(String id);

    boolean existsByTestName(String testName);

    boolean existsByTestNameAndLabTestIdNot(String testName, String labTestId);

    String generateLabTestId();
}
