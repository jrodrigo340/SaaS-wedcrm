package com.wedcrm.dto.response;

import com.wedcrm.enums.ActivityStatus;
import com.wedcrm.enums.ActivityType;
import com.wedcrm.enums.Priority;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record ActivityResponseDTO(
        UUID id,
        String title,
        String description,
        ActivityType type,
        String typeIcon,
        ActivityStatus status,
        Priority priority,
        String priorityColor,
        LocalDateTime dueDate,
        String formattedDueDate,
        LocalDateTime completedAt,
        UUID customerId,
        String customerName,
        UUID opportunityId,
        String opportunityTitle,
        UUID assignedToId,
        String assignedToName,
        LocalDateTime reminderAt,
        Boolean reminderSent,
        Boolean isOverdue,
        Boolean isEditable,
        LocalDateTime createdAt,
        String createdBy
) {}