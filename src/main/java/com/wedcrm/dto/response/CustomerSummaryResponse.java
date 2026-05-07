package com.wedcrm.dto.response;

public record CustomerSummaryResponse(

        UUID id,
        String name,
        String company,
        String status,
        String ownerName

) {}