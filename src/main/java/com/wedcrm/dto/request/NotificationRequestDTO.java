package com.wedcrm.dto.request;

import com.wedcrm.enums.NotificationType;
import lombok.Builder;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Builder
public record NotificationRequestDTO(
        UUID userId,
        String title,
        String message,
        NotificationType type,
        UUID referenceId,
        String referenceType
) {}