package com.qanunqapisi.service;

import com.qanunqapisi.dto.request.admin.CreateUserRequest;
import com.qanunqapisi.dto.request.admin.UpdateUserRequest;
import com.qanunqapisi.dto.response.admin.AdminUserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface AdminUserService {
    Page<AdminUserResponse> listUsers(Pageable pageable);
    
    AdminUserResponse getUser(UUID userId);
    
    AdminUserResponse createUser(CreateUserRequest request);
    
    AdminUserResponse updateUser(UUID userId, UpdateUserRequest request);
    
    void deleteUser(UUID userId);
}
