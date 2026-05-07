package com.wedcrm.repository;

import com.wedcrm.entity.AutomationExecution;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AutomationExecutionRepository extends JpaRepository<AutomationExecution, UUID> {

    List<AutomationExecution> findByRuleId(UUID ruleId);

}