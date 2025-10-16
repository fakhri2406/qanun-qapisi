package com.qanunqapisi.repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.qanunqapisi.domain.RevokedToken;

@Repository
public interface RevokedTokenRepository extends JpaRepository<RevokedToken, UUID> {
    Optional<RevokedToken> findByToken(String token);
    void deleteByExpiresAtBefore(LocalDateTime dateTime);
}
