package com.wedcrm.dto.dashboard;

import lombok.Builder;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Builder
public record OpportunitySummaryDTO(
        UUID id,
        String title,
        UUID customerId,
        String customerName,
        UUID stageId,
        String stageName,
        String stageColor,
        UUID assignedToId,
        String assignedToName,
        BigDecimal value,
        String formattedValue,
        Integer probability,
        LocalDate expectedCloseDate,
        String status,
        Boolean isOverdue
) {}
