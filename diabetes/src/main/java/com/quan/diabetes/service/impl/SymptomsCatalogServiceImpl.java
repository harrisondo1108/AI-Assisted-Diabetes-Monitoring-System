package com.quan.diabetes.service.impl;

import com.quan.diabetes.entity.SymptomsCatalog;
import com.quan.diabetes.repository.SymptomsCatalogRepository;
import com.quan.diabetes.service.SymptomsCatalogService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SymptomsCatalogServiceImpl implements SymptomsCatalogService {

    private final SymptomsCatalogRepository symptomsCatalogRepository;

    public SymptomsCatalogServiceImpl(SymptomsCatalogRepository symptomsCatalogRepository) {
        this.symptomsCatalogRepository = symptomsCatalogRepository;
    }

    @Override
    public List<SymptomsCatalog> findAll() {
        return symptomsCatalogRepository.findAll();
    }

    @Override
    public Optional<SymptomsCatalog> findById(String id) {
        return symptomsCatalogRepository.findById(id);
    }

    @Override
    public SymptomsCatalog create(SymptomsCatalog entity) {
        return symptomsCatalogRepository.save(entity);
    }

    @Override
    public SymptomsCatalog update(String id, SymptomsCatalog entity) {
        if (!symptomsCatalogRepository.existsById(id)) {
            throw new EntityNotFoundException("SymptomsCatalog not found with id: " + id);
        }
        return symptomsCatalogRepository.save(entity);
    }

    @Override
    public void deleteById(String id) {
        if (!symptomsCatalogRepository.existsById(id)) {
            throw new EntityNotFoundException("SymptomsCatalog not found with id: " + id);
        }
        symptomsCatalogRepository.deleteById(id);
    }

    @Override
    public boolean existsById(String id) {
        return symptomsCatalogRepository.existsById(id);
    }
}
