package com.wedcrm.repository;

import com.wedcrm.entity.AutomationRule;
import com.wedcrm.enums.AutomationTrigger;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AutomationRuleRepository extends JpaRepository<AutomationRule, UUID> {

    List<AutomationRule> findByTriggerAndIsActiveTrue(AutomationTrigger trigger);

    List<AutomationRule> findAllByIsActiveTrue();

}

