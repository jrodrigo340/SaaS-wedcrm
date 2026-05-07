package com.wedcrm.dto.request;

import com.wedcrm.enums.CustomerStatus;

import java.time.LocalDate;
import java.util.UUID;

public record CustomerFilterDTO(

        String search,
        CustomerStatus status,
        UUID assignedToId,
        UUID tagId,

        Integer minScore,
        Integer maxScore,

        LocalDate createdFrom,
        LocalDate createdTo,

        LocalDate lastContactFrom,
        LocalDate lastContactTo,

        Boolean hasOpportunity,
        Boolean inactive

) {}