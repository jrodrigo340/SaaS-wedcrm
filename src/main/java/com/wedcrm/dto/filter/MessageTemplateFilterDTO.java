package com.wedcrm.dto.filter;

import com.wedcrm.enums.MessageChannel;
import com.wedcrm.enums.TemplateCategory;
import lombok.Builder;

@Builder
public record MessageTemplateFilterDTO(
        MessageChannel channel,
        TemplateCategory category,
        Boolean isActive,
        String language,
        Integer minUsageCount,
        Integer maxUsageCount,
        String nameSearch,
        String descriptionSearch,
        Boolean onlyUnused
) {}
