package com.wedcrm.dto.Assistants;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Builder
public record KanbanColumnDTO(
        UUID id,
        String name,
        String color,
        List<KanbanCardDTO> cards,
        Long totalCards,
        BigDecimal totalValue
) {}