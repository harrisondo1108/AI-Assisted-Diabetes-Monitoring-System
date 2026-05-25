package com.quan.diabetes.service;

import com.quan.diabetes.entity.LabOrder;
import java.util.List;
import java.util.Optional;

public interface LabOrderService {

    public List<LabOrder> findAll();

    public Optional<LabOrder> findById(String id);

    public LabOrder create(LabOrder entity);

    public LabOrder update(String id, LabOrder entity);

    public void deleteById(String id);

    public boolean existsById(String id);
}
