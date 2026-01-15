package com.qanunqapisi.service;

import com.qanunqapisi.dto.request.profile.ChangeEmailRequest;
import com.qanunqapisi.dto.request.profile.ChangePasswordRequest;
import com.qanunqapisi.dto.request.profile.UpdateProfileRequest;
import com.qanunqapisi.dto.request.profile.VerifyEmailChangeRequest;
import com.qanunqapisi.dto.response.profile.ProfileResponse;

/**
 * Service interface for user profile management operations.
 * Handles profile updates, password changes, email changes, and profile picture management.
 */
public interface ProfileService {
    /**
     * Retrieves the authenticated user's profile information.
     *
     * @return the user's profile details
     * @throws NoSuchElementException if authenticated user not found
     */
    ProfileResponse getProfile();

    /**
     * Updates the authenticated user's profile information.
     *
     * @param request the profile update request containing new details
     * @return the updated profile information
     * @throws NoSuchElementException if authenticated user not found
     */
    ProfileResponse updateProfile(UpdateProfileRequest request);

    /**
     * Changes the authenticated user's password.
     *
     * @param request the password change request containing old and new passwords
     * @throws NoSuchElementException   if authenticated user not found
     * @throws IllegalArgumentException if old password is incorrect
     */
    void changePassword(ChangePasswordRequest request);

    /**
     * Initiates email change process by sending verification code to new email.
     *
     * @param request the email change request containing new email address
     * @throws NoSuchElementException   if authenticated user not found
     * @throws IllegalArgumentException if new email already exists
     */
    void requestEmailChange(ChangeEmailRequest request);

    /**
     * Confirms email change using verification code sent to new email address.
     *
     * @param request the verification request containing code
     * @throws NoSuchElementException   if authenticated user not found
     * @throws IllegalArgumentException if verification code is invalid or expired
     */
    void verifyEmailChange(VerifyEmailChangeRequest request);

    /**
     * Deletes the authenticated user's account and all associated data.
     * This action is permanent and cannot be undone.
     *
     * @param password the user's current password for confirmation
     * @throws NoSuchElementException if authenticated user not found
     * @throws IllegalArgumentException if password is incorrect
     */
    void deleteAccount(String password);
}
