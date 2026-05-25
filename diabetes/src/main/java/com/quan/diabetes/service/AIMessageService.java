package com.quan.diabetes.service;

import com.quan.diabetes.entity.AIMessage;
import java.util.List;
import java.util.Optional;

public interface AIMessageService {

    public List<AIMessage> findAll();

    public Optional<AIMessage> findById(Long id);

    public AIMessage create(AIMessage entity);

    public AIMessage update(Long id, AIMessage entity);

    public void deleteById(Long id);

    public boolean existsById(Long id);
}
