package com.wedcrm.dto.response;

import java.util.UUID;

public record TagResponseDTO(
        UUID id,
        String name,
        String color,
        String validColor,
        String description,
        Integer customerCount,
        Boolean isUsed,
        String createdAt,
        String createdBy
) {}