package com.wedcrm.service.impl;

import com.wedcrm.dto.*;
import com.wedcrm.entity.*;
import com.wedcrm.enums.ActivityStatus;
import com.wedcrm.enums.CustomerStatus;
import com.wedcrm.enums.OpportunityStatus;
import com.wedcrm.repository.*;
import com.wedcrm.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private OpportunityRepository opportunityRepository;

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private InteractionRepository interactionRepository;

    private final DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("dd/MM");
    private final DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMM/yy");

    // ========== DASHBOARD SUMMARY ==========

    @Override
    public DashboardSummaryDTO getDashboardSummary(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        DashboardSummaryDTO summary = new DashboardSummaryDTO();

        // Total de clientes
        if (user.isAdmin() || user.isManager()) {
            summary.setTotalCustomers(customerRepository.countActiveCustomers());
            summary.setTotalLeads(customerRepository.countByStatus(CustomerStatus.LEAD));
            summary.setTotalProspects(customerRepository.countByStatus(CustomerStatus.PROSPECT));
            summary.setTotalCustomersWon(customerRepository.countByStatus(CustomerStatus.CUSTOMER));
        } else {
            List<Customer> userCustomers = customerRepository.findByAssignedTo(user);
            summary.setTotalCustomers((long) userCustomers.size());
            summary.setTotalLeads((long) userCustomers.stream()
                    .filter(c -> c.getStatus() == CustomerStatus.LEAD).count());
            summary.setTotalProspects((long) userCustomers.stream()
                    .filter(c -> c.getStatus() == CustomerStatus.PROSPECT).count());
            summary.setTotalCustomersWon((long) userCustomers.stream()
                    .filter(c -> c.getStatus() == CustomerStatus.CUSTOMER).count());
        }

        // Oportunidades abertas
        BigDecimal totalPipelineValue;
        long openOpportunitiesCount;

        if (user.isAdmin() || user.isManager()) {
            totalPipelineValue = opportunityRepository.getTotalPipelineValue();
            openOpportunitiesCount = opportunityRepository.countOpenOpportunities();
        } else {
            List<Opportunity> userOpportunities = opportunityRepository.findByAssignedTo(user);
            openOpportunitiesCount = userOpportunities.stream()
                    .filter(o -> o.getStatus() == OpportunityStatus.OPEN).count();
            totalPipelineValue = userOpportunities.stream()
                    .filter(o -> o.getStatus() == OpportunityStatus.OPEN)
                    .map(Opportunity::getValue)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        summary.setOpenOpportunitiesCount(openOpportunitiesCount);
        summary.setTotalPipelineValue(totalPipelineValue);
        summary.setFormattedPipelineValue(formatCurrency(totalPipelineValue));

        // Atividades pendentes
        long pendingActivities;
        long overdueActivities;

        if (user.isAdmin() || user.isManager()) {
            pendingActivities = activityRepository.countByStatus(ActivityStatus.PENDING);
            overdueActivities = activityRepository.findOverdueActivities(LocalDateTime.now()).size();
        } else {
            pendingActivities = activityRepository.countByAssignedToAndStatus(user, ActivityStatus.PENDING);
            overdueActivities = activityRepository.findOverdueActivitiesForUser(user, LocalDateTime.now()).size();
        }

        summary.setPendingActivities(pendingActivities);
        summary.setOverdueActivities(overdueActivities);

        // Receita prevista (valor médio de oportunidades em pipeline)
        if (openOpportunitiesCount > 0) {
            BigDecimal avgPipelineValue = totalPipelineValue.divide(
                    BigDecimal.valueOf(openOpportunitiesCount), 2, RoundingMode.HALF_UP);
            summary.setAverageDealValue(avgPipelineValue);
            summary.setFormattedAverageDealValue(formatCurrency(avgPipelineValue));
        }

        // Taxa de conversão geral
        long totalClosed = opportunityRepository.countByStatus(OpportunityStatus.WON) +
                opportunityRepository.countByStatus(OpportunityStatus.LOST);
        long wonCount = opportunityRepository.countByStatus(OpportunityStatus.WON);
        double conversionRate = totalClosed > 0 ? (wonCount * 100.0 / totalClosed) : 0;
        summary.setConversionRate(Math.round(conversionRate * 10) / 10.0);

        // Crescimento mensal (últimos 30 dias)
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        long newCustomersLastMonth = customerRepository.countByCreatedAtAfter(thirtyDaysAgo);
        summary.setNewCustomersLastMonth(newCustomersLastMonth);

        return summary;
    }

    // ========== SALES FUNNEL ==========

    @Override
    public SalesFunnelDTO getSalesFunnel(DateRangeDTO dateRange, UUID userId) {
        User user = userId != null ? userRepository.findById(userId).orElse(null) : null;
        LocalDateTime startDate = dateRange.getStartDate();
        LocalDateTime endDate = dateRange.getEndDate();

        List<PipelineStage> stages = stageRepository.findAllByOrderByOrderAsc();
        List<SalesFunnelStageDTO> funnelStages = new ArrayList<>();

        long totalEntered = 0;
        long totalWon = 0;

        for (PipelineStage stage : stages) {
            List<Opportunity> opportunities;

            if (user != null && !user.isAdmin() && !user.isManager()) {
                opportunities = opportunityRepository.findByStageAndAssignedTo(stage, user);
            } else {
                opportunities = opportunityRepository.findByStage(stage);
            }

            // Filtra por data de criação ou movimentação
            long count = opportunities.stream()
                    .filter(o -> o.getCreatedAt().isAfter(startDate) && o.getCreatedAt().isBefore(endDate))
                    .count();

            long wonFromStage = opportunities.stream()
                    .filter(o -> o.getStatus() == OpportunityStatus.WON)
                    .filter(o -> o.getClosedAt() != null && o.getClosedAt().isAfter(startDate) && o.getClosedAt().isBefore(endDate))
                    .count();

            totalEntered += count;
            totalWon += wonFromStage;

            double conversionRate = count > 0 ? (wonFromStage * 100.0 / count) : 0;

            funnelStages.add(SalesFunnelStageDTO.builder()
                    .stageId(stage.getId())
                    .stageName(stage.getName())
                    .stageOrder(stage.getOrder())
                    .color(stage.getColor())
                    .enteredCount(count)
                    .wonCount(wonFromStage)
                    .conversionRate(Math.round(conversionRate * 10) / 10.0)
                    .build());
        }

        double overallConversionRate = totalEntered > 0 ? (totalWon * 100.0 / totalEntered) : 0;

        return SalesFunnelDTO.builder()
                .stages(funnelStages)
                .totalEntered(totalEntered)
                .totalWon(totalWon)
                .overallConversionRate(Math.round(overallConversionRate * 10) / 10.0)
                .build();
    }

    // ========== REVENUE CHART ==========

    @Override
    public RevenueChartDTO getRevenueChart(DateRangeDTO dateRange, GroupByType groupBy) {
        LocalDateTime startDate = dateRange.getStartDate();
        LocalDateTime endDate = dateRange.getEndDate();

        List<RevenueChartDataDTO> data = new ArrayList<>();

        switch (groupBy) {
            case DAILY -> {
                long days = ChronoUnit.DAYS.between(startDate.toLocalDate(), endDate.toLocalDate());
                for (int i = 0; i <= days; i++) {
                    LocalDate currentDate = startDate.toLocalDate().plusDays(i);
                    LocalDateTime dayStart = currentDate.atStartOfDay();
                    LocalDateTime dayEnd = currentDate.atTime(23, 59, 59);

                    BigDecimal revenue = getRevenueForPeriod(dayStart, dayEnd);
                    data.add(RevenueChartDataDTO.builder()
                            .label(currentDate.format(dayFormatter))
                            .date(currentDate)
                            .revenue(revenue)
                            .formattedRevenue(formatCurrency(revenue))
                            .build());
                }
            }
            case WEEKLY -> {
                LocalDate current = startDate.toLocalDate();
                while (!current.isAfter(endDate.toLocalDate())) {
                    LocalDate weekStart = current;
                    LocalDate weekEnd = current.plusDays(6);
                    if (weekEnd.isAfter(endDate.toLocalDate())) {
                        weekEnd = endDate.toLocalDate();
                    }

                    BigDecimal revenue = getRevenueForPeriod(
                            weekStart.atStartOfDay(),
                            weekEnd.atTime(23, 59, 59)
                    );

                    data.add(RevenueChartDataDTO.builder()
                            .label(String.format("%s - %s",
                                    weekStart.format(dayFormatter),
                                    weekEnd.format(dayFormatter)))
                            .date(weekStart)
                            .revenue(revenue)
                            .formattedRevenue(formatCurrency(revenue))
                            .build());

                    current = current.plusWeeks(1);
                }
            }
            case MONTHLY -> {
                LocalDate current = startDate.toLocalDate().withDayOfMonth(1);
                while (!current.isAfter(endDate.toLocalDate())) {
                    LocalDate monthStart = current;
                    LocalDate monthEnd = current.withDayOfMonth(current.lengthOfMonth());
                    if (monthEnd.isAfter(endDate.toLocalDate())) {
                        monthEnd = endDate.toLocalDate();
                    }

                    BigDecimal revenue = getRevenueForPeriod(
                            monthStart.atStartOfDay(),
                            monthEnd.atTime(23, 59, 59)
                    );

                    data.add(RevenueChartDataDTO.builder()
                            .label(monthStart.format(monthFormatter))
                            .date(monthStart)
                            .revenue(revenue)
                            .formattedRevenue(formatCurrency(revenue))
                            .build());

                    current = current.plusMonths(1);
                }
            }
        }

        // Calcula totais
        BigDecimal totalRevenue = data.stream()
                .map(RevenueChartDataDTO::revenue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Optional<BigDecimal> maxRevenue = data.stream()
                .map(RevenueChartDataDTO::revenue)
                .max(BigDecimal::compareTo);

        return RevenueChartDTO.builder()
                .data(data)
                .totalRevenue(totalRevenue)
                .formattedTotalRevenue(formatCurrency(totalRevenue))
                .maxRevenue(maxRevenue.orElse(BigDecimal.ZERO))
                .groupBy(groupBy)
                .build();
    }

    // ========== TOP CUSTOMERS ==========

    @Override
    public List<TopCustomerDTO> getTopCustomers(int limit) {
        List<Object[]> results = opportunityRepository.getTopCustomersByValue();

        return results.stream()
                .limit(limit)
                .map(row -> {
                    Customer customer = (Customer) row[0];
                    Long totalDeals = ((Number) row[1]).longValue();
                    BigDecimal totalValue = (BigDecimal) row[2];

                    return TopCustomerDTO.builder()
                            .customerId(customer.getId())
                            .customerName(customer.getFullName())
                            .customerEmail(customer.getEmail())
                            .customerPhone(customer.getPhone())
                            .company(customer.getCompany())
                            .totalDeals(totalDeals)
                            .totalValue(totalValue)
                            .formattedTotalValue(formatCurrency(totalValue))
                            .assignedTo(customer.getAssignedTo() != null ? customer.getAssignedTo().getName() : null)
                            .build();
                })
                .collect(Collectors.toList());
    }

    // ========== CONVERSION RATE ==========

    @Override
    public ConversionRateDTO getConversionRate(DateRangeDTO dateRange) {
        LocalDateTime startDate = dateRange.getStartDate();
        LocalDateTime endDate = dateRange.getEndDate();

        // Novos leads no período
        long newLeads = customerRepository.countByStatusAndCreatedAtBetween(
                CustomerStatus.LEAD, startDate, endDate);

        // Leads que se tornaram clientes no período
        long convertedToCustomer = customerRepository.countByStatusChangedTo(
                CustomerStatus.CUSTOMER, startDate, endDate);

        // Oportunidades ganhas no período
        long wonOpportunities = opportunityRepository.countByStatusAndClosedAtBetween(
                OpportunityStatus.WON, startDate, endDate);

        // Oportunidades perdidas no período
        long lostOpportunities = opportunityRepository.countByStatusAndClosedAtBetween(
                OpportunityStatus.LOST, startDate, endDate);

        // Cálculo das taxas
        double leadToCustomerRate = newLeads > 0 ? (convertedToCustomer * 100.0 / newLeads) : 0;
        double opportunityWinRate = (wonOpportunities + lostOpportunities) > 0 ?
                (wonOpportunities * 100.0 / (wonOpportunities + lostOpportunities)) : 0;

        return ConversionRateDTO.builder()
                .newLeads(newLeads)
                .convertedToCustomer(convertedToCustomer)
                .leadToCustomerRate(Math.round(leadToCustomerRate * 10) / 10.0)
                .wonOpportunities(wonOpportunities)
                .lostOpportunities(lostOpportunities)
                .opportunityWinRate(Math.round(opportunityWinRate * 10) / 10.0)
                .dateRangeStart(startDate.toLocalDate())
                .dateRangeEnd(endDate.toLocalDate())
                .build();
    }

    // ========== TEAM PERFORMANCE ==========

    @Override
    public List<TeamPerformanceDTO> getTeamPerformance(DateRangeDTO dateRange) {
        LocalDateTime startDate = dateRange.getStartDate();
        LocalDateTime endDate = dateRange.getEndDate();

        List<User> sellers = userRepository.findAllByRoleAndActiveTrue(Role.SALES);
        List<TeamPerformanceDTO> performance = new ArrayList<>();

        for (User seller : sellers) {
            // Oportunidades
            List<Opportunity> opportunities = opportunityRepository.findByAssignedToAndClosedAtBetween(
                    seller, startDate, endDate);

            long totalDeals = opportunities.size();
            long wonDeals = opportunities.stream()
                    .filter(o -> o.getStatus() == OpportunityStatus.WON).count();
            long lostDeals = opportunities.stream()
                    .filter(o -> o.getStatus() == OpportunityStatus.LOST).count();

            BigDecimal totalRevenue = opportunities.stream()
                    .filter(o -> o.getStatus() == OpportunityStatus.WON)
                    .map(Opportunity::getValue)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            double conversionRate = totalDeals > 0 ? (wonDeals * 100.0 / totalDeals) : 0;

            // Atividades
            long completedActivities = activityRepository.countByAssignedToAndStatusAndCompletedAtBetween(
                    seller, ActivityStatus.DONE, startDate, endDate);

            long totalActivities = activityRepository.countByAssignedToAndCreatedAtBetween(
                    seller, startDate, endDate);

            // Novos clientes
            long newCustomers = customerRepository.countByAssignedToAndCreatedAtBetween(
                    seller, startDate, endDate);

            performance.add(TeamPerformanceDTO.builder()
                    .userId(seller.getId())
                    .userName(seller.getName())
                    .userAvatar(seller.getAvatarUrl())
                    .totalDeals(totalDeals)
                    .wonDeals(wonDeals)
                    .lostDeals(lostDeals)
                    .totalRevenue(totalRevenue)
                    .formattedTotalRevenue(formatCurrency(totalRevenue))
                    .conversionRate(Math.round(conversionRate * 10) / 10.0)
                    .completedActivities(completedActivities)
                    .totalActivities(totalActivities)
                    .activityCompletionRate(totalActivities > 0 ?
                            Math.round((completedActivities * 100.0 / totalActivities) * 10) / 10.0 : 0)
                    .newCustomers(newCustomers)
                    .build());
        }

        // Ordena por receita total (decrescente)
        performance.sort((a, b) -> b.totalRevenue().compareTo(a.totalRevenue()));

        return performance;
    }

    // ========== ACTIVITY METRICS ==========

    @Override
    public ActivityMetricsDTO getActivityMetrics(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        List<ActivityTypeMetricDTO> byType = new ArrayList<>();
        List<PriorityMetricDTO> byPriority = new ArrayList<>();

        // Métricas por tipo de atividade
        for (ActivityType type : ActivityType.values()) {
            long total = activityRepository.countByAssignedToAndType(user, type);
            long completed = activityRepository.countByAssignedToAndTypeAndStatus(
                    user, type, ActivityStatus.DONE);

            double completionRate = total > 0 ? (completed * 100.0 / total) : 0;

            byType.add(ActivityTypeMetricDTO.builder()
                    .type(type)
                    .typeIcon(type.getIcon())
                    .typeDescription(type.getDescription())
                    .total(total)
                    .completed(completed)
                    .completionRate(Math.round(completionRate * 10) / 10.0)
                    .build());
        }

        // Métricas por prioridade
        for (Priority priority : Priority.values()) {
            long total = activityRepository.countByAssignedToAndPriority(user, priority);
            long completed = activityRepository.countByAssignedToAndPriorityAndStatus(
                    user, priority, ActivityStatus.DONE);
            long overdue = activityRepository.countByAssignedToAndPriorityAndOverdue(
                    user, priority, LocalDateTime.now());

            byPriority.add(PriorityMetricDTO.builder()
                    .priority(priority)
                    .priorityColor(priority.getColor())
                    .total(total)
                    .completed(completed)
                    .overdue(overdue)
                    .completionRate(total > 0 ? Math.round((completed * 100.0 / total) * 10) / 10.0 : 0)
                    .build());
        }

        // Totais gerais
        long totalActivities = activityRepository.countByAssignedTo(user);
        long completedActivities = activityRepository.countByAssignedToAndStatus(user, ActivityStatus.DONE);
        long overdueActivities = activityRepository.countOverdueForUser(user, LocalDateTime.now());
        long pendingActivities = totalActivities - completedActivities;

        return ActivityMetricsDTO.builder()
                .totalActivities(totalActivities)
                .completedActivities(completedActivities)
                .pendingActivities(pendingActivities)
                .overdueActivities(overdueActivities)
                .overallCompletionRate(totalActivities > 0 ?
                        Math.round((completedActivities * 100.0 / totalActivities) * 10) / 10.0 : 0)
                .byType(byType)
                .byPriority(byPriority)
                .build();
    }

    // ========== MÉTODOS PRIVADOS ==========

    private BigDecimal getRevenueForPeriod(LocalDateTime start, LocalDateTime end) {
        return opportunityRepository.getWonValueInPeriod(start, end);
    }

    private String formatCurrency(BigDecimal value) {
        if (value == null) return "R$ 0,00";
        return String.format("R$ %,.2f", value);
    }
}