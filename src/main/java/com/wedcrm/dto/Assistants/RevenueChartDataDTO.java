package com.wedcrm.dto.Assistants;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
public record RevenueChartDataDTO(
        String label,
        LocalDate date,
        BigDecimal revenue,
        String formattedRevenue
) {}
