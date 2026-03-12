package com.wedcrm.repository;

import com.wedcrm.entity.Customer;
import com.wedcrm.entity.Tag;
import com.wedcrm.entity.User;
import com.wedcrm.enums.CustomerStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.MonthDay;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    List<Customer> findByStatus(CustomerStatus status);

    List<Customer> findByAssignedTo(User user);

    Optional<Customer> findByEmailIgnoreCase(String email);

    @Query("""
        SELECT c FROM Customer c
        WHERE LOWER(c.firstName) LIKE LOWER(CONCAT('%', :term, '%'))
        OR LOWER(c.lastName) LIKE LOWER(CONCAT('%', :term, '%'))
        OR LOWER(c.email) LIKE LOWER(CONCAT('%', :term, '%'))
        OR LOWER(c.company) LIKE LOWER(CONCAT('%', :term, '%'))
    """)
    Page<Customer> searchCustomers(@Param("term") String term, Pageable pageable);

    @Query("""
        SELECT c FROM Customer c
        WHERE FUNCTION('MONTH', c.birthday) = :month
        AND FUNCTION('DAY', c.birthday) = :day
    """)
    List<Customer> findByBirthday(@Param("month") int month, @Param("day") int day);

    @Query("""
        SELECT c FROM Customer c
        WHERE c.lastContactDate < :since
    """)
    List<Customer> findInactiveCustomers(@Param("since") LocalDate since);

    List<Customer> findByTagsContaining(Tag tag);

    Long countByStatus(CustomerStatus status);

    List<Customer> findByScoreGreaterThan(int score);

}