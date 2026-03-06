package com.wedcrm.dto.request;

import com.wedcrm.enums.UserRole;
import lombok.Setter;
import lombok.Getter;

@Getter
@Setter
public class UserRequestDTO {
    private String name;
    private String email;
    private String password;
    private UserRole role;
}
