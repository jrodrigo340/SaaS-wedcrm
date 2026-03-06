package com.wedcrm.dto.response;

import com.wedcrm.enums.UserRole;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class UserResponseDTO {

    private UUID id;
    private String name;
    private String email;
    private UserRole role;
}
