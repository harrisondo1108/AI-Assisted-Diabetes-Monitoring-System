package com.quan.diabetes.service;

import com.quan.diabetes.entity.LabTestCatalog;
import java.util.List;
import java.util.Optional;

public interface LabTestCatalogService {

    public List<LabTestCatalog> findAll();

    public Optional<LabTestCatalog> findById(String id);

    public LabTestCatalog create(LabTestCatalog entity);

    public LabTestCatalog update(String id, LabTestCatalog entity);

    public void deleteById(String id);

    public boolean existsById(String id);
}
