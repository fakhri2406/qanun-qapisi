package com.qanunqapisi.service.impl;

import com.qanunqapisi.config.email.EmailProperties;
import com.qanunqapisi.domain.Role;
import com.qanunqapisi.domain.TestAttempt;
import com.qanunqapisi.domain.User;
import com.qanunqapisi.dto.request.profile.ChangeEmailRequest;
import com.qanunqapisi.dto.request.profile.ChangePasswordRequest;
import com.qanunqapisi.dto.request.profile.UpdateProfileRequest;
import com.qanunqapisi.dto.request.profile.VerifyEmailChangeRequest;
import com.qanunqapisi.dto.response.profile.ProfileResponse;
import com.qanunqapisi.exception.EmailSendException;
import com.qanunqapisi.repository.*;
import com.qanunqapisi.service.ProfileService;
import com.qanunqapisi.service.external.email.EmailService;
import com.qanunqapisi.service.external.email.EmailTemplateService;
import com.qanunqapisi.util.Hasher;
import com.qanunqapisi.util.TokenGenerator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

import static com.qanunqapisi.util.ErrorMessages.*;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ProfileServiceImpl implements ProfileService {
    private static final int EMAIL_CHANGE_MAX_ATTEMPTS = 5;
    private static final int EMAIL_CHANGE_LOCK_MINUTES = 60;
    private static final int EMAIL_CHANGE_CODE_TTL_MINUTES = 15;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RevokedTokenRepository revokedTokenRepository;
    private final TestAttemptRepository testAttemptRepository;
    private final UserAnswerRepository userAnswerRepository;
    private final Hasher hasher;
    private final TokenGenerator tokenGenerator;
    private final EmailProperties emailProperties;
    private final EmailService emailService;
    private final EmailTemplateService emailTemplateService;

    @Override
    @Transactional(readOnly = true)
    public ProfileResponse getProfile() {
        User user = getCurrentUser();
        return buildProfileResponse(user);
    }

    @Override
    public ProfileResponse updateProfile(@Valid UpdateProfileRequest request) {
        User user = getCurrentUser();

        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        userRepository.save(user);

        return buildProfileResponse(user);
    }

    @Override
    public void changePassword(@Valid ChangePasswordRequest request) {
        User user = getCurrentUser();

        if (!hasher.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException(INVALID_CURRENT_PASSWORD);
        }

        String newPasswordHash = hasher.hash(request.newPassword());
        user.setPasswordHash(newPasswordHash);
        userRepository.save(user);
    }

    @Override
    public void requestEmailChange(@Valid ChangeEmailRequest request) {
        User user = getCurrentUser();

        if (user.getEmail().equals(request.newEmail())) {
            throw new IllegalArgumentException(NEW_EMAIL_SAME);
        }

        if (userRepository.existsByEmail(request.newEmail())) {
            throw new IllegalArgumentException(EMAIL_IN_USE);
        }

        if (user.getPendingEmailLockedUntil() != null &&
            user.getPendingEmailLockedUntil().isAfter(LocalDateTime.now())) {
            throw new IllegalStateException(EMAIL_CHANGE_LOCKED);
        }

        int code = tokenGenerator.generateCode();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(EMAIL_CHANGE_CODE_TTL_MINUTES);

        user.setPendingEmail(request.newEmail());
        user.setPendingEmailCode(code);
        user.setPendingEmailExpiresAt(expiresAt);
        user.setPendingEmailAttempts(0);
        user.setPendingEmailLockedUntil(null);
        userRepository.save(user);

        try {
            String subject = "Qanun Qapısı - Email Dəyişikliyi";
            String body = emailTemplateService.render("verification", Map.of(
                "code", String.valueOf(code),
                "year", String.valueOf(LocalDateTime.now().getYear()),
                "expiry", String.valueOf(EMAIL_CHANGE_CODE_TTL_MINUTES)
            ));
            emailService.sendEmail(emailProperties.getFrom(), request.newEmail(), subject, body, true);
        } catch (RuntimeException ex) {
            log.error("Failed to send email change verification", ex);
            throw new EmailSendException(FAILED_TO_SEND_EMAIL, ex);
        }
    }

    @Override
    public void verifyEmailChange(@Valid VerifyEmailChangeRequest request) {
        User user = getCurrentUser();

        if (user.getPendingEmail() == null) {
            throw new IllegalStateException(EMAIL_CHANGE_MISMATCH);
        }

        if (user.getPendingEmailLockedUntil() != null &&
            user.getPendingEmailLockedUntil().isAfter(LocalDateTime.now())) {
            throw new IllegalStateException(EMAIL_CHANGE_LOCKED);
        } else if (user.getPendingEmailLockedUntil() != null) {
            user.setPendingEmailAttempts(0);
            user.setPendingEmailLockedUntil(null);
        }

        if (user.getPendingEmailExpiresAt() == null ||
            user.getPendingEmailExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException(EMAIL_CHANGE_EXPIRED);
        }

        if (!request.code().equals(user.getPendingEmailCode())) {
            user.setPendingEmailAttempts(user.getPendingEmailAttempts() + 1);
            if (user.getPendingEmailAttempts() >= EMAIL_CHANGE_MAX_ATTEMPTS) {
                user.setPendingEmailLockedUntil(LocalDateTime.now().plusMinutes(EMAIL_CHANGE_LOCK_MINUTES));
            }
            userRepository.save(user);
            throw new IllegalArgumentException(EMAIL_CHANGE_INVALID);
        }

        user.setEmail(user.getPendingEmail());
        user.setPendingEmail(null);
        user.setPendingEmailCode(null);
        user.setPendingEmailExpiresAt(null);
        user.setPendingEmailAttempts(0);
        user.setPendingEmailLockedUntil(null);
        userRepository.save(user);
    }

    @Override
    public void deleteAccount(String password) {
        User user = getCurrentUser();

        if (!hasher.matches(password, user.getPasswordHash())) {
            throw new BadCredentialsException(INVALID_CURRENT_PASSWORD);
        }

        UUID userId = user.getId();

        List<UUID> testAttemptIds = testAttemptRepository.findByUserId(userId).stream()
            .map(TestAttempt::getId)
            .toList();

        if (!testAttemptIds.isEmpty()) {
            userAnswerRepository.deleteByTestAttemptIdIn(testAttemptIds);
        }

        testAttemptRepository.deleteByUserId(userId);

        refreshTokenRepository.deleteByUserId(userId);

        revokedTokenRepository.deleteByUserId(userId);

        userRepository.delete(user);

        log.info("Successfully deleted user account: {}", userId);
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            throw new IllegalStateException(NOT_AUTHENTICATED);
        }
        return userRepository.findByEmail(auth.getName())
            .orElseThrow(() -> new NoSuchElementException(USER_NOT_FOUND));
    }

    private ProfileResponse buildProfileResponse(User user) {
        Role role = roleRepository.findById(user.getRoleId())
            .orElseThrow(() -> new NoSuchElementException(ROLE_NOT_FOUND));

        return new ProfileResponse(
            user.getId(),
            user.getEmail(),
            user.getFirstName(),
            user.getLastName(),
            user.getDateOfBirth(),
            user.getIsPremium(),
            user.getIsVerified(),
            role.getTitle(),
            user.getLastLoginAt(),
            user.getCreatedAt(),
            user.getUpdatedAt()
        );
    }
}
