package com.qanunqapisi.dto.request.admin;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
    @Email(message = "Invalid email format")
    @Size(min = 1, message = "Email must not be empty if provided")
    String email,

    @Size(max = 100, message = "First name must not exceed 100 characters")
    String firstName,

    @Size(max = 100, message = "Last name must not exceed 100 characters")
    String lastName,

    String role,

    Boolean isPremium,

    Boolean isActive
) {
}
