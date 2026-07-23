package com.quan.diabetes.service.masterdata;

import com.quan.diabetes.entity.PromptTemplate;
import java.util.List;
import java.util.Optional;

public interface PromptTemplateService {
    List<PromptTemplate> findAll();
    Optional<PromptTemplate> findById(Integer id);
    PromptTemplate create(PromptTemplate entity);
    PromptTemplate update(Integer id, PromptTemplate entity);
    void deleteById(Integer id);
    boolean existsById(Integer id);

    List<PromptTemplate> findAllActive();
}