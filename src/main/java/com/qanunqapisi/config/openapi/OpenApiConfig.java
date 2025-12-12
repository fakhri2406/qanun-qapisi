package com.qanunqapisi.config.openapi;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
            .addServersItem(new Server()
                .url("https://vmi2809419.contaboserver.net/api/v1")
                .description("Production server"))
            .addServersItem(new Server()
                .url("http://localhost:8080/api/v1")
                .description("Local development server"))
            .info(new Info()
                .title("Qanun Qapısı API")
                .version("1.0.0")
                .description("API documentation for Qanun Qapısı - Azerbaijani Law Exam Prep Application")
                .contact(new Contact()
                    .name("Qanun Qapısı Team")
                    .email("qanunqapisi@gmail.com"))
                .license(new License()
                    .name("Private")
                    .url("https://qanunqapisi.az")))
            .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
            .components(new Components()
                .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                    .name(securitySchemeName)
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
                    .description("JWT authentication using Bearer token")));
    }
}
