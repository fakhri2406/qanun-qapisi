package com.qanunqapisi.dto.response.auth;

import java.time.LocalDateTime;
import java.util.UUID;

public record MeResponse(
    UUID id,
    String email,
    String firstName,
    String lastName,
    LocalDateTime dateOfBirth,
    Boolean isPremium,
    String role,
    LocalDateTime lastLoginAt
) {
}
