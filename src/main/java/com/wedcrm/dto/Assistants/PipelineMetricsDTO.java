package com.wedcrm.dto.Assistants;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record PipelineMetricsDTO(
        BigDecimal totalOpenValue,
        Long openOpportunitiesCount,
        Double conversionRate,
        BigDecimal averageWonValue,
        Long averageClosingDays,
        BigDecimal wonValueInPeriod,
        List<PipelineStageMetricDTO> pipelineByStage,
        List<SellerPerformanceDTO> sellerPerformance
) {}
