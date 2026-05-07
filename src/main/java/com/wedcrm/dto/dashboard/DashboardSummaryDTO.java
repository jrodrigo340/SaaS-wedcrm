package com.wedcrm.dto.dashboard;

import lombok.Builder;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Builder
public record DashboardSummaryDTO(
        long totalCustomers,
        long totalLeads,
        long totalProspects,
        long totalCustomersWon,
        long openOpportunitiesCount,
        BigDecimal totalPipelineValue,
        String formattedPipelineValue,
        long pendingActivities,
        long overdueActivities,
        BigDecimal averageDealValue,
        String formattedAverageDealValue,
        double conversionRate,
        long newCustomersLastMonth
) {}