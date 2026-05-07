package com.wedcrm.dto.request;

import com.wedcrm.dto.AddressDTO;
import com.wedcrm.enums.CustomerStatus;
import com.wedcrm.enums.LeadSource;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record CustomerRequestDTO(

        @NotBlank String firstName,

        @NotBlank String lastName,

        @Email
        @NotBlank String email,

        String phone,

        String whatsapp,

        String company,

        String position,

        CustomerStatus status,

        LeadSource source,

        UUID assignedToId,

        List<UUID> tagIds,

        @ValidAddress
        AddressDTO address,

        @Size(max = 5000)
        String notes,

        LocalDate birthday,

        Map<String, String> customFields

) {}