package com.quan.diabetes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.quan.diabetes.entity.User;
import org.springframework.stereotype.Repository;

import java.util.Optional;


public interface UserRepository extends JpaRepository<User, String> {
    // add custom query methods if needed

    public Optional<User> findByPhoneNumber(String phoneNumber);

    Optional<User> findByPhoneNumberAndPasswordHash(String username, String password);
}

