package com.wedcrm.dto.response;

import com.wedcrm.enums.LeadSource;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class CustomerResponseDTO {
    private UUID id;
    private String name;
    private String email;
    private LeadSource source;
    private String notes;
}
