package com.quan.diabetes.repository;

import com.quan.diabetes.entity.PromptTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PromptTemplateRepository extends JpaRepository<PromptTemplate, Integer> {
    @Query("SELECT p FROM PromptTemplate p WHERE p.isActive = true ORDER BY p.createdAt DESC")
    Optional<PromptTemplate> findFirstActiveTemplate();

    @Query("SELECT p FROM PromptTemplate p WHERE p.isActive = true")
    List<PromptTemplate> findAllActive();
}
