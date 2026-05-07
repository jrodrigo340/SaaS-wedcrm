package com.wedcrm.dto.dashboard;

import com.wedcrm.enums.Role;
import lombok.Builder;
import java.util.UUID;

@Builder
public record UserSummaryDTO(
        UUID id,
        String name,
        String email,
        Role role,
        Boolean active
) {}
