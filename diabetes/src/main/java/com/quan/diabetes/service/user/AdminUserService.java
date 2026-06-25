package com.quan.diabetes.service.user;

import com.quan.diabetes.dto.UserManagementDTO;
import org.springframework.data.domain.Page;
import java.util.List;

/**
 * Service interface for admin‑level user management.
 * It groups all operations that involve multiple domain entities (User, Patient, Profile, Role).
 */
public interface AdminUserService {
    /**
     * Retrieve users together with their extended information for admin view, optionally filtered.
     */
    List<UserManagementDTO> getAllUserManagementDTOs(String role, String search);

    /**
     * Retrieve a paged list of user DTOs for admin view.
     * @param role role filter (or "all")
     * @param search search text
     * @param page zero-based page index
     * @param size page size
     */
    Page<UserManagementDTO> getPagedUserManagementDTOs(String role, String search, int page, int size);

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

    // New methods for Thymeleaf MVC
    UserManagementDTO getUserManagementDTOById(String userId);

    /**
     * Check whether a phone number is already used by another account.
     * @param phone phone to check
     * @param excludeUserId optional userId to exclude (when updating)
     * @return true if the phone is taken by a different user
     */
    boolean isPhoneTaken(String phone, String excludeUserId);

}