package com.qanunqapisi.dto.response.auth;

public record PasswordStrengthResponse(
    int score,
    String level,
    String message,
    String[] suggestions,
    String estimatedCrackTime
) {
}
