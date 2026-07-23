package com.quan.diabetes.service.masterdata.impl;

import com.quan.diabetes.entity.PromptTemplate;
import com.quan.diabetes.repository.PromptTemplateRepository;
import com.quan.diabetes.service.masterdata.PromptTemplateService;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PromptTemplateServiceImpl implements PromptTemplateService {
    private static final Logger logger = LoggerFactory.getLogger(PromptTemplateServiceImpl.class);
    @Autowired
    private PromptTemplateRepository promptTemplateRepository;

    @Override
    public List<PromptTemplate> findAll() {
        return promptTemplateRepository.findAll();
    }

    @Override
    public Optional<PromptTemplate> findById(Integer id) {
        return promptTemplateRepository.findById(id);
    }


    @Override
    public PromptTemplate update(Integer id, PromptTemplate entity) {
        if (!promptTemplateRepository.existsById(id)) {
            throw new RuntimeException("PromptTemplate not found with id: " + id);
        }
        entity.setTemplateId(id);
        return promptTemplateRepository.save(entity);
    }


    @Override
    public boolean existsById(Integer id) {
        return promptTemplateRepository.existsById(id);
    }

    public PromptTemplateServiceImpl(PromptTemplateRepository promptTemplateRepository) {
        this.promptTemplateRepository = promptTemplateRepository;
    }

    @Override
    public List<PromptTemplate> findAllActive() {
        return promptTemplateRepository.findAllActive();
    }

    @Override
    public PromptTemplate create(PromptTemplate entity) {
        entity.setCreatedAt(LocalDateTime.now());
        entity.setIsActive(true);
        return promptTemplateRepository.save(entity);
    }

    @Override
    public void deleteById(Integer id) {
        if (!promptTemplateRepository.existsById(id)) {
            throw new EntityNotFoundException("PromptTemplate not found with id: " + id);
        }
        promptTemplateRepository.deleteById(id);
        logger.info("Deleted PromptTemplate with id: {}", id);
    }

}