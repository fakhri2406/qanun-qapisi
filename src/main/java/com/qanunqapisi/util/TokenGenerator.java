package com.qanunqapisi.util;

import com.qanunqapisi.config.jwt.JwtProperties;
import com.qanunqapisi.domain.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TokenGenerator {
    private static final SecureRandom RANDOM = new SecureRandom();
    private final JwtProperties jwtProperties;
    @Getter
    private SecretKey signingKey;

    @PostConstruct
    void init() {
        this.signingKey = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(User user, String roleTitle) {
        Instant now = Instant.now();
        return Jwts.builder()
            .setSubject(user.getEmail())
            .setId(UUID.randomUUID().toString())
            .setIssuer(jwtProperties.getIssuer())
            .setAudience(jwtProperties.getAudience())
            .setIssuedAt(Date.from(now))
            .setExpiration(Date.from(now.plusSeconds(jwtProperties.getAccessTokenValiditySeconds())))
            .claim("userId", user.getId().toString())
            .claim("email", user.getEmail())
            .claim("firstName", user.getFirstName())
            .claim("lastName", user.getLastName())
            .claim("isActive", user.getIsActive())
            .claim("isPremium", user.getIsPremium())
            .claim("isVerified", user.getIsVerified())
            .claim("roles", List.of(roleTitle))
            .signWith(signingKey)
            .compact();
    }

    public String generateUUID() {
        return UUID.randomUUID().toString();
    }

    public int generateCode() {
        return 100000 + RANDOM.nextInt(900000);
    }
}
