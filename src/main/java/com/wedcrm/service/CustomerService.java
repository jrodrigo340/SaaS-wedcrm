package com.wedcrm.service;

import com.wedcrm.dto.Assistants.ImportResultDTO;
import com.wedcrm.dto.response.CustomerResponse;
import com.wedcrm.dto.response.CustomerSummaryResponse;
import com.wedcrm.dto.request.CustomerFilterDTO;
import com.wedcrm.dto.Assistants.TimelineEventDTO;
import com.wedcrm.dto.request.CustomerRequestDTO;
import com.wedcrm.dto.response.CustomerResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface CustomerService {
    CustomerResponse updateCustomer(UUID id, CustomerRequestDTO request);

    CustomerResponse createCustomer(CustomerRequestDTO request);

    CustomerResponse getCustomerById(UUID id);

    Page<CustomerSummaryResponse> listCustomers(CustomerFilterDTO filter, Pageable pageable);

    void deleteCustomer(UUID id);

    CustomerResponse assignCustomer(UUID customerId, UUID userId);

    CustomerResponse addTag(UUID customerId, UUID tagId);

    CustomerResponse removeTag(UUID customerId, UUID tagId);

    CustomerResponse recalculateScore(UUID customerId);

    List<CustomerSummaryResponse> findInactiveCustomers(int days);

    ImportResultDTO importCustomers(MultipartFile csvFile);

    byte[] exportCustomers(CustomerFilterDTO filter);

    List<TimelineEventDTO> getCustomerTimeline(UUID id);
}