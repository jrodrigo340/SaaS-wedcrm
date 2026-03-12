package com.wedcrm.repository;

import com.wedcrm.entity.Activity;
import com.wedcrm.entity.Customer;
import com.wedcrm.entity.User;
import com.wedcrm.enums.ActivityStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ActivityRepository extends JpaRepository<Activity, UUID> {

    List<Activity> findByAssignedToAndStatus(User user, ActivityStatus status);

    @Query("""
        SELECT a FROM Activity a
        WHERE a.dueDate < :now
        AND a.status <> com.wedcrm.enums.ActivityStatus.DONE
    """)
    List<Activity> findOverdueActivities(@Param("now") LocalDateTime now);

    @Query("""
        SELECT a FROM Activity a
        WHERE a.assignedTo = :user
        AND FUNCTION('DATE', a.dueDate) = FUNCTION('DATE', CURRENT_DATE)
    """)
    List<Activity> findActivitiesDueToday(@Param("user") User user);

    List<Activity> findByCustomerOrderByDueDateDesc(Customer customer);

    @Query("""
        SELECT a FROM Activity a
        WHERE a.reminderAt <= :now
        AND a.reminderSent = false
    """)
    List<Activity> findPendingReminders(@Param("now") LocalDateTime now);

}