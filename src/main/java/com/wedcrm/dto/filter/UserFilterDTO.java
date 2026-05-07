package com.wedcrm.dto.filter;

import com.wedcrm.enums.Role;
import lombok.Builder;

@Builder
public record UserFilterDTO(
        String name,
        String email,
        Role role,
        Boolean active,
        Boolean emailVerified
) {}
