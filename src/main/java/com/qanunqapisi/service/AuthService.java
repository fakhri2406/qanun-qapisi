package com.qanunqapisi.service;

import com.qanunqapisi.dto.request.auth.ConfirmResetPasswordRequest;
import com.qanunqapisi.dto.request.auth.LoginRequest;
import com.qanunqapisi.dto.request.auth.RefreshTokenRequest;
import com.qanunqapisi.dto.request.auth.ResendVerificationRequest;
import com.qanunqapisi.dto.request.auth.ResetPasswordRequest;
import com.qanunqapisi.dto.request.auth.SignupRequest;
import com.qanunqapisi.dto.request.auth.VerifyRequest;
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
