package com.wedcrm.dto.request;

import com.wedcrm.enums.LeadSource;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerRequestDTO {

    private String name;
    private String email;
    private LeadSource source;
    private String notes;
}
