package com.qanunqapisi.util;

import com.qanunqapisi.config.jwt.JwtProperties;
import com.qanunqapisi.domain.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TokenGenerator {
    private static final SecureRandom RANDOM = new SecureRandom();
    private final JwtProperties jwtProperties;

    public String generateToken() {
        return UUID.randomUUID().toString();
    }

    public int generateCode() {
        return 100000 + RANDOM.nextInt(900000);
    }

    public String generateAccessToken(User user) {
        return Jwts.builder()
            .setSubject(user.getEmail())
            .setIssuer(jwtProperties.getIssuer())
            .setAudience(jwtProperties.getAudience())
            .setIssuedAt(java.util.Date.from(Instant.now()))
            .setExpiration(java.util.Date.from(Instant.now().plusSeconds(jwtProperties.getAccessTokenValiditySeconds())))
            .claim("userId", user.getId().toString())
            .claim("email", user.getEmail())
            .claim("firstName", user.getFirstName())
            .claim("lastName", user.getLastName())
            .claim("profilePictureUrl", user.getProfilePictureUrl())
            .claim("isActive", user.getIsActive())
            .claim("isPremium", user.getIsPremium())
            .claim("isVerified", user.getIsVerified())
            .signWith(Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes()))
            .compact();
    }

    public String generateRefreshToken(User user) {
        return Jwts.builder()
            .setSubject(user.getEmail())
            .setIssuer(jwtProperties.getIssuer())
            .setAudience(jwtProperties.getAudience())
            .setIssuedAt(java.util.Date.from(Instant.now()))
            .setExpiration(java.util.Date.from(Instant.now().plusSeconds(jwtProperties.getRefreshTokenValiditySeconds())))
            .claim("userId", user.getId().toString())
            .claim("type", "refresh")
            .signWith(Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes()))
            .compact();
    }
}
