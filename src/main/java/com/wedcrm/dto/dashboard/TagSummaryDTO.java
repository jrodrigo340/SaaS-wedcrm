package com.wedcrm.dto.dashboard;

import java.util.UUID;

public record TagSummaryDTO(
        UUID id,
        String name,
        String color,
        String validColor,
        Integer customerCount
) {}