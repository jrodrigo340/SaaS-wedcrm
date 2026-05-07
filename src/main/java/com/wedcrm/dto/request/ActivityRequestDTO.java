package com.wedcrm.dto.request;

import com.wedcrm.enums.ActivityType;
import com.wedcrm.enums.Priority;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record ActivityRequestDTO(
        @NotBlank(message = "Título é obrigatório")
        String title,

        String description,

        @NotNull(message = "Tipo é obrigatório")
        ActivityType type,

        @NotNull(message = "Prioridade é obrigatória")
        Priority priority,

        @NotNull(message = "Data de vencimento é obrigatória")
        @Future(message = "Data de vencimento deve ser no futuro")
        LocalDateTime dueDate,

        UUID customerId,

        UUID opportunityId,

        UUID assignedToId,

        LocalDateTime reminderAt
) {}