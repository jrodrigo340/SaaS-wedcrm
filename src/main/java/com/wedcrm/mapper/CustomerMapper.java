package com.wedcrm.mapper;

import com.wedcrm.dto.request.CustomerRequestDTO;
import com.wedcrm.dto.response.CustomerResponseDTO;
import com.wedcrm.entity.Customer;
import org.springframework.stereotype.Component;

@Component
public class CustomerMapper {

    public Customer toEntity(CustomerRequestDTO dto) {
        Customer customer = new Customer();

        customer.setName(dto.getName());
        customer.setEmail(dto.getEmail());
        customer.setSource(dto.getSource());
        customer.setNotes(dto.getNotes());

        return customer;
    }

    public CustomerResponseDTO toDto(Customer customer) {
        CustomerResponseDTO dto = new CustomerResponseDTO();

        dto.setId(customer.getId());
        dto.setName(customer.getName());
        dto.setEmail(customer.getEmail());
        dto.setSource(customer.getSource());
        dto.setNotes(customer.getNotes());

        return dto;
    }
}
