package com.wedcrm.dto.Assistants;

import com.wedcrm.enums.Priority;
import lombok.Builder;

@Builder
public record PriorityMetricDTO(
        Priority priority,
        String priorityColor,
        long total,
        long completed,
        long overdue,
        double completionRate
) {}
