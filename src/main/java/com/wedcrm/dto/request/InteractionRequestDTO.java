package com.wedcrm.dto.request;

import com.wedcrm.enums.InteractionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.UUID;

@Builder
public record InteractionRequestDTO(
        @NotNull(message = "Cliente é obrigatório")
        UUID customerId,
        @NotNull(message = "Tipo é obrigatório")
        InteractionType type,
        @NotBlank(message = "Conteúdo é obrigatório")
        String content,
        String subject,
        String channel
) {}