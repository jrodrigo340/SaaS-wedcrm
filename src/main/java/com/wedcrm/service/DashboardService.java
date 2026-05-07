package com.wedcrm.service;

import com.wedcrm.dto.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface DashboardService {

    DashboardSummaryDTO getDashboardSummary(UUID userId);

    SalesFunnelDTO getSalesFunnel(DateRangeDTO dateRange, UUID userId);

    RevenueChartDTO getRevenueChart(DateRangeDTO dateRange, GroupByType groupBy);

    List<TopCustomerDTO> getTopCustomers(int limit);

    ConversionRateDTO getConversionRate(DateRangeDTO dateRange);

    List<TeamPerformanceDTO> getTeamPerformance(DateRangeDTO dateRange);

    ActivityMetricsDTO getActivityMetrics(UUID userId);
}