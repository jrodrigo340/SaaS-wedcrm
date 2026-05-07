package com.wedcrm.dto.request;
public record AddressRequestDTO(

        String street,
        String number,
        String complement,
        String district,
        String city,
        String state,
        String zipCode,
        String country

) {}
