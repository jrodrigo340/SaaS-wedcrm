package com.wedcrm.mapper;

import com.wedcrm.dto.request.UserRequestDTO;
import com.wedcrm.dto.response.UserResponseDTO;
import com.wedcrm.dto.dashboard.UserSummaryDTO;
import com.wedcrm.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponseDTO toResponseDTO(User user);
    UserSummaryDTO toSummaryDTO(User user);
    User toEntity(UserRequestDTO dto);
    void updateEntity(UserRequestDTO dto, @MappingTarget User user);
}