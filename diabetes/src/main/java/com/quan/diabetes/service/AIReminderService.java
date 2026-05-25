package com.quan.diabetes.service;

import com.quan.diabetes.entity.AIReminder;
import java.util.List;
import java.util.Optional;

public interface AIReminderService {

    public List<AIReminder> findAll();

    public Optional<AIReminder> findById(Long id);

    public AIReminder create(AIReminder entity);

    public AIReminder update(Long id, AIReminder entity);

    public void deleteById(Long id);

    public boolean existsById(Long id);
}
