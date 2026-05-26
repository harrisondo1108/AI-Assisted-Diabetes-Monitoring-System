package com.quan.diabetes.service.impl;

import com.quan.diabetes.entity.Medication;
import com.quan.diabetes.repository.MedicationRepository;
import com.quan.diabetes.service.MedicationService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class MedicationServiceImpl implements MedicationService {

    @Autowired
    private MedicationRepository medicationRepository;

    @Override
    public List<Medication> findAll() {
        return medicationRepository.findAll();
    }

    @Override
    public Optional<Medication> findById(String id) {
        return medicationRepository.findById(id);
    }
    private String generateMedicationId() {
        List<Medication> allMedications = medicationRepository.findAll();

        int maxNumber = 0;
        for (Medication med : allMedications) {
            String id = med.getMedicationId();
            if (id != null && id.startsWith("MED-")) {
                try {
                    String numberPart = id.substring(4);
                    int number = Integer.parseInt(numberPart);
                    if (number > maxNumber) {
                        maxNumber = number;
                    }
                } catch (NumberFormatException e) {
                    // Bỏ qua nếu không phải số
                }
            }
        }

        int nextNumber = maxNumber + 1;
        return String.format("MED-%02d", nextNumber);
    }
    @Override
    public Medication create(Medication entity) {
        // TẠO ID DẠNG MED-01, MED-02...
        String newId = generateMedicationId();
        entity.setMedicationId(newId);
        System.out.println("Generated ID in Service: " + newId);

        return medicationRepository.save(entity);
    }

    @Override
    public Medication update(String id, Medication entity) {
        if (!medicationRepository.existsById(id)) {
            throw new EntityNotFoundException("Medication not found with id: " + id);
        }
        entity.setMedicationId(id);
        return medicationRepository.save(entity);
    }

    @Override
    public void deleteById(String id) {
        if (!medicationRepository.existsById(id)) {
            throw new EntityNotFoundException("Medication not found with id: " + id);
        }
        medicationRepository.deleteById(id);
    }

    @Override
    public boolean existsById(String id) {
        return medicationRepository.existsById(id);
    }

    @Override
    public List<Medication> searchByKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return findAll();
        }
        return medicationRepository.searchByKeyword(keyword);
    }

    @Override
    public List<String> findAllDistinctRoutes() {
        return medicationRepository.findAllDistinctRoutes();
    }
}