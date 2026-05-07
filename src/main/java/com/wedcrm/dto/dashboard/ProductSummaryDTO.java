package com.wedcrm.dto.dashboard;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductSummaryDTO(
        UUID id,
        String name,
        String sku,
        BigDecimal price,
        String formattedPrice,
        String category,
        String categoryIcon,
        Boolean isActive
) {}


