package com.quan.diabetes.service;

import com.quan.diabetes.entity.Prescription;
import java.util.List;
import java.util.Optional;

public interface PrescriptionService {

    public List<Prescription> findAll();

    public Optional<Prescription> findById(String id);

    public Prescription create(Prescription entity);

    public Prescription update(String id, Prescription entity);

    public void deleteById(String id);

    public boolean existsById(String id);
}
