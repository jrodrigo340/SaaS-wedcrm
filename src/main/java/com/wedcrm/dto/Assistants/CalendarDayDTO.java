package com.wedcrm.dto.Assistants;

import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

@Builder
public record CalendarDayDTO(
        LocalDate date,
        int dayOfMonth,
        int dayOfWeek,
        List<CalendarDayActivityDTO> activities,
        boolean isEmpty
) {}