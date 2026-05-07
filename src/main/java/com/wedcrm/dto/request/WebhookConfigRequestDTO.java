package com.wedcrm.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record WebhookConfigRequestDTO(
        @NotBlank(message = "URL é obrigatória")
        String url,
        @NotBlank(message = "Event type é obrigatório")
        String eventType,
        @NotNull(message = "Active é obrigatório")
        Boolean active
) {}