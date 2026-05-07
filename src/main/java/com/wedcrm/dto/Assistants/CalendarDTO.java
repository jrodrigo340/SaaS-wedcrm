package com.wedcrm.dto.Assistants;

import com.wedcrm.enums.ActivityStatus;
import com.wedcrm.enums.ActivityType;
import com.wedcrm.enums.Priority;
import lombok.Builder;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Builder
public record CalendarDTO(
        int year,
        int month,
        List<CalendarDayDTO> days
) {}

@Builder
public record CalendarDayDTO(
        LocalDate date,
        int dayOfMonth,
        int dayOfWeek,
        List<CalendarDayActivityDTO> activities,
        boolean isEmpty
) {}

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
