package com.wedcrm.dto.response;

import com.wedcrm.enums.ReportType;
import lombok.Builder;
import java.time.LocalDateTime;

@Builder
public record ReportResponseDTO(
        String title,
        ReportType type,
        LocalDateTime generatedAt,
        String generatedBy,
        String fileUrl,
        byte[] content
) {}
