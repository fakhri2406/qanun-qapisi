package com.qanunqapisi.service;

import org.springframework.web.multipart.MultipartFile;

import com.qanunqapisi.dto.request.profile.ChangeEmailRequest;
import com.qanunqapisi.dto.request.profile.ChangePasswordRequest;
import com.qanunqapisi.dto.request.profile.UpdateProfileRequest;
import com.qanunqapisi.dto.request.profile.VerifyEmailChangeRequest;
import com.qanunqapisi.dto.response.profile.ProfileResponse;

public interface ProfileService {
    ProfileResponse getProfile();
    
    ProfileResponse updateProfile(UpdateProfileRequest request);
    
    void changePassword(ChangePasswordRequest request);
    
    void requestEmailChange(ChangeEmailRequest request);
    
    void verifyEmailChange(VerifyEmailChangeRequest request);
    
    String uploadProfilePicture(MultipartFile file);
    
    void deleteProfilePicture();
}
