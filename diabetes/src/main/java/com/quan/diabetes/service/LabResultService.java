package com.quan.diabetes.service;

import com.quan.diabetes.entity.LabResult;
import java.util.List;
import java.util.Optional;

public interface LabResultService {

    public List<LabResult> findAll();

    public Optional<LabResult> findById(String id);

    public LabResult create(LabResult entity);

    public LabResult update(String id, LabResult entity);

    public void deleteById(String id);

    public boolean existsById(String id);
}
