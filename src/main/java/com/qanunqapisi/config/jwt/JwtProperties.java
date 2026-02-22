package com.qanunqapisi.config.jwt;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "jwt")
@Validated
@Data
public class JwtProperties {
    @NotBlank
    private String issuer;

    @NotBlank
    private String audience;

    @NotBlank
    @Size(min = 32, message = "JWT secret must be at least 32 characters for HMAC-SHA256")
    private String secret;

    @Positive
    private long accessTokenValiditySeconds;

    @Positive
    private long refreshTokenValiditySeconds;
}
