package com.wedcrm.dto.response;

@Builder
public record OpportunityResponseDTO(
        UUID id,
        String title,
        UUID customerId,
        String customerName,
        UUID stageId,
        String stageName,
        String stageColor,
        UUID assignedToId,
        String assignedToName,
        BigDecimal value,
        String formattedValue,
        Integer probability,
        LocalDate expectedCloseDate,
        LocalDateTime closedAt,
        OpportunityStatus status,
        String lostReason,
        String notes,
        List<OpportunityProductResponseDTO> products,
        Integer totalProducts,
        Boolean isOverdue,
        Long daysUntilExpectedClose,
        LocalDateTime createdAt,
        String createdBy
) {}