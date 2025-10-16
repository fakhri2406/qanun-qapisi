package com.qanunqapisi.service.scheduler;

import java.time.LocalDateTime;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.qanunqapisi.repository.RefreshTokenRepository;
import com.qanunqapisi.repository.RevokedTokenRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class TokenCleanupScheduler {
    private final RefreshTokenRepository refreshTokenRepository;
    private final RevokedTokenRepository revokedTokenRepository;

    /**
     * Clean up expired refresh tokens every hour
     */
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void cleanupExpiredRefreshTokens() {
        try {
            LocalDateTime now = LocalDateTime.now();
            refreshTokenRepository.deleteByExpiresAtBefore(now);
            log.info("Cleaned up expired refresh tokens");
        } catch (Exception e) {
            log.error("Failed to cleanup expired refresh tokens", e);
        }
    }

    /**
     * Clean up expired revoked tokens every 6 hours
     */
    @Scheduled(cron = "0 0 */6 * * *")
    @Transactional
    public void cleanupExpiredRevokedTokens() {
        try {
            LocalDateTime now = LocalDateTime.now();
            revokedTokenRepository.deleteByExpiresAtBefore(now);
            log.info("Cleaned up expired revoked tokens");
        } catch (Exception e) {
            log.error("Failed to cleanup expired revoked tokens", e);
        }
    }
}
