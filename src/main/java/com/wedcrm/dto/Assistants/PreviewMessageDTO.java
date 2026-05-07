package com.wedcrm.dto.Assistants;

import lombok.Builder;

import java.util.Set;
import java.util.UUID;

@Builder
public record PreviewMessageDTO(
        UUID templateId,
        String templateName,
        String channel,
        String subject,
        String body,
        Set<String> usedVariables,
        String customerName,
        String customerEmail
) {}