package com.wedcrm.dto.response;

import com.wedcrm.enums.MessageChannel;
import com.wedcrm.enums.TemplateCategory;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Builder
public record MessageTemplateResponseDTO(
        UUID id,
        String name,
        String description,
        MessageChannel channel,
        TemplateCategory category,
        String subject,
        String body,
        List<String> variables,
        Boolean isActive,
        String language,
        Integer usageCount,
        LocalDateTime createdAt,
        String createdBy,
        String channelIcon,
        String categoryColor
) {}
