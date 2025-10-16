package com.qanunqapisi.controller;

import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.qanunqapisi.dto.request.profile.ChangeEmailRequest;
import com.qanunqapisi.dto.request.profile.ChangePasswordRequest;
import com.qanunqapisi.dto.request.profile.UpdateProfileRequest;
import com.qanunqapisi.dto.request.profile.VerifyEmailChangeRequest;
import com.qanunqapisi.dto.response.profile.ProfileResponse;
import com.qanunqapisi.service.ProfileService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/profile")
@Validated
@RequiredArgsConstructor
public class ProfileController {
    private final ProfileService profileService;

    @GetMapping
    public ResponseEntity<ProfileResponse> getProfile() {
        return ResponseEntity.ok(profileService.getProfile());
    }

    @PutMapping
    public ResponseEntity<ProfileResponse> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(profileService.updateProfile(request));
    }

    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        profileService.changePassword(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/change-email")
    public ResponseEntity<Void> requestEmailChange(@Valid @RequestBody ChangeEmailRequest request) {
        profileService.requestEmailChange(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/verify-email-change")
    public ResponseEntity<Void> verifyEmailChange(@Valid @RequestBody VerifyEmailChangeRequest request) {
        profileService.verifyEmailChange(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/picture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> uploadProfilePicture(@RequestParam("file") MultipartFile file) {
        String imageUrl = profileService.uploadProfilePicture(file);
        return ResponseEntity.ok(Map.of("imageUrl", imageUrl));
    }

    @DeleteMapping("/picture")
    public ResponseEntity<Void> deleteProfilePicture() {
        profileService.deleteProfilePicture();
        return ResponseEntity.noContent().build();
    }
}
