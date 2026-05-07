package com.wedcrm.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ProductResponseDTO(
        UUID id,
        String name,
        String description,
        String sku,
        BigDecimal price,
        String formattedPrice,
        String unit,
        String priceWithUnit,
        String category,
        String categoryIcon,
        Boolean isActive,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String createdBy
) {}

