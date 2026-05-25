package com.quan.diabetes.service.impl;

import com.quan.diabetes.entity.AIAssistant;
import com.quan.diabetes.repository.AIAssistantRepository;
import com.quan.diabetes.service.AIAssistantService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AIAssistantServiceImpl implements AIAssistantService {

    private final AIAssistantRepository aIAssistantRepository;

    public AIAssistantServiceImpl(AIAssistantRepository aIAssistantRepository) {
        this.aIAssistantRepository = aIAssistantRepository;
    }

    @Override
    public List<AIAssistant> findAll() {
        return aIAssistantRepository.findAll();
    }

    @Override
    public Optional<AIAssistant> findById(Integer id) {
        return aIAssistantRepository.findById(id);
    }

    @Override
    public AIAssistant create(AIAssistant entity) {
        return aIAssistantRepository.save(entity);
    }

    @Override
    public AIAssistant update(Integer id, AIAssistant entity) {
        if (!aIAssistantRepository.existsById(id)) {
            throw new EntityNotFoundException("AIAssistant not found with id: " + id);
        }
        return aIAssistantRepository.save(entity);
    }

    @Override
    public void deleteById(Integer id) {
        if (!aIAssistantRepository.existsById(id)) {
            throw new EntityNotFoundException("AIAssistant not found with id: " + id);
        }
        aIAssistantRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Integer id) {
        return aIAssistantRepository.existsById(id);
    }
}
