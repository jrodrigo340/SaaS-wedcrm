package com.wedcrm.dto.filter;

import com.wedcrm.enums.AutomationTrigger;
import com.wedcrm.enums.MessageChannel;
import lombok.Builder;

import java.util.UUID;

@Builder
public record AutomationRuleFilterDTO(
        AutomationTrigger trigger,
        MessageChannel channel,
        Boolean isActive,
        UUID templateId,
        Integer minDelay,
        Integer maxDelay,
        Long minExecutions,
        String nameSearch,
        Boolean onlyNeverExecuted,
        Boolean onlyBirthdayRules,
        Boolean onlyInactivityRules
) {}
