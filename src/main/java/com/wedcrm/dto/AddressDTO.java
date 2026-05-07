package com.wedcrm.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AddressDTO(
        @Size(max = 200) String street,
        @Size(max = 20) String number,
        @Size(max = 100) String complement,
        @Size(max = 100) String neighborhood,
        @Size(max = 100) String city,
        @Size(min = 2, max = 2) @Pattern(regexp = "^[A-Z]{2}$", message = "Estado deve ser sigla de 2 letras maiúsculas")
        String state,
        @Pattern(regexp = "^\\d{5}-\\d{3}$", message = "CEP deve estar no formato 00000-000")
        String zipCode,
        @Size(max = 50) String country
) {
    public AddressDTO {
        if (country == null || country.isEmpty()) {
            country = "Brasil";
        }
    }
}