package com.wedcrm.dto.filter;

import com.wedcrm.enums.OpportunityStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Builder
public class OpportunityFilterDTO {

    private UUID customerId;
    private UUID stageId;
    private UUID assignedToId;
    private OpportunityStatus status;
    private BigDecimal minValue;
    private BigDecimal maxValue;
    private Integer minProbability;
    private LocalDate expectedCloseDateFrom;
    private LocalDate expectedCloseDateTo;
    private String searchTerm;
    private Boolean onlyOverdue;
}