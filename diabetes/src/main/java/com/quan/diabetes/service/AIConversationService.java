package com.quan.diabetes.service;

import com.quan.diabetes.entity.AIConversation;
import java.util.List;
import java.util.Optional;

public interface AIConversationService {

    public List<AIConversation> findAll();

    public Optional<AIConversation> findById(String id);

    public AIConversation create(AIConversation entity);

    public AIConversation update(String id, AIConversation entity);

    public void deleteById(String id);

    public boolean existsById(String id);
}
