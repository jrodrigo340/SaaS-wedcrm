package com.wedcrm.dto.Assistants;

import com.wedcrm.enums.ActivityStatus;
import com.wedcrm.enums.ActivityType;
import com.wedcrm.enums.Priority;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record ActivityDTO(
        UUID id,
        String title,
        String description,
        ActivityType type,
        ActivityStatus status,
        Priority priority,
        LocalDateTime dueDate,
        LocalDateTime completedAt,
        UUID customerId,
        String customerName,
        UUID assignedToId,
        String assignedToName,
        LocalDateTime reminderAt,
        Boolean reminderSent,
        Boolean isOverdue,
        LocalDateTime createdAt
) {}
