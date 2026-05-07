package com.wedcrm.dto.Assistants;

import com.wedcrm.enums.ActivityType;
import lombok.Builder;

@Builder
public record ActivityTypeMetricDTO(
        ActivityType type,
        String typeIcon,
        String typeDescription,
        long total,
        long completed,
        double completionRate
) {}
