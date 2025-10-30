package com.qanunqapisi.service;

import com.qanunqapisi.dto.request.admin.CreateUserRequest;
import com.qanunqapisi.dto.request.admin.UpdateUserRequest;
import com.qanunqapisi.dto.response.admin.AdminUserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Service interface for administrative user management operations.
 * Allows admins to manage user accounts, roles, and permissions.
 */
public interface AdminUserService {
    /**
     * Lists all users with pagination.
     *
     * @param pageable pagination information
     * @return page of users with their details
     */
    Page<AdminUserResponse> listUsers(Pageable pageable);

    /**
     * Retrieves detailed information about a specific user.
     *
     * @param userId the ID of the user to retrieve
     * @return the user's details
     * @throws NoSuchElementException if user not found
     */
    AdminUserResponse getUser(UUID userId);

    /**
     * Creates a new user account with specified role and status.
     * User is marked as verified by default.
     *
     * @param request the user creation request
     * @return the created user's details
     * @throws IllegalArgumentException if email already exists
     */
    AdminUserResponse createUser(CreateUserRequest request);

    /**
     * Updates an existing user's information, role, and status.
     *
     * @param userId  the ID of the user to update
     * @param request the user update request
     * @return the updated user's details
     * @throws NoSuchElementException   if user not found
     * @throws IllegalArgumentException if new email already exists
     */
    AdminUserResponse updateUser(UUID userId, UpdateUserRequest request);

    /**
     * Deletes a user account and all associated data.
     *
     * @param userId the ID of the user to delete
     * @throws NoSuchElementException if user not found
     */
    void deleteUser(UUID userId);
}
