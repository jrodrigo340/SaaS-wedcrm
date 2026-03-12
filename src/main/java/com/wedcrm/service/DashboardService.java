package com.wedcrm.service;

import java.util.List;
import java.util.UUID;

public interface DashboardService {

    DashboardSummaryDTO getDashboardSummary(UUID userId);

    SalesFunnelDTO getSalesFunnel(DateRange range, UUID userId);

    List<RevenueChartDTO> getRevenueChart(DateRange range, GroupBy groupBy);

    List<TopCustomerDTO> getTopCustomers(int limit);

    ConversionRateDTO getConversionRate(DateRange range);

    List<TeamPerformanceDTO> getTeamPerformance(DateRange range);

    ActivityMetricsDTO getActivityMetrics(UUID userId);

}