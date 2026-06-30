package com.quan.diabetes.service.ai;

import com.quan.diabetes.entity.AIAssistant;
import java.util.List;
import java.util.Optional;

public interface AIAssistantService {

    public List<AIAssistant> findAll();

    public Optional<AIAssistant> findById(Integer id);

    public AIAssistant create(AIAssistant entity);

    public AIAssistant update(Integer id, AIAssistant entity);

    public void deleteById(Integer id);

    public boolean existsById(Integer id);


    List<AIAssistant> findByStatus(String status);

    Optional<AIAssistant> findByModelName(String modelName);

    AIAssistant getDefaultAssistant();

    AIAssistant getOrCreateDefaultAssistant();
}
