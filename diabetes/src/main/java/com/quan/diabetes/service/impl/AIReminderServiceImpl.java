package com.quan.diabetes.service.impl;

import com.quan.diabetes.entity.AIReminder;
import com.quan.diabetes.repository.AIReminderRepository;
import com.quan.diabetes.service.AIReminderService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AIReminderServiceImpl implements AIReminderService {

    private final AIReminderRepository aIReminderRepository;

    public AIReminderServiceImpl(AIReminderRepository aIReminderRepository) {
        this.aIReminderRepository = aIReminderRepository;
    }

    @Override
    public List<AIReminder> findAll() {
        return aIReminderRepository.findAll();
    }

    @Override
    public Optional<AIReminder> findById(Long id) {
        return aIReminderRepository.findById(id);
    }

    @Override
    public AIReminder create(AIReminder entity) {
        return aIReminderRepository.save(entity);
    }

    @Override
    public AIReminder update(Long id, AIReminder entity) {
        if (!aIReminderRepository.existsById(id)) {
            throw new EntityNotFoundException("AIReminder not found with id: " + id);
        }
        return aIReminderRepository.save(entity);
    }

    @Override
    public void deleteById(Long id) {
        if (!aIReminderRepository.existsById(id)) {
            throw new EntityNotFoundException("AIReminder not found with id: " + id);
        }
        aIReminderRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return aIReminderRepository.existsById(id);
    }
}
