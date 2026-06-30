package com.quan.diabetes.service.ai;

import com.quan.diabetes.entity.AIConversation;
import java.util.List;
import java.util.Optional;

public interface AIConversationService {

    List<AIConversation> findAll();
    Optional<AIConversation> findById(String id);
    List<AIConversation> findByPatientId(String patientId);
    List<AIConversation> findByAssistantId(Integer assistantId);
    List<AIConversation> findByPatientIdAndAssistantId(String patientId, Integer assistantId);
    AIConversation create(AIConversation entity);
    AIConversation update(String id, AIConversation entity);
    void deleteById(String id);
    boolean existsById(String id);
    long countByPatientId(String patientId);
}
