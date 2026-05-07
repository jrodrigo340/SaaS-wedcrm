package com.wedcrm.mapper;

import com.wedcrm.dto.request.CustomerRequestDTO;
import com.wedcrm.dto.response.CustomerResponse;
import com.wedcrm.dto.response.CustomerSummaryResponse;
import com.wedcrm.entity.Customer;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(
        componentModel = "spring",
        uses = {UserMapper.class, TagMapper.class}
)
public interface CustomerMapper {

    // ENTITY → RESPONSE
    @Mapping(source = "assignedTo.id", target = "assignedToId")
    CustomerResponse toResponse(Customer customer);

    // ENTITY → SUMMARY
    @Mapping(source = "assignedTo.id", target = "assignedToId")
    CustomerSummaryResponse toSummary(Customer customer);

    // REQUEST → ENTITY
    Customer toEntity(CustomerRequestDTO request);

    // UPDATE
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(CustomerRequestDTO request, @MappingTarget Customer customer);
}