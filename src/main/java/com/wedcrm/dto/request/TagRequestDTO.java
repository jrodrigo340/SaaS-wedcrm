package com.wedcrm.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record TagRequestDTO(
        @NotBlank @Size(max = 50) String name,
        String color,
        String description
) {}