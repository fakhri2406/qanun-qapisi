package com.qanunqapisi.config.admin;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.qanunqapisi.domain.Role;
import com.qanunqapisi.domain.User;
import com.qanunqapisi.repository.RoleRepository;
import com.qanunqapisi.repository.UserRepository;
import com.qanunqapisi.util.Hasher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements ApplicationRunner {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AdminProperties adminProperties;
    private final Hasher hasher;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        List<String> roles = List.of("ADMIN", "CUSTOMER");
        for (String roleName : roles) {
            roleRepository.findByTitle(roleName).orElseGet(() -> {
                Role role = Role.builder().title(roleName).build();
                return roleRepository.save(role);
            });
        }

        String email = adminProperties.getEmail();
        if (userRepository.findByEmail(email).isEmpty()) {
            Role adminRole = roleRepository.findByTitle("ADMIN")
                .orElseThrow(() -> new IllegalStateException("ADMIN role not found"));

            String passwordHash = hasher.hash(adminProperties.getPassword());

            User admin = User.builder()
                .roleId(adminRole.getId())
                .email(email)
                .passwordHash(passwordHash)
                .firstName(adminProperties.getFirstName())
                .lastName(adminProperties.getLastName())
                .isActive(true)
                .isPremium(true)
                .isVerified(true)
                .failedLoginAttempts(0)
                .verificationAttempts(0)
                .pendingEmailAttempts(0)
                .passwordResetAttempts(0)
                .lastLoginAt(LocalDateTime.now())
                .build();

            userRepository.save(admin);
            log.info("Default admin user created successfully: {}", email);
        } else {
            log.info("Admin user already exists, skipping admin creation");
        }
    }
}
