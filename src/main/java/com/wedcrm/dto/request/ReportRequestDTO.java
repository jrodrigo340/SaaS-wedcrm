package com.wedcrm.dto.request;

import com.wedcrm.enums.ReportType;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record ReportRequestDTO(
        @NotNull ReportType type,
        LocalDateTime startDate,
        LocalDateTime endDate,
        String format, // "PDF" ou "XLSX"
        UUID userId
) {}