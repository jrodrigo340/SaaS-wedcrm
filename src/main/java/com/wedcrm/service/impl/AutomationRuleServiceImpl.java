package com.wedcrm.service.impl;

@Service
@Transactional
public class AutomationRuleServiceImpl implements AutomationRuleService {

    private final AutomationRuleRepository ruleRepository;
    private final MessageGeneratorService messageGeneratorService;
    private final AutomationExecutionRepository executionRepository;

    public AutomationRuleServiceImpl(
            AutomationRuleRepository ruleRepository,
            MessageGeneratorService messageGeneratorService,
            AutomationExecutionRepository executionRepository) {

        this.ruleRepository = ruleRepository;
        this.messageGeneratorService = messageGeneratorService;
        this.executionRepository = executionRepository;
    }
}