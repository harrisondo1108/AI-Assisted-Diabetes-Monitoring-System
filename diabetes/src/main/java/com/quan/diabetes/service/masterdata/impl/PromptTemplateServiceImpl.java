package com.quan.diabetes.service.masterdata.impl;

import com.quan.diabetes.entity.PromptTemplate;
import com.quan.diabetes.repository.PromptTemplateRepository;
import com.quan.diabetes.service.masterdata.PromptTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class PromptTemplateServiceImpl implements PromptTemplateService {

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
    public PromptTemplate create(PromptTemplate entity) {
        return promptTemplateRepository.save(entity);
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
    public void deleteById(Integer id) {
        promptTemplateRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Integer id) {
        return promptTemplateRepository.existsById(id);
    }
}