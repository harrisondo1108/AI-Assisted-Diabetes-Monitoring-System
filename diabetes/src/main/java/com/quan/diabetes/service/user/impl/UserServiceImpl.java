package com.quan.diabetes.service.user.impl;

import com.quan.diabetes.entity.User;
import com.quan.diabetes.repository.UserRepository;

import com.quan.diabetes.service.user.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Random;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Optional<User> findById(String id) {
        return userRepository.findById(id);
    }

    @Override
    public Optional<User> findByUsernameAndPassword(String username, String password) {
        Optional<User> userOpt = this.findByPhoneNumber(username);
        if (userOpt.isPresent()){
            User user = userOpt.get();
            if (passwordEncoder.matches(password, user.getPasswordHash())) {
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<User> findByPhoneNumber(String phoneNumber) {
        return userRepository.findByPhoneNumber(phoneNumber);
    }

    @Override
    public User create(User entity) {
        User newUser = new User();
        newUser.setUserId(entity.getUserId());
        newUser.setPhoneNumber(entity.getPhoneNumber());
        newUser.setRole(entity.getRole());
        newUser.setPasswordHash(passwordEncoder.encode(entity.getPasswordHash()));
        return userRepository.save(newUser);
    }

    @Override
    public User update(String id, User entity) {
        if (!userRepository.existsById(id)) {
            throw new EntityNotFoundException("User not found with id: " + id);
        }
        User newUser = new User();
        newUser.setUserId(entity.getUserId());
        newUser.setPhoneNumber(entity.getPhoneNumber());
        newUser.setRole(entity.getRole());
        newUser.setStatus(entity.getStatus());
        
        String pwd = entity.getPasswordHash();
        if (pwd != null && (pwd.startsWith("$2a$") || pwd.startsWith("$2b$") || pwd.startsWith("$2y$"))) {
            newUser.setPasswordHash(pwd);
        } else if (pwd != null) {
            newUser.setPasswordHash(passwordEncoder.encode(pwd));
        }
        
        return userRepository.save(newUser);
    }

    @Override
    public String getNewID(String roleId) {
        if (roleId == null) {
            return null;
        }
        String userId = null;
        switch (roleId) {
            case "PAT": {
                do {
                    String number = "00000" + new Random().nextInt(1000000);
                    userId = "P" + number.substring(number.length() - 6);
                } while (userRepository.existsById(userId));
                break;
            }
            case "DOC": {
                do {
                    String number = "00000" + new Random().nextInt(1000000);
                    userId = "D" + number.substring(number.length() - 6);
                } while (userRepository.existsById(userId));
                break;
            }
            default:
                break;
        }
        return userId;
    }
}
