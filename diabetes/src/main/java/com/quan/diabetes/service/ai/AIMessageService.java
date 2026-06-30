package com.quan.diabetes.service.ai;

import com.quan.diabetes.entity.AIMessage;
import java.util.List;
import java.util.Optional;

public interface AIMessageService {

    List<AIMessage> findAll();
    Optional<AIMessage> findById(Long id);
    List<AIMessage> findByConversationId(String conversationId);
    List<AIMessage> findByConversationIdAndSender(String conversationId, String sender);
    long countByConversationId(String conversationId);
    AIMessage create(AIMessage entity);
    AIMessage update(Long id, AIMessage entity);
    void deleteById(Long id);
    void deleteByConversationId(String conversationId);
    boolean existsById(Long id);
    List<AIMessage> searchByContent(String keyword);
}
