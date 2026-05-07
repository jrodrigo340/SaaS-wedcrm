package com.wedcrm.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record OpportunityProductResponseDTO(
        UUID id,
        UUID opportunityId,
        UUID productId,
        String productName,
        String productSku,
        Integer quantity,
        BigDecimal unitPrice,
        String formattedUnitPrice,
        BigDecimal discount,
        String formattedDiscount,
        BigDecimal totalPrice,
        String formattedTotalPrice,
        BigDecimal subtotal,
        BigDecimal discountAmount,
        Boolean hasDiscount,
        String notes
) {}
