package com.quan.diabetes.service;

import com.quan.diabetes.entity.User;
import java.util.List;
import java.util.Optional;

public interface UserService {

    public List<User> findAll();

    public Optional<User> findById(String id);

    public Optional<User> findByUsernameAndPassword(String username, String password);

    public User create(User entity);

    public User update(String id, User entity);

    public void deleteById(String id);

    public boolean existsById(String id);
}
