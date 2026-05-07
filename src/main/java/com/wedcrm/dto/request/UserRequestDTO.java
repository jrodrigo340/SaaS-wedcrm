package com.wedcrm.dto.request;

import com.wedcrm.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record UserRequestDTO(
        @NotBlank String name,
        @NotBlank @Email String email,
        String password,
        @NotNull Role role,
        String phone
) {}
