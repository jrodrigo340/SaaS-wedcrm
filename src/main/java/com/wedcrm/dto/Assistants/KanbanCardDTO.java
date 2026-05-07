package com.wedcrm.dto.Assistants;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Builder
public record KanbanCardDTO(
        UUID id,
        String title,
        String customerName,
        BigDecimal value,
        String formattedValue,
        Integer probability,
        LocalDate expectedCloseDate,
        Boolean isOverdue
) {}
