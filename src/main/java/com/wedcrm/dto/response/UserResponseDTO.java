package com.wedcrm.dto.response;

import com.wedcrm.enums.Role;
import lombok.Builder;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record UserResponseDTO(
        UUID id,
        String name,
        String email,
        Role role,
        String phone,
        String avatarUrl,
        Boolean active,
        Boolean emailVerified,
        LocalDateTime lastLogin,
        LocalDateTime createdAt
) {}
