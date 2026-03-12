package com.wedcrm.service;

import com.wedcrm.dto.customer.*;
import com.wedcrm.entity.Customer;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface CustomerService {

    Customer createCustomer(CustomerRequest request);

    Customer updateCustomer(UUID id, CustomerRequest request);

    Customer getCustomerById(UUID id);

    Page<Customer> listCustomers(CustomerFilter filter, Pageable pageable);

    void deleteCustomer(UUID id);

    void assignCustomer(UUID customerId, UUID userId);

    void addTag(UUID customerId, UUID tagId);

    void removeTag(UUID customerId, UUID tagId);

    void recalculateScore(UUID customerId);

    List<Customer> findInactiveCustomers(int days);

    ImportResult importCustomers(MultipartFile csv);

    byte[] exportCustomers(CustomerFilter filter);

    CustomerTimelineResponse getCustomerTimeline(UUID id);
}
