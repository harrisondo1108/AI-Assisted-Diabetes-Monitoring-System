package com.quan.diabetes.service;

import com.quan.diabetes.entity.PrescriptionDetail;
import java.util.List;
import java.util.Optional;

public interface PrescriptionDetailService {

    public List<PrescriptionDetail> findAll();

    public Optional<PrescriptionDetail> findById(String id);

    public PrescriptionDetail create(PrescriptionDetail entity);

    public PrescriptionDetail update(String id, PrescriptionDetail entity);

    public void deleteById(String id);

    public boolean existsById(String id);
}
