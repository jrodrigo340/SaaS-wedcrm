package com.wedcrm.dto.filter;

import com.wedcrm.enums.ActivityStatus;
import com.wedcrm.enums.ActivityType;
import com.wedcrm.enums.Priority;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record ActivityFilterDTO(
        UUID assignedToId,
        UUID customerId,
        UUID opportunityId,
        ActivityStatus status,
        ActivityType type,
        Priority priority,
        LocalDateTime dueDateFrom,
        LocalDateTime dueDateTo,
        String searchTerm,
        Boolean onlyOverdue,
        Boolean onlyDueToday
) {}