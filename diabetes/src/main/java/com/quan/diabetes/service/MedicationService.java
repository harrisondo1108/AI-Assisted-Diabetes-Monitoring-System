package com.quan.diabetes.service;

import com.quan.diabetes.entity.Medication;
import java.util.List;
import java.util.Optional;

public interface MedicationService {

    public List<Medication> findAll();

    public Optional<Medication> findById(String id);

    public Medication create(Medication entity);

    public Medication update(String id, Medication entity);

    public void deleteById(String id);

    public boolean existsById(String id);
}
