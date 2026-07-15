package com.quan.diabetes.service.user;

import com.quan.diabetes.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserService {

    List<User> findAll();

    Optional<User> findById(String id);

    Optional<User> findByUsernameAndPassword(String username, String password);

    User create(User entity);

    User update(String id, User entity);

    void deleteById(String id);

    boolean existsById(String id);

    Optional<User> findByPhoneNumber(String phoneNumber);

    String getNewID(String roleId);

    void changePassword(String userId, String newPassword);
}