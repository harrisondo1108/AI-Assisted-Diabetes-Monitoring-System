package com.quan.diabetes.service;

import com.quan.diabetes.entity.SymptomsCatalog;
import java.util.List;
import java.util.Optional;

public interface SymptomsCatalogService {

    public List<SymptomsCatalog> findAll();

    public Optional<SymptomsCatalog> findById(String id);

    public SymptomsCatalog create(SymptomsCatalog entity);

    public SymptomsCatalog update(String id, SymptomsCatalog entity);

    public void deleteById(String id);

    public boolean existsById(String id);
}
