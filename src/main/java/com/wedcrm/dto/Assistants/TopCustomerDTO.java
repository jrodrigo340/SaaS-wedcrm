package com.wedcrm.dto.Assistants;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
public record TopCustomerDTO(
        UUID customerId,
        String customerName,
        String customerEmail,
        String customerPhone,
        String company,
        long totalDeals,
        BigDecimal totalValue,
        String formattedTotalValue,
        String assignedTo
) {}
