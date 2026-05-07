package com.wedcrm.dto.Assistants;

import lombok.Builder;
import java.util.UUID;

@Builder
public record WebhookConfigDTO(
        UUID id,
        String url,
        String eventType,
        Boolean active
) {}
