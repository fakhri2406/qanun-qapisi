package com.qanunqapisi.service.impl;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.qanunqapisi.config.email.EmailProperties;
import com.qanunqapisi.config.jwt.JwtProperties;
import com.qanunqapisi.domain.RefreshToken;
import com.qanunqapisi.domain.RevokedToken;
import com.qanunqapisi.domain.Role;
import com.qanunqapisi.domain.User;
import com.qanunqapisi.dto.request.auth.ConfirmResetPasswordRequest;
import com.qanunqapisi.dto.request.auth.LoginRequest;
import com.qanunqapisi.dto.request.auth.RefreshTokenRequest;
import com.qanunqapisi.dto.request.auth.ResendVerificationRequest;
import com.qanunqapisi.dto.request.auth.ResetPasswordRequest;
import com.qanunqapisi.dto.request.auth.SignupRequest;
import com.qanunqapisi.dto.request.auth.VerifyRequest;
import com.qanunqapisi.dto.response.auth.AuthResponse;
import com.qanunqapisi.dto.response.auth.MeResponse;
import com.qanunqapisi.exception.EmailSendException;
import com.qanunqapisi.repository.RefreshTokenRepository;
import com.qanunqapisi.repository.RevokedTokenRepository;
import com.qanunqapisi.repository.RoleRepository;
import com.qanunqapisi.repository.UserRepository;
import com.qanunqapisi.service.AuthService;
import com.qanunqapisi.service.external.email.EmailService;
import com.qanunqapisi.service.external.email.EmailTemplateService;
import static com.qanunqapisi.util.ErrorMessages.ACCOUNT_LOCKED;
import static com.qanunqapisi.util.ErrorMessages.ACCOUNT_NOT_VERIFIED;
import static com.qanunqapisi.util.ErrorMessages.EMAIL_IN_USE;
import static com.qanunqapisi.util.ErrorMessages.FAILED_TO_SEND_EMAIL;
import static com.qanunqapisi.util.ErrorMessages.INVALID_CREDENTIALS;
import static com.qanunqapisi.util.ErrorMessages.INVALID_REFRESH_TOKEN;
import static com.qanunqapisi.util.ErrorMessages.NOT_AUTHENTICATED;
import static com.qanunqapisi.util.ErrorMessages.PASSWORD_RESET_EXPIRED;
import static com.qanunqapisi.util.ErrorMessages.PASSWORD_RESET_INVALID;
import static com.qanunqapisi.util.ErrorMessages.PASSWORD_RESET_LOCKED;
import static com.qanunqapisi.util.ErrorMessages.REFRESH_TOKEN_EXPIRED;
import static com.qanunqapisi.util.ErrorMessages.RESEND_COOLDOWN;
import static com.qanunqapisi.util.ErrorMessages.ROLE_NOT_FOUND;
import static com.qanunqapisi.util.ErrorMessages.USER_NOT_FOUND;
import static com.qanunqapisi.util.ErrorMessages.VERIFICATION_EXPIRED;
import static com.qanunqapisi.util.ErrorMessages.VERIFICATION_INVALID;
import static com.qanunqapisi.util.ErrorMessages.VERIFICATION_LOCKED;
import com.qanunqapisi.util.Hasher;
import com.qanunqapisi.util.TokenGenerator;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {
    private static final int LOGIN_MAX_ATTEMPTS = 5;
    private static final int LOGIN_LOCK_MINUTES = 30;
    private static final int VERIFY_MAX_ATTEMPTS = 5;
    private static final int VERIFY_LOCK_MINUTES = 60;
    private static final int VERIFY_CODE_TTL_MINUTES = 15;
    private static final int RESEND_COOLDOWN_SECONDS = 60;
    private static final int RESET_MAX_ATTEMPTS = 5;
    private static final int RESET_LOCK_MINUTES = 60;
    private static final int RESET_TOKEN_TTL_MINUTES = 30;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RevokedTokenRepository revokedTokenRepository;
    private final Hasher hasher;
    private final TokenGenerator tokenGenerator;
    private final JwtProperties jwtProperties;
    private final EmailProperties emailProperties;
    private final EmailService emailService;
    private final EmailTemplateService emailTemplateService;

    @Value("${app.device-bypass-email:}")
    private String deviceBypassEmail;

    @Override
    public void signup(@Valid SignupRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DataIntegrityViolationException(EMAIL_IN_USE);
        }

        Role customerRole = roleRepository.findByTitle("CUSTOMER")
            .orElseThrow(() -> new IllegalStateException(ROLE_NOT_FOUND));

        String passwordHash = hasher.hash(request.password());
        int code = tokenGenerator.generateCode();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(VERIFY_CODE_TTL_MINUTES);

        User user = User.builder()
            .roleId(customerRole.getId())
            .email(request.email())
            .passwordHash(passwordHash)
            .firstName(request.firstName())
            .lastName(request.lastName())
            .isActive(true)
            .isPremium(false)
            .isVerified(false)
            .verificationCode(code)
            .verificationCodeExpiresAt(expiresAt)
            .verificationAttempts(0)
            .verificationLastSentAt(LocalDateTime.now())
            .failedLoginAttempts(0)
            .pendingEmailAttempts(0)
            .passwordResetAttempts(0)
            .build();

        userRepository.save(user);

        try {
            String subject = "Qanun Qapısı - Email Təsdiqlənməsi";
            String body = emailTemplateService.render("verification", Map.of(
                "code", String.valueOf(code),
                "year", String.valueOf(LocalDateTime.now().getYear()),
                "expiry", String.valueOf(VERIFY_CODE_TTL_MINUTES)
            ));
            emailService.sendEmail(emailProperties.getFrom(), user.getEmail(), subject, body, true);
        } catch (RuntimeException ex) {
            log.error("Failed to send verification email", ex);
        }
    }

    @Override
    public AuthResponse verify(@Valid VerifyRequest request) {
        User user = userRepository.findByEmail(request.email())
            .orElseThrow(() -> new NoSuchElementException(USER_NOT_FOUND));

        if (Boolean.TRUE.equals(user.getIsVerified())) {
            return createAuthResponse(user);
        }

        if (user.getVerificationLockedUntil() != null && user.getVerificationLockedUntil().isAfter(LocalDateTime.now())) {
            throw new IllegalStateException(VERIFICATION_LOCKED);
        }

        if (user.getVerificationCodeExpiresAt() == null || user.getVerificationCodeExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException(VERIFICATION_EXPIRED);
        }

        if (!request.code().equals(user.getVerificationCode())) {
            user.setVerificationAttempts(user.getVerificationAttempts() + 1);
            if (user.getVerificationAttempts() >= VERIFY_MAX_ATTEMPTS) {
                user.setVerificationLockedUntil(LocalDateTime.now().plusMinutes(VERIFY_LOCK_MINUTES));
            }
            userRepository.save(user);
            throw new IllegalArgumentException(VERIFICATION_INVALID);
        }

        user.setIsVerified(true);
        user.setVerificationCode(null);
        user.setVerificationCodeExpiresAt(null);
        user.setVerificationAttempts(0);
        user.setVerificationLockedUntil(null);
        userRepository.save(user);

        return createAuthResponse(user);
    }

    @Override
    public void resend(@Valid ResendVerificationRequest request) {
        User user = userRepository.findByEmail(request.email())
            .orElseThrow(() -> new NoSuchElementException(USER_NOT_FOUND));

        if (Boolean.TRUE.equals(user.getIsVerified())) {
            throw new IllegalStateException("Account already verified");
        }

        if (user.getVerificationLastSentAt() != null &&
            user.getVerificationLastSentAt().plusSeconds(RESEND_COOLDOWN_SECONDS).isAfter(LocalDateTime.now())) {
            throw new IllegalStateException(RESEND_COOLDOWN);
        }

        int code = tokenGenerator.generateCode();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(VERIFY_CODE_TTL_MINUTES);

        user.setVerificationCode(code);
        user.setVerificationCodeExpiresAt(expiresAt);
        user.setVerificationAttempts(0);
        user.setVerificationLockedUntil(null);
        user.setVerificationLastSentAt(LocalDateTime.now());
        userRepository.save(user);

        try {
            String subject = "Qanun Qapısı - Email Təsdiqlənməsi";
            String body = emailTemplateService.render("verification", Map.of(
                "code", String.valueOf(code),
                "year", String.valueOf(LocalDateTime.now().getYear()),
                "expiry", String.valueOf(VERIFY_CODE_TTL_MINUTES)
            ));
            emailService.sendEmail(emailProperties.getFrom(), user.getEmail(), subject, body, true);
        } catch (RuntimeException ex) {
            log.error("Failed to send verification email", ex);
            throw new EmailSendException(FAILED_TO_SEND_EMAIL, ex);
        }
    }

    @Override
    public AuthResponse login(@Valid LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
            .orElseThrow(() -> new BadCredentialsException(INVALID_CREDENTIALS));

        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now())) {
            throw new IllegalStateException(ACCOUNT_LOCKED);
        }

        if (!hasher.matches(request.password(), user.getPasswordHash())) {
            user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);

            if (user.getFailedLoginAttempts() >= LOGIN_MAX_ATTEMPTS) {
                user.setLockedUntil(LocalDateTime.now().plusMinutes(LOGIN_LOCK_MINUTES));
            }

            userRepository.save(user);
            throw new BadCredentialsException(INVALID_CREDENTIALS);
        }

        if (!Boolean.TRUE.equals(user.getIsVerified())) {
            throw new IllegalStateException(ACCOUNT_NOT_VERIFIED);
        }

        boolean isBypassUser = deviceBypassEmail != null && 
                               !deviceBypassEmail.isEmpty() && 
                               deviceBypassEmail.equalsIgnoreCase(user.getEmail());
        
        if (!isBypassUser) {
            if (user.getDeviceId() != null && !user.getDeviceId().equals(request.deviceId())) {
                throw new IllegalStateException("Bu hesab artıq başqa cihazda istifadə olunur. Əvvəlcə digər cihazdan çıxış edin.");
            }
            user.setDeviceId(request.deviceId());
        } else {
            log.debug("Device ID check bypassed for user: {}", user.getEmail());
        }

        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        return createAuthResponse(user);
    }

    @Override
    public AuthResponse refresh(@Valid RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.refreshToken())
            .orElseThrow(() -> new BadCredentialsException(INVALID_REFRESH_TOKEN));

        if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new BadCredentialsException(REFRESH_TOKEN_EXPIRED);
        }

        User user = userRepository.findById(refreshToken.getUserId())
            .orElseThrow(() -> new NoSuchElementException(USER_NOT_FOUND));

        String accessToken = tokenGenerator.generateAccessToken(user);
        return new AuthResponse(accessToken, refreshToken.getToken());
    }

    @Override
    public void logout(String accessToken) {
        if (accessToken != null) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getName() != null) {
                User user = userRepository.findByEmail(auth.getName())
                    .orElseThrow(() -> new NoSuchElementException(USER_NOT_FOUND));

                RevokedToken revokedToken = RevokedToken.builder()
                    .userId(user.getId())
                    .token(accessToken)
                    .expiresAt(LocalDateTime.now().plusSeconds(jwtProperties.getAccessTokenValiditySeconds()))
                    .build();
                revokedTokenRepository.save(revokedToken);

                refreshTokenRepository.deleteByUserId(user.getId());
                
                boolean isBypassUser = deviceBypassEmail != null && 
                                       !deviceBypassEmail.isEmpty() && 
                                       deviceBypassEmail.equalsIgnoreCase(user.getEmail());
                
                if (!isBypassUser) {
                    user.setDeviceId(null);
                }
                userRepository.save(user);
            }
        }
    }

    @Override
    public void logoutFromHeader(String authorizationHeader) {
        String token = null;
        if (authorizationHeader != null && authorizationHeader.toLowerCase().startsWith("bearer ")) {
            token = authorizationHeader.substring(authorizationHeader.indexOf(' ') + 1);
        }
        logout(token);
    }

    @Override
    public MeResponse me() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            throw new IllegalStateException(NOT_AUTHENTICATED);
        }

        User user = userRepository.findByEmail(auth.getName())
            .orElseThrow(() -> new NoSuchElementException(USER_NOT_FOUND));

        Role role = roleRepository.findById(user.getRoleId())
            .orElseThrow(() -> new NoSuchElementException(ROLE_NOT_FOUND));

        return new MeResponse(
            user.getId(),
            user.getEmail(),
            user.getFirstName(),
            user.getLastName(),
            user.getDateOfBirth(),
            user.getProfilePictureUrl(),
            user.getIsPremium(),
            role.getTitle(),
            user.getLastLoginAt()
        );
    }

    @Override
    public void resetPassword(@Valid ResetPasswordRequest request) {
        User user = userRepository.findByEmail(request.email())
            .orElseThrow(() -> new NoSuchElementException(USER_NOT_FOUND));

        if (user.getPasswordResetLockedUntil() != null &&
            user.getPasswordResetLockedUntil().isAfter(LocalDateTime.now())) {
            throw new IllegalStateException(PASSWORD_RESET_LOCKED);
        }

        String token = tokenGenerator.generateUUID();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(RESET_TOKEN_TTL_MINUTES);

        user.setPasswordResetToken(token);
        user.setPasswordResetExpiresAt(expiresAt);
        user.setPasswordResetAttempts(0);
        userRepository.save(user);

        try {
            String subject = "Qanun Qapısı - Şifrə Sıfırlama";
            String body = emailTemplateService.render("password-reset", Map.of(
                "token", token,
                "year", String.valueOf(LocalDateTime.now().getYear()),
                "expiry", String.valueOf(RESET_TOKEN_TTL_MINUTES)
            ));
            emailService.sendEmail(emailProperties.getFrom(), user.getEmail(), subject, body, true);
        } catch (RuntimeException ex) {
            log.error("Failed to send password reset email", ex);
            throw new EmailSendException(FAILED_TO_SEND_EMAIL, ex);
        }
    }

    @Override
    public void confirmResetPassword(@Valid ConfirmResetPasswordRequest request) {
        User user = userRepository.findByEmail(request.email())
            .orElseThrow(() -> new NoSuchElementException(USER_NOT_FOUND));

        if (user.getPasswordResetToken() == null || !user.getPasswordResetToken().equals(request.token())) {
            user.setPasswordResetAttempts(user.getPasswordResetAttempts() + 1);
            if (user.getPasswordResetAttempts() >= RESET_MAX_ATTEMPTS) {
                user.setPasswordResetLockedUntil(LocalDateTime.now().plusMinutes(RESET_LOCK_MINUTES));
            }
            userRepository.save(user);
            throw new IllegalArgumentException(PASSWORD_RESET_INVALID);
        }

        if (user.getPasswordResetExpiresAt() == null ||
            user.getPasswordResetExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException(PASSWORD_RESET_EXPIRED);
        }

        String passwordHash = hasher.hash(request.newPassword());

        user.setPasswordHash(passwordHash);
        user.setPasswordResetToken(null);
        user.setPasswordResetExpiresAt(null);
        user.setPasswordResetAttempts(0);
        user.setPasswordResetLockedUntil(null);
        userRepository.save(user);
    }

    private AuthResponse createAuthResponse(User user) {
        String accessToken = tokenGenerator.generateAccessToken(user);
        String refreshToken = generateRefreshToken(user);
        return new AuthResponse(accessToken, refreshToken);
    }

    private String generateRefreshToken(User user) {
        String token = tokenGenerator.generateUUID();
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(jwtProperties.getRefreshTokenValiditySeconds());

        RefreshToken refreshToken = RefreshToken.builder()
            .userId(user.getId())
            .token(token)
            .expiresAt(expiresAt)
            .build();

        refreshTokenRepository.save(refreshToken);
        return token;
    }
}
