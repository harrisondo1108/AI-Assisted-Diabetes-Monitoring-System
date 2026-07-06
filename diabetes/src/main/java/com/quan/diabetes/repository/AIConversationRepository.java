package com.quan.diabetes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.quan.diabetes.entity.AIConversation;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AIConversationRepository extends JpaRepository<AIConversation, String> {
    @Query("SELECT c FROM AIConversation c WHERE c.patient.userId = :patientId ORDER BY c.createdAt DESC")
    List<AIConversation> findByPatientUserIdOrderByCreatedAtDesc(@Param("patientId") String patientId);

    @Query("SELECT c FROM AIConversation c WHERE c.aiAssistant.aiAssistantId = :assistantId ORDER BY c.createdAt DESC")
    List<AIConversation> findByAiAssistantIdOrderByCreatedAtDesc(@Param("assistantId") Integer assistantId);

    @Query("SELECT c FROM AIConversation c WHERE c.patient.userId = :patientId AND c.aiAssistant.aiAssistantId = :assistantId ORDER BY c.createdAt DESC")
    List<AIConversation> findByPatientUserIdAndAiAssistantId(@Param("patientId") String patientId, @Param("assistantId") Integer assistantId);

    @Query("SELECT c FROM AIConversation c WHERE c.topic LIKE %:keyword%")
    List<AIConversation> searchByTopic(@Param("keyword") String keyword);

    // Find conversations created between two datetimes (inclusive start, exclusive end)
    List<AIConversation> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}

