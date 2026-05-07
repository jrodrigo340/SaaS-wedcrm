package com.wedcrm.dto.Assistants;

import lombok.Builder;

import java.util.UUID;

@Builder
public record SalesFunnelStageDTO(
        UUID stageId,
        String stageName,
        Integer stageOrder,
        String color,
        long enteredCount,
        long wonCount,
        double conversionRate
) {}
