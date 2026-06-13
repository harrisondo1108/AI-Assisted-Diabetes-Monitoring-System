package com.quan.diabetes.service.impl;

import com.quan.diabetes.entity.LabTestCatalog;
import com.quan.diabetes.repository.LabTestCatalogRepository;
import com.quan.diabetes.service.LabTestCatalogService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class LabTestCatalogServiceImpl implements LabTestCatalogService {

    private final LabTestCatalogRepository labTestCatalogRepository;

    public LabTestCatalogServiceImpl(LabTestCatalogRepository labTestCatalogRepository) {
        this.labTestCatalogRepository = labTestCatalogRepository;
    }

    @Override
    public List<LabTestCatalog> findAll() {
        return labTestCatalogRepository.findAll();
    }

    @Override
    public Optional<LabTestCatalog> findById(String id) {
        return labTestCatalogRepository.findById(id);
    }

    @Override
    public LabTestCatalog create(LabTestCatalog entity) {
        return labTestCatalogRepository.save(entity);
    }

    @Override
    public LabTestCatalog update(String id, LabTestCatalog entity) {
        if (!labTestCatalogRepository.existsById(id)) {
            throw new EntityNotFoundException("LabTestCatalog not found with id: " + id);
        }

        entity.setLabTestId(id);
        return labTestCatalogRepository.save(entity);
    }

    @Override
    public void deleteById(String id) {
        if (!labTestCatalogRepository.existsById(id)) {
            throw new EntityNotFoundException("LabTestCatalog not found with id: " + id);
        }

        labTestCatalogRepository.deleteById(id);
    }

    @Override
    public boolean existsById(String id) {
        return labTestCatalogRepository.existsById(id);
    }

    @Override
    public boolean existsByTestName(String testName) {
        return labTestCatalogRepository.existsByTestName(testName);
    }

    @Override
    public boolean existsByTestNameAndLabTestIdNot(String testName, String labTestId) {
        return labTestCatalogRepository.existsByTestNameAndLabTestIdNot(testName, labTestId);
    }

    @Override
    public String generateLabTestId() {
        Random random = new Random();
        String id;

        do {
            int number = random.nextInt(9000) + 1000;
            id = "LT" + number;
        } while (labTestCatalogRepository.existsById(id));

        return id;
    }
}