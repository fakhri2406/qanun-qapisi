package com.qanunqapisi.config.cloudinary;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class CloudinaryConfig {
    private final CloudinaryProperties cloudinaryProperties;

    @Bean
    public Cloudinary cloudinary() {
        return new Cloudinary(ObjectUtils.asMap(
            "cloud_name", cloudinaryProperties.getCloudName(),
            "api_key", cloudinaryProperties.getApiKey(),
            "api_secret", cloudinaryProperties.getApiSecret(),
            "secure", true
        ));
    }
}
