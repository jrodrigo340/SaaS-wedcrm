package com.wedcrm.dto.request;

import com.wedcrm.enums.InteractionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import java.util.UUID;

@Builder
public record InteractionRequestDTO(
        @NotNull UUID customerId,
        @NotNull InteractionType type,
        @NotBlank String content,
        String subject,
        String channel
) {}