package com.wedcrm.dto.request;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record CloseRequestDTO(
        BigDecimal finalValue,
        String notes
) {}
