package com.wedcrm.service;

import com.wedcrm.dto.AutomationRuleRequestDTO;
import com.wedcrm.dto.AutomationRuleResponseDTO;
import com.wedcrm.dto.RuleExecutionHistoryDTO;
import com.wedcrm.entity.AutomationRule;
import com.wedcrm.entity.Customer;
import com.wedcrm.enums.AutomationTrigger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface AutomationRuleService {

    AutomationRuleResponseDTO createRule(AutomationRuleRequestDTO request);

    AutomationRuleResponseDTO updateRule(UUID id, AutomationRuleRequestDTO request);

    AutomationRuleResponseDTO getRuleById(UUID id);

    Page<AutomationRuleResponseDTO> listRules(AutomationRuleFilterDTO filter, Pageable pageable);

    void deleteRule(UUID id);

    AutomationRuleResponseDTO toggleRule(UUID id);

    List<AutomationRuleResponseDTO> getRulesByTrigger(AutomationTrigger trigger);

    boolean evaluateConditions(AutomationRule rule, Customer customer);

    void executeRule(AutomationRule rule, Customer customer);

    List<RuleExecutionHistoryDTO> getRuleExecutionHistory(UUID ruleId);

    AutomationRuleResponseDTO duplicateRule(UUID id, String newName);

    void validateRule(AutomationRule rule);
}