package com.wedcrm.service;

import java.util.List;
import java.util.UUID;

public interface AutomationRuleService {

    AutomationRule createRule(AutomationRuleRequest request);

    AutomationRule updateRule(UUID id, AutomationRuleRequest request);

    void toggleRule(UUID id);

    List<AutomationRule> getRulesByTrigger(AutomationTrigger trigger);

    boolean evaluateConditions(AutomationRule rule, Customer customer);

    void executeRule(AutomationRule rule, Customer customer);

    List<AutomationExecution> getRuleExecutionHistory(UUID ruleId);

}
