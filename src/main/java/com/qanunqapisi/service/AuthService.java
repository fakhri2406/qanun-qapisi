package com.qanunqapisi.service;

import com.qanunqapisi.dto.request.auth.*;
import com.qanunqapisi.dto.response.auth.AuthResponse;
import com.qanunqapisi.dto.response.auth.MeResponse;

public interface AuthService {
    void signup(SignupRequest request);

    AuthResponse verify(VerifyRequest request);

    void resend(ResendVerificationRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refresh(RefreshTokenRequest request);

    void logout(String accessToken);

    void logoutFromHeader(String authorizationHeader);

    MeResponse me();

    void resetPassword(ResetPasswordRequest request);

    void confirmResetPassword(ConfirmResetPasswordRequest request);
}
