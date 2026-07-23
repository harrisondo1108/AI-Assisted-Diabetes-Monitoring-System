package com.quan.diabetes.service.user;

import com.quan.diabetes.entity.User;

import java.util.Optional;

public interface UserService {

    Optional<User> findById(String id);

    Optional<User> findByUsernameAndPassword(String username, String password);

    User create(User entity);

    User update(String id, User entity);

    Optional<User> findByPhoneNumber(String phoneNumber);

    String getNewID(String roleId);
}