package com.wedcrm.dto.response;

import com.wedcrm.enums.AutomationTrigger;
import com.wedcrm.enums.MessageChannel;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Builder
public record AutomationRuleResponseDTO(
        UUID id,
        String name,
        String description,
        AutomationTrigger trigger,
        String triggerDescription,
        String triggerIcon,
        Map<String, Object> conditions,
        UUID templateId,
        String templateName,
        Integer delayMinutes,
        Boolean isActive,
        MessageChannel channel,
        LocalDateTime lastExecutedAt,
        Long executionCount,
        LocalDateTime createdAt,
        String createdBy
) {}

