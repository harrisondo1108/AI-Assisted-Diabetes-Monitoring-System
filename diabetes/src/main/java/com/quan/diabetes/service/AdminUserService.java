package com.quan.diabetes.service;

import com.quan.diabetes.dto.UserManagementDTO;
import java.util.List;

/**
 * Service interface for admin‑level user management.
 * It groups all operations that involve multiple domain entities (User, Patient, Profile, Role).
 */
public interface AdminUserService {
    /**
     * Retrieve all users together with their extended information for admin view.
     */
    List<UserManagementDTO> getAllUserManagementDTOs();

    /**
     * Create a new user together with the related patient/profile records.
     */
    UserManagementDTO createUserManagementDTO(UserManagementDTO dto);

    /**
     * Update an existing user and its related details.
     */
    UserManagementDTO updateUserManagementDTO(String userId, UserManagementDTO dto);

    /**
     * Toggle the lock status of a user between ACTIVE and LOCKED.
     */
    void toggleLock(String userId);
}
