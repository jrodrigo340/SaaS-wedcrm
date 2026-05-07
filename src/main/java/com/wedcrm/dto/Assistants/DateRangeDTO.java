package com.wedcrm.dto.Assistants;

import java.time.LocalDateTime;

public record DateRangeDTO(
        LocalDateTime startDate,
        LocalDateTime endDate
) {
    public DateRangeDTO {
        if (endDate == null) {
            endDate = LocalDateTime.now();
        }
        if (startDate == null) {
            startDate = endDate.minusDays(30);
        }
    }
}
