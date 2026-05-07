package com.wedcrm.dto.Assistants;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record RuleExecutionHistoryDTO(
        UUID ruleId,
        String ruleName,
        UUID customerId,
        String customerName,
        LocalDateTime executedAt,
        boolean success,
        String errorMessage
) {}
