package com.quan.diabetes.service.impl;

import com.quan.diabetes.entity.AIMessage;
import com.quan.diabetes.repository.AIMessageRepository;
import com.quan.diabetes.service.AIMessageService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AIMessageServiceImpl implements AIMessageService {

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
    public AIMessage create(AIMessage entity) {
        return aIMessageRepository.save(entity);
    }

    @Override
    public AIMessage update(Long id, AIMessage entity) {
        if (!aIMessageRepository.existsById(id)) {
            throw new EntityNotFoundException("AIMessage not found with id: " + id);
        }
        return aIMessageRepository.save(entity);
    }

    @Override
    public void deleteById(Long id) {
        if (!aIMessageRepository.existsById(id)) {
            throw new EntityNotFoundException("AIMessage not found with id: " + id);
        }
        aIMessageRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return aIMessageRepository.existsById(id);
    }
}
