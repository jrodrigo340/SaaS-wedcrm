package com.wedcrm.dto.Assistants;

import lombok.Builder;
import java.time.LocalDateTime;

@Builder
public record TimelineEventDTO(
        LocalDateTime date,
        String type,
        String title,
        String description,
        String icon,
        String status
) {}
