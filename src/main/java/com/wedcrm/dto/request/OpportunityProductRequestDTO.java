package com.wedcrm.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record OpportunityProductRequestDTO(
        @NotNull UUID productId,
        @NotNull @Min(1) Integer quantity,
        BigDecimal unitPrice, // Se não informado, usa o preço atual do produto
        @DecimalMin("0.0") @DecimalMax("100.0") BigDecimal discount,
        String notes
) {}