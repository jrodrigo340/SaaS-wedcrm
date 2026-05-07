package com.wedcrm.dto.Assistants;

import com.wedcrm.enums.ActivityStatus;
import com.wedcrm.enums.ActivityType;
import com.wedcrm.enums.Priority;
import lombok.Builder;

import java.util.UUID;

@Builder
public record CalendarDayActivityDTO(
        UUID id,
        String title,
        ActivityType type,
        String icon,
        Priority priority,
        String priorityColor,
        ActivityStatus status,
        String time
) {}
