package com.wedcrm.dto.response;

import com.wedcrm.enums.NotificationType;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record NotificationResponseDTO(
        UUID id,
        UUID userId,
        String userName,
        String title,
        String message,
        NotificationType type,
        String icon,
        String typeColor,
        Boolean isRead,
        LocalDateTime readAt,
        LocalDateTime createdAt,
        String timeAgo,
        UUID referenceId,
        String referenceType
) {}