package com.wedcrm.dto.Assistants;

import lombok.Builder;

import java.util.List;

@Builder
public record SalesFunnelDTO(
        List<SalesFunnelStageDTO> stages,
        long totalEntered,
        long totalWon,
        double overallConversionRate
) {}