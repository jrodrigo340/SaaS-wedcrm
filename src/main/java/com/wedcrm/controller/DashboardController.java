package com.wedcrm.controller;

import com.wedcrm.dto.*;
import com.wedcrm.dto.Assistants.DateRangeDTO;
import com.wedcrm.dto.Assistants.SalesFunnelDTO;
import com.wedcrm.dto.Assistants.TopCustomerDTO;
import com.wedcrm.dto.dashboard.ConversionRateDTO;
import com.wedcrm.dto.dashboard.DashboardSummaryDTO;
import com.wedcrm.dto.dashboard.RevenueChartDTO;
import com.wedcrm.dto.dashboard.TeamPerformanceDTO;
import com.wedcrm.enums.GroupByType;
import com.wedcrm.service.DashboardService;
import com.wedcrm.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/dashboard")
@Tag(name = "Dashboard", description = "Endpoints para KPIs e métricas do sistema")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SALES', 'VIEWER')")
    @Operation(summary = "Resumo do dashboard", description = "Retorna cards com KPIs principais")
    public ResponseEntity<ApiResponse<DashboardSummaryDTO>> getDashboardSummary(
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = getUserIdFromUserDetails(userDetails);
        DashboardSummaryDTO summary = dashboardService.getDashboardSummary(userId);
        return ResponseEntity.ok(ApiResponse.success(summary));
    }

    @GetMapping("/funnel")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SALES', 'VIEWER')")
    @Operation(summary = "Funil de vendas", description = "Retorna dados do funil com volume e conversão por estágio")
    public ResponseEntity<ApiResponse<SalesFunnelDTO>> getSalesFunnel(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @AuthenticationPrincipal UserDetails userDetails) {
        DateRangeDTO dateRange = new DateRangeDTO(startDate, endDate);
        UUID userId = getUserIdFromUserDetails(userDetails);
        SalesFunnelDTO funnel = dashboardService.getSalesFunnel(dateRange, userId);
        return ResponseEntity.ok(ApiResponse.success(funnel));
    }

    @GetMapping("/revenue")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Gráfico de receita", description = "Retorna receita por período (diário, semanal, mensal)")
    public ResponseEntity<ApiResponse<RevenueChartDTO>> getRevenueChart(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "MONTHLY") GroupByType groupBy) {
        DateRangeDTO dateRange = new DateRangeDTO(startDate, endDate);
        RevenueChartDTO chart = dashboardService.getRevenueChart(dateRange, groupBy);
        return ResponseEntity.ok(ApiResponse.success(chart));
    }

    @GetMapping("/top-customers")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Top clientes", description = "Retorna os clientes com maior valor de oportunidades ganhas")
    public ResponseEntity<ApiResponse<List<TopCustomerDTO>>> getTopCustomers(
            @RequestParam(defaultValue = "10") int limit) {
        List<TopCustomerDTO> topCustomers = dashboardService.getTopCustomers(limit);
        return ResponseEntity.ok(ApiResponse.success(topCustomers));
    }

    @GetMapping("/conversion-rate")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Taxa de conversão", description = "Retorna taxa de conversão de leads em clientes")
    public ResponseEntity<ApiResponse<ConversionRateDTO>> getConversionRate(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        DateRangeDTO dateRange = new DateRangeDTO(startDate, endDate);
        ConversionRateDTO conversionRate = dashboardService.getConversionRate(dateRange);
        return ResponseEntity.ok(ApiResponse.success(conversionRate));
    }

    @GetMapping("/team-performance")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Performance da equipe", description = "Retorna métricas de desempenho por vendedor")
    public ResponseEntity<ApiResponse<List<TeamPerformanceDTO>>> getTeamPerformance(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        DateRangeDTO dateRange = new DateRangeDTO(startDate, endDate);
        List<TeamPerformanceDTO> performance = dashboardService.getTeamPerformance(dateRange);
        return ResponseEntity.ok(ApiResponse.success(performance));
    }

    // Método auxiliar para extrair o ID do usuário do token JWT
    private UUID getUserIdFromUserDetails(UserDetails userDetails) {
        // Ajuste conforme sua implementação (ex: userDetails.getUsername() retorna o ID)
        return UUID.fromString(userDetails.getUsername());
    }
}