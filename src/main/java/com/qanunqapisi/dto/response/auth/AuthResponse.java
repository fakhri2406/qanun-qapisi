package com.qanunqapisi.dto.response.auth;

public record AuthResponse(
    String accessToken,
    String refreshToken
) {}
