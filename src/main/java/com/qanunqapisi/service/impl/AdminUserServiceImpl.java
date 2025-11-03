package com.qanunqapisi.service.impl;

import com.qanunqapisi.domain.Role;
import com.qanunqapisi.domain.User;
import com.qanunqapisi.dto.request.admin.CreateUserRequest;
import com.qanunqapisi.dto.request.admin.UpdateUserRequest;
import com.qanunqapisi.dto.response.admin.AdminUserResponse;
import com.qanunqapisi.repository.RoleRepository;
import com.qanunqapisi.repository.UserRepository;
import com.qanunqapisi.service.AdminUserService;
import com.qanunqapisi.util.Hasher;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.UUID;

import static com.qanunqapisi.util.ErrorMessages.*;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AdminUserServiceImpl implements AdminUserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final Hasher hasher;

    @Override
    @Transactional(readOnly = true)
    public Page<AdminUserResponse> listUsers(String role, Boolean isActive, Boolean isVerified, String search, Pageable pageable) {
        Specification<User> spec = (root, query, cb) -> cb.conjunction();
        
        if (role != null && !role.isEmpty()) {
            Role roleEntity = roleRepository.findByTitle(role).orElse(null);
            if (roleEntity != null) {
                UUID roleId = roleEntity.getId();
                spec = spec.and((root, query, cb) -> cb.equal(root.get("roleId"), roleId));
            }
        }
        
        if (isActive != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("isActive"), isActive));
        }
        
        if (isVerified != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("isVerified"), isVerified));
        }
        
        if (search != null && !search.isEmpty()) {
            String searchLower = search.toLowerCase();
            spec = spec.and((root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("email")), "%" + searchLower + "%"),
                cb.like(cb.lower(root.get("firstName")), "%" + searchLower + "%"),
                cb.like(cb.lower(root.get("lastName")), "%" + searchLower + "%")
            ));
        }
        
        Page<User> users = userRepository.findAll(spec, pageable);
        return users.map(this::toAdminUserResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminUserResponse getUser(UUID userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new NoSuchElementException(USER_NOT_FOUND));
        return toAdminUserResponse(user);
    }

    @Override
    public AdminUserResponse createUser(@Valid CreateUserRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DataIntegrityViolationException(EMAIL_IN_USE);
        }

        Role role = roleRepository.findByTitle(request.role())
            .orElseThrow(() -> new NoSuchElementException(ROLE_NOT_FOUND));

        String passwordHash = hasher.hash(request.password());

        User user = User.builder()
            .roleId(role.getId())
            .email(request.email())
            .passwordHash(passwordHash)
            .firstName(request.firstName())
            .lastName(request.lastName())
            .isActive(true)
            .isPremium(request.isPremium())
            .isVerified(true)
            .failedLoginAttempts(0)
            .verificationAttempts(0)
            .pendingEmailAttempts(0)
            .passwordResetAttempts(0)
            .build();

        userRepository.save(user);
        return toAdminUserResponse(user);
    }

    @Override
    public AdminUserResponse updateUser(UUID userId, @Valid UpdateUserRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new NoSuchElementException(USER_NOT_FOUND));

        if (request.email() != null && !request.email().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.email())) {
                throw new DataIntegrityViolationException(EMAIL_IN_USE);
            }
            user.setEmail(request.email());
        }

        if (request.firstName() != null) {
            user.setFirstName(request.firstName());
        }

        if (request.lastName() != null) {
            user.setLastName(request.lastName());
        }

        if (request.role() != null) {
            Role newRole = roleRepository.findByTitle(request.role())
                .orElseThrow(() -> new NoSuchElementException(ROLE_NOT_FOUND));
            user.setRoleId(newRole.getId());
        }

        if (request.isPremium() != null) {
            user.setIsPremium(request.isPremium());
        }

        if (request.isActive() != null) {
            user.setIsActive(request.isActive());
        }

        userRepository.save(user);
        return toAdminUserResponse(user);
    }

    @Override
    public void deleteUser(UUID userId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userRepository.findByEmail(auth.getName())
            .orElseThrow(() -> new NoSuchElementException(USER_NOT_FOUND));

        if (currentUser.getId().equals(userId)) {
            throw new IllegalStateException(CANNOT_DELETE_SELF);
        }

        User userToDelete = userRepository.findById(userId)
            .orElseThrow(() -> new NoSuchElementException(USER_NOT_FOUND));

        userRepository.delete(userToDelete);
    }

    private AdminUserResponse toAdminUserResponse(User user) {
        Role role = roleRepository.findById(user.getRoleId())
            .orElseThrow(() -> new NoSuchElementException(ROLE_NOT_FOUND));

        return new AdminUserResponse(
            user.getId(),
            user.getEmail(),
            user.getFirstName(),
            user.getLastName(),
            user.getIsPremium(),
            user.getIsActive(),
            user.getIsVerified(),
            role.getTitle(),
            user.getLastLoginAt(),
            user.getCreatedAt()
        );
    }
}
