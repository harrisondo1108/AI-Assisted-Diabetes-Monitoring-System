package com.quan.diabetes.service.impl;

import com.quan.diabetes.entity.AIConversation;
import com.quan.diabetes.repository.AIConversationRepository;
import com.quan.diabetes.service.AIConversationService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AIConversationServiceImpl implements AIConversationService {

    private final AIConversationRepository aIConversationRepository;

    public AIConversationServiceImpl(AIConversationRepository aIConversationRepository) {
        this.aIConversationRepository = aIConversationRepository;
    }

    @Override
    public List<AIConversation> findAll() {
        return aIConversationRepository.findAll();
    }

    @Override
    public Optional<AIConversation> findById(String id) {
        return aIConversationRepository.findById(id);
    }

    @Override
    public AIConversation create(AIConversation entity) {
        return aIConversationRepository.save(entity);
    }

    @Override
    public AIConversation update(String id, AIConversation entity) {
        if (!aIConversationRepository.existsById(id)) {
            throw new EntityNotFoundException("AIConversation not found with id: " + id);
        }
        return aIConversationRepository.save(entity);
    }

    @Override
    public void deleteById(String id) {
        if (!aIConversationRepository.existsById(id)) {
            throw new EntityNotFoundException("AIConversation not found with id: " + id);
        }
        aIConversationRepository.deleteById(id);
    }

    @Override
    public boolean existsById(String id) {
        return aIConversationRepository.existsById(id);
    }
}
