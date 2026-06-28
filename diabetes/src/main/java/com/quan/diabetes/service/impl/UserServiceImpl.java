package com.quan.diabetes.service.impl;

import com.quan.diabetes.entity.User;
import com.quan.diabetes.repository.UserRepository;
import com.quan.diabetes.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public Optional<User> findById(String id) {
        return userRepository.findById(id);
    }

    @Override
    public Optional<User> findByUsernameAndPassword(String username, String password) {
        return userRepository.findByPhoneNumberAndPasswordHash(username, password);
    }

    @Override
    public User create(User entity) {
        return userRepository.save(entity);
    }

    @Override
    public User update(String id, User entity) {
        if (!userRepository.existsById(id)) {
            throw new EntityNotFoundException("User not found with id: " + id);
        }

        return userRepository.save(entity);
    }

    @Override
    public void deleteById(String id) {
        if (!userRepository.existsById(id)) {
            throw new EntityNotFoundException("User not found with id: " + id);
        }

        userRepository.deleteById(id);
    }

    @Override
    public boolean existsById(String id) {
        return userRepository.existsById(id);
    }

    @Override
    public Optional<User> findByPhoneNumber(String phoneNumber) {
        return userRepository.findByPhoneNumber(phoneNumber);
    }

    @Override
    public String getNewID(String roleId) {
        String prefix = "USR-";
        if (roleId != null) {
            String roleUpper = roleId.toUpperCase().trim();
            if (roleUpper.startsWith("PAT")) {
                prefix = "PAT-";
            } else if (roleUpper.startsWith("DOC")) {
                prefix = "DOC-";
            } else if (roleUpper.startsWith("AD")) {
                prefix = "AD-";
            } else {
                prefix = roleUpper + "-";
            }
        }
        return prefix + java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }
}