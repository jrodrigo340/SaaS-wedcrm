package com.wedcrm.dto.update;

public record CustomerUpdateRequest(

        String name,
        String phone,
        String company

) {}