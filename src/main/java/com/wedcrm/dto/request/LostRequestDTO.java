package com.wedcrm.dto.request;

import lombok.Builder;

@Builder
public record LostRequestDTO(
        String reason,
        String notes
) {}
