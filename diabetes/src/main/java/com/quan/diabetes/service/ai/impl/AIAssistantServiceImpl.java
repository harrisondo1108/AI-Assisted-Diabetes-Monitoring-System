package com.quan.diabetes.service.ai.impl;

import com.quan.diabetes.entity.AIAssistant;
import com.quan.diabetes.repository.AIAssistantRepository;
import com.quan.diabetes.service.ai.AIAssistantService;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public  class AIAssistantServiceImpl implements AIAssistantService {
    private static final Logger logger = LoggerFactory.getLogger(AIAssistantServiceImpl.class);
    private final AIAssistantRepository aIAssistantRepository;

    @Value("${ollama.model:diabetes}")
    private String defaultModel;

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
    public List<AIAssistant> findByStatus(String status) {
        return aIAssistantRepository.findByStatus(status);
    }

    @Override
    public Optional<AIAssistant> findByModelName(String modelName) {
        return aIAssistantRepository.findByModelName(modelName);
    }

    @Override
    public AIAssistant create(AIAssistant entity) {
        try {
            return aIAssistantRepository.save(entity);
        } catch (DataIntegrityViolationException e) {
            // Nếu bị trùng tên, tìm và cập nhật
            logger.warn("Duplicate AI Assistant name, updating existing...");
            Optional<AIAssistant> existing = aIAssistantRepository.findAll().stream()
                    .filter(a -> a.getAiName().equalsIgnoreCase(entity.getAiName()))
                    .findFirst();
            if (existing.isPresent()) {
                AIAssistant existingAssistant = existing.get();
                existingAssistant.setStatus(entity.getStatus());
                existingAssistant.setModelName(entity.getModelName());
                return aIAssistantRepository.save(existingAssistant);
            }
            throw e;
        }
    }

    @Override
    public AIAssistant update(Integer id, AIAssistant entity) {
        if (!aIAssistantRepository.existsById(id)) {
            throw new EntityNotFoundException("AIAssistant not found with id: " + id);
        }
        entity.setAiAssistantId(id);
        return aIAssistantRepository.save(entity);
    }

    @Override
    public void deleteById(Integer id) {
        if (!aIAssistantRepository.existsById(id)) {
            throw new EntityNotFoundException("AIAssistant not found with id: " + id);
        }
        aIAssistantRepository.deleteById(id);
        logger.info("Deleted AIAssistant with id: {}", id);
    }

    @Override
    public boolean existsById(Integer id) {
        return aIAssistantRepository.existsById(id);
    }

    @Override
    public AIAssistant getDefaultAssistant() {
        return findById(1)
                .orElseThrow(() -> new EntityNotFoundException("Default AI Assistant not found with id: 1"));
    }

    @Override
    public AIAssistant getOrCreateDefaultAssistant() {
        // Try to find assistant with ID 1
        Optional<AIAssistant> existing = findById(1);
        if (existing.isPresent()) {
            logger.info("Found AI Assistant with ID 1: {}", existing.get().getAiName());
            return existing.get();
        }

        // Try to find by name
        Optional<AIAssistant> byName = aIAssistantRepository.findAll().stream()
                .filter(a -> "Diabetes AI Specialist".equalsIgnoreCase(a.getAiName()))
                .findFirst();
        if (byName.isPresent()) {
            logger.info("Found AI Assistant by name: {}", byName.get().getAiName());
            return byName.get();
        }

        // Try to find any active assistant
        List<AIAssistant> activeAssistants = findByStatus("Active");
        if (!activeAssistants.isEmpty()) {
            logger.info("Found existing active assistant with id: {}", activeAssistants.get(0).getAiAssistantId());
            return activeAssistants.get(0);
        }

        // Create new default assistant
        logger.info("No AI Assistant found, creating default assistant with model: {}", defaultModel);
        AIAssistant defaultAssistant = new AIAssistant();
        defaultAssistant.setAiName("Diabetes AI Specialist");
        defaultAssistant.setStatus("Active");
        defaultAssistant.setModelName(defaultModel);

        try {
            AIAssistant saved = create(defaultAssistant);
            logger.info("Created default AI Assistant with id: {}", saved.getAiAssistantId());
            return saved;
        } catch (DataIntegrityViolationException e) {
            // If still duplicate, try to find and return existing
            logger.warn("Duplicate key error, trying to find existing assistant");
            return aIAssistantRepository.findAll().stream()
                    .filter(a -> "Diabetes AI Specialist".equalsIgnoreCase(a.getAiName()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Could not create or find default assistant"));
        }
    }
}
