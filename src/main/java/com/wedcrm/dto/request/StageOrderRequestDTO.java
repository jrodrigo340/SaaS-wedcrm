package com.wedcrm.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import java.util.UUID;

@Builder
public record StageOrderRequestDTO(
        @NotNull UUID stageId,
        @NotNull Integer newOrder
) {}