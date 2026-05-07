package com.wedcrm.dto.request;

import lombok.Builder;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Builder
public record OpportunityRequestDTO(
        String title,
        UUID customerId,
        UUID stageId,
        UUID assignedToId,
        BigDecimal value,
        Integer probability,
        LocalDate expectedCloseDate,
        String notes,
        List<OpportunityProductRequestDTO> products
) {}