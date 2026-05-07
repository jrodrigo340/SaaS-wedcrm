package com.wedcrm.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record CustomerResponse(
        UUID id,
        String fullName,
        String email,
        String phone,
        String company,
        String status,
        String assignedToName,
        LocalDateTime createdAt
) {}