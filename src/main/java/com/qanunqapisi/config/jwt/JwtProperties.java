package com.qanunqapisi.config.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@ConfigurationProperties(prefix = "jwt")
@Validated
@Data
public class JwtProperties {
    @NotBlank
    private String issuer;

    @NotBlank
    private String audience;

    @NotBlank
    private String secret;

    @Positive
    private long accessTokenValiditySeconds;

    @Positive
    private long refreshTokenValiditySeconds;
}
