package com.wedcrm.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record ProductRequestDTO(
        @NotBlank @Size(max = 200) String name,
        @Size(max = 2000) String description,
        @Size(max = 50) String sku,
        @NotNull @DecimalMin("0.0") BigDecimal price,
        @Size(max = 30) String unit,
        @Size(max = 100) String category,
        Boolean isActive
) {}

