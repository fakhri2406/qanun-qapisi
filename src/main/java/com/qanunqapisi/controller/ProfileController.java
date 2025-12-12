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
import com.qanunqapisi.dto.request.profile.DeleteAccountRequest;
import com.qanunqapisi.dto.request.profile.UpdateProfileRequest;
import com.qanunqapisi.dto.request.profile.VerifyEmailChangeRequest;
import com.qanunqapisi.dto.response.error.ErrorResponse;
import com.qanunqapisi.dto.response.profile.ProfileResponse;
import com.qanunqapisi.service.ProfileService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/profile")
@Validated
@RequiredArgsConstructor
@Tag(name = "Profile", description = "User profile management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class ProfileController {
    private final ProfileService profileService;

    @GetMapping
    @Operation(summary = "Get user profile", description = "Retrieves the authenticated user's profile information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Profile retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ProfileResponse> getProfile() {
        return ResponseEntity.ok(profileService.getProfile());
    }

    @PutMapping
    @Operation(summary = "Update user profile", description = "Updates the authenticated user's profile information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Profile updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ProfileResponse> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(profileService.updateProfile(request));
    }

    @PostMapping("/change-password")
    @Operation(summary = "Change password", description = "Changes the authenticated user's password")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Password changed successfully"),
        @ApiResponse(responseCode = "400", description = "Old password is incorrect", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        profileService.changePassword(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/change-email")
    @Operation(summary = "Request email change", description = "Initiates email change by sending verification code to new email")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Verification code sent successfully"),
        @ApiResponse(responseCode = "400", description = "Email already exists", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> requestEmailChange(@Valid @RequestBody ChangeEmailRequest request) {
        profileService.requestEmailChange(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/verify-email-change")
    @Operation(summary = "Verify email change", description = "Confirms email change using verification code")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Email changed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid or expired code", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> verifyEmailChange(@Valid @RequestBody VerifyEmailChangeRequest request) {
        profileService.verifyEmailChange(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/picture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload profile picture", description = "Uploads a new profile picture (max 5MB, jpg/png)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Picture uploaded successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid file", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Map<String, String>> uploadProfilePicture(@RequestParam("file") MultipartFile file) {
        String imageUrl = profileService.uploadProfilePicture(file);
        return ResponseEntity.ok(Map.of("imageUrl", imageUrl));
    }

    @DeleteMapping("/picture")
    @Operation(summary = "Delete profile picture", description = "Removes the user's profile picture")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Picture deleted successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deleteProfilePicture() {
        profileService.deleteProfilePicture();
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/account")
    @Operation(summary = "Delete account", description = "Permanently deletes the user's account and all associated data. This action cannot be undone.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Account deleted successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid password", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deleteAccount(@Valid @RequestBody DeleteAccountRequest request) {
        profileService.deleteAccount(request.password());
        return ResponseEntity.noContent().build();
    }
}
