package com.wedcrm.dto.response;

import com.wedcrm.enums.Direction;
import com.wedcrm.enums.InteractionStatus;
import com.wedcrm.enums.InteractionType;
import lombok.Builder;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record InteractionResponseDTO(
        UUID id,
        UUID customerId,
        String customerName,
        UUID userId,
        String userName,
        InteractionType type,
        String typeIcon,
        Direction direction,
        String subject,
        String content,
        String contentSummary,
        String channel,
        LocalDateTime sentAt,
        LocalDateTime readAt,
        InteractionStatus status,
        String statusColor,
        Boolean isAutomatic,
        UUID templateId,
        String timeAgo
) {}