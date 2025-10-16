package com.qanunqapisi.dto.request.admin;

import jakarta.validation.constraints.Email;

public record UpdateUserRequest(
    @Email(message = "Invalid email format")
    String email,

    String firstName,

    String lastName,

    String role,

    Boolean isPremium,

    Boolean isActive
) {
}
