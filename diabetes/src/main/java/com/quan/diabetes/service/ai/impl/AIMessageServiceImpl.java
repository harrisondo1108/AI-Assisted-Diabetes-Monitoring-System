package com.quan.diabetes.service.ai.impl;

import com.quan.diabetes.entity.AIMessage;
import com.quan.diabetes.repository.AIMessageRepository;
import com.quan.diabetes.service.ai.AIMessageService;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AIMessageServiceImpl implements AIMessageService {

    private static final Logger logger = LoggerFactory.getLogger(AIMessageServiceImpl.class);
    private final AIMessageRepository aIMessageRepository;

    public AIMessageServiceImpl(AIMessageRepository aIMessageRepository) {
        this.aIMessageRepository = aIMessageRepository;
    }

    @Override
    public List<AIMessage> findAll() {
        return aIMessageRepository.findAll();
    }

    @Override
    public Optional<AIMessage> findById(Long id) {
        return aIMessageRepository.findById(id);
    }

    @Override
    public List<AIMessage> findByConversationId(String conversationId) {
        return aIMessageRepository.findByConversationIdOrderByTimeAsc(conversationId);
    }

    @Override
    public List<AIMessage> findByConversationIdAndSender(String conversationId, String sender) {
        return aIMessageRepository.findByConversationIdAndSender(conversationId, sender);
    }

    @Override
    public long countByConversationId(String conversationId) {
        return aIMessageRepository.countByConversationId(conversationId);
    }

    @Override
    public AIMessage create(AIMessage entity) {
        return aIMessageRepository.save(entity);
    }

    @Override
    public AIMessage update(Long id, AIMessage entity) {
        if (!aIMessageRepository.existsById(id)) {
            throw new EntityNotFoundException("AIMessage not found with id: " + id);
        }
        entity.setAiMessageId(id);
        return aIMessageRepository.save(entity);
    }

    @Override
    public void deleteById(Long id) {
        if (!aIMessageRepository.existsById(id)) {
            throw new EntityNotFoundException("AIMessage not found with id: " + id);
        }
        aIMessageRepository.deleteById(id);
        logger.info("Deleted AIMessage with id: {}", id);
    }

    @Override
    public void deleteByConversationId(String conversationId) {
        List<AIMessage> messages = aIMessageRepository.findByConversationIdOrderByTimeAsc(conversationId);
        if (!messages.isEmpty()) {
            aIMessageRepository.deleteAll(messages);
            logger.info("Deleted all messages for conversation: {}", conversationId);
        }
    }

    @Override
    public boolean existsById(Long id) {
        return aIMessageRepository.existsById(id);
    }

    @Override
    public List<AIMessage> searchByContent(String keyword) {
        return aIMessageRepository.searchByContent(keyword);
    }
}
