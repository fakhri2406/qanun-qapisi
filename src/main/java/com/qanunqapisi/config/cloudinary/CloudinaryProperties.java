package com.qanunqapisi.config.cloudinary;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Component
@ConfigurationProperties(prefix = "cloudinary")
@Validated
@Data
public class CloudinaryProperties {
    @NotBlank
    private String cloudName;
    
    @NotBlank
    private String apiKey;
    
    @NotBlank
    private String apiSecret;
}
