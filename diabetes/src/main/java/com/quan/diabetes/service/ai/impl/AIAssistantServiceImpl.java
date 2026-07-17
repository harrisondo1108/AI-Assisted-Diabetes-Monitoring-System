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

    @Value("${ollama.model:diabetes-ai}")
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

    @Value("${gemini.model:gemini-2.5-flash}")
    private String geminiDefaultModel;

    @Override
    public AIAssistant getDefaultAssistant() {
        return getActiveAssistant();
    }

    @Override
    public synchronized void initDefaultAssistants() {
        List<AIAssistant> all = aIAssistantRepository.findAll();
        boolean hasLocal = false;
        boolean hasGemini = false;

        for (AIAssistant a : all) {
            String name = (a.getAiName() != null) ? a.getAiName().toLowerCase() : "";
            String model = (a.getModelName() != null) ? a.getModelName().toLowerCase() : "";
            if (name.contains("local") || name.contains("ollama") || model.contains("diabetes") || model.contains("ollama")) {
                hasLocal = true;
            }
            if (name.contains("gemini") || model.contains("gemini")) {
                hasGemini = true;
                if (!"gemini-2.5-flash".equalsIgnoreCase(a.getModelName())) {
                    a.setModelName("gemini-2.5-flash");
                    aIAssistantRepository.save(a);
                    logger.info("Auto-migrated existing Gemini Assistant model to gemini-2.5-flash");
                }
            }
        }

        if (!hasLocal && all.isEmpty()) {
            logger.info("Initializing default Local Ollama AI Assistant...");
            AIAssistant local = new AIAssistant();
            local.setAiName("Diabetes AI Specialist (Local Ollama)");
            local.setStatus("Active");
            local.setModelName(defaultModel);
            try {
                create(local);
                hasLocal = true;
            } catch (Exception e) {
                logger.warn("Could not create local assistant: {}", e.getMessage());
            }
        }

        if (!hasGemini) {
            logger.info("Initializing default Gemini Cloud AI Assistant...");
            AIAssistant gemini = new AIAssistant();
            gemini.setAiName("Diabetes AI Specialist (Gemini Cloud)");
            gemini.setStatus("Inactive");
            gemini.setModelName(geminiDefaultModel);
            try {
                create(gemini);
            } catch (Exception e) {
                logger.warn("Could not create gemini assistant: {}", e.getMessage());
            }
        }
    }

    @Override
    public AIAssistant switchActiveAssistant(Integer aiAssistantId) {
        initDefaultAssistants();
        List<AIAssistant> all = aIAssistantRepository.findAll();
        AIAssistant newActive = null;
        for (AIAssistant a : all) {
            if (a.getAiAssistantId() == aiAssistantId) {
                a.setStatus("Active");
                newActive = aIAssistantRepository.save(a);
                logger.info("Switched active AI Assistant to: {} (ID: {})", a.getAiName(), a.getAiAssistantId());
            } else if ("Active".equalsIgnoreCase(a.getStatus())) {
                a.setStatus("Inactive");
                aIAssistantRepository.save(a);
            }
        }
        if (newActive == null) {
            throw new EntityNotFoundException("AIAssistant not found with id: " + aiAssistantId);
        }
        return newActive;
    }

    @Override
    public AIAssistant getActiveAssistant() {
        List<AIAssistant> activeList = findByStatus("Active");
        if (!activeList.isEmpty()) {
            return activeList.get(0);
        }
        // Nếu chưa có trợ lý nào Active, khởi tạo và tìm lại
        initDefaultAssistants();
        activeList = findByStatus("Active");
        if (!activeList.isEmpty()) {
            return activeList.get(0);
        }
        // Fallback chọn trợ lý đầu tiên và bật Active
        List<AIAssistant> all = findAll();
        if (!all.isEmpty()) {
            AIAssistant first = all.get(0);
            first.setStatus("Active");
            return aIAssistantRepository.save(first);
        }
        throw new RuntimeException("Could not find or initialize any AI Assistant");
    }

    @Override
    public AIAssistant getOrCreateDefaultAssistant() {
        return getActiveAssistant();
    }
}
