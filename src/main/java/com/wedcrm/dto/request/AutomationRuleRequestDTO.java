package com.wedcrm.dto.request;

import com.wedcrm.enums.AutomationTrigger;
import com.wedcrm.enums.MessageChannel;
import lombok.Builder;
import java.util.Map;
import java.util.UUID;

@Builder
public record AutomationRuleRequestDTO(
        String name,
        String description,
        AutomationTrigger trigger,
        Map<String, Object> conditions,
        UUID templateId,
        Integer delayMinutes,
        MessageChannel channel,
        Boolean isActive
) {}

