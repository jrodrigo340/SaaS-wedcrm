package com.wedcrm.dto.auth;

import com.wedcrm.enums.Role;
import java.util.UUID;

public record UserInfoDTO(
        UUID id,
        String name,
        String email,
        Role role,
        String phone,
        String avatarUrl,
        boolean emailVerified
) {}