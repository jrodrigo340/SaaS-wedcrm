package com.wedcrm.dto.filter;

import com.wedcrm.enums.Direction;
import com.wedcrm.enums.InteractionStatus;
import com.wedcrm.enums.InteractionType;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record InteractionFilterDTO(
        UUID customerId,
        UUID userId,
        InteractionType type,
        Direction direction,
        InteractionStatus status,
        Boolean isAutomatic,
        UUID templateId,
        String channel,
        LocalDateTime dateFrom,
        LocalDateTime dateTo,
        String searchTerm
) {}