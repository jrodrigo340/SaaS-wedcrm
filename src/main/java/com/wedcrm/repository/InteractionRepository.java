package com.wedcrm.repository;

import com.wedcrm.entity.Interaction;
import com.wedcrm.entity.Customer;
import com.wedcrm.enums.InteractionType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface InteractionRepository extends JpaRepository<Interaction, UUID> {

    Page<Interaction> findByCustomerOrderByCreatedAtDesc(Customer customer, Pageable pageable);

    List<Interaction> findByCustomerAndType(Customer customer, InteractionType type);

    Long countByCustomerAndCreatedAtBetween(
            Customer customer,
            LocalDateTime start,
            LocalDateTime end
    );

    List<Interaction> findByIsAutomaticTrue();

}
