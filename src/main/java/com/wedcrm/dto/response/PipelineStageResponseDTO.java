package com.wedcrm.dto.response;

import lombok.Builder;
import java.util.UUID;

@Builder
public record PipelineStageResponseDTO(
        UUID id,
        String name,
        String description,
        Integer order,
        String color,
        Integer probability,
        Boolean isWon,
        Boolean isLost,
        Long opportunitiesCount
) {}
