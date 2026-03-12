package com.wedcrm.repository;

import com.wedcrm.entity.Customer;
import com.wedcrm.entity.Opportunity;
import com.wedcrm.entity.PipelineStage;
import com.wedcrm.entity.User;
import com.wedcrm.enums.OpportunityStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface OpportunityRepository extends JpaRepository<Opportunity, UUID> {

    List<Opportunity> findByCustomer(Customer customer);

    List<Opportunity> findByStage(PipelineStage stage);

    List<Opportunity> findByAssignedTo(User user);

    List<Opportunity> findByStatus(OpportunityStatus status);

    List<Opportunity> findByExpectedCloseDateBefore(LocalDate date);

    @Query("""
        SELECT COALESCE(SUM(o.value),0)
        FROM Opportunity o
        WHERE o.status = :status
    """)
    BigDecimal sumValueByStatus(@Param("status") OpportunityStatus status);

    List<Opportunity> findByStatusAndExpectedCloseDateBefore(
            OpportunityStatus status,
            LocalDate date
    );

}