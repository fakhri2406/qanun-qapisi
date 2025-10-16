package com.qanunqapisi.dto.response.admin;

import java.time.LocalDateTime;
import java.util.UUID;

public record AdminUserResponse(
    UUID id,
    String email,
    String firstName,
    String lastName,
    Boolean isPremium,
    Boolean isActive,
    Boolean isVerified,
    String role,
    LocalDateTime lastLoginAt,
    LocalDateTime createdAt
) {}
