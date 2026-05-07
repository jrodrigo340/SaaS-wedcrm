package com.wedcrm.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record PipelineStageRequestDTO(
        @NotBlank String name,
        String description,
        @NotNull @Min(1) Integer order,
        String color,
        @Min(0) @Max(100) Integer probability,
        Boolean isWon,
        Boolean isLost
) {}
