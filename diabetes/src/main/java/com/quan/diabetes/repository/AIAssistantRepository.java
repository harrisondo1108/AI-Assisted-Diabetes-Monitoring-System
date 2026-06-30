package com.quan.diabetes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.quan.diabetes.entity.AIAssistant;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AIAssistantRepository extends JpaRepository<AIAssistant, Integer> {
    @Query("SELECT a FROM AIAssistant a WHERE a.status = :status")
    List<AIAssistant> findByStatus(@Param("status") String status);

    @Query("SELECT a FROM AIAssistant a WHERE a.modelName = :modelName")
    Optional<AIAssistant> findByModelName(@Param("modelName") String modelName);

    @Query("SELECT a FROM AIAssistant a WHERE a.aiName LIKE %:keyword%")
    List<AIAssistant> searchByKeyword(@Param("keyword") String keyword);

    @Query("SELECT a FROM AIAssistant a WHERE a.aiName = :name")
    Optional<AIAssistant> findByAiName(@Param("name") String name);
}

