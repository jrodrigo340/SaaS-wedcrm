package com.wedcrm.service.impl;

@Service
@Transactional
public class MessageGeneratorServiceImpl implements MessageGeneratorService {

    private final MessageTemplateRepository templateRepository;
    private final CustomerRepository customerRepository;
    private final AutomationRuleRepository automationRuleRepository;
    private final InteractionRepository interactionRepository;

    public MessageGeneratorServiceImpl(
            MessageTemplateRepository templateRepository,
            CustomerRepository customerRepository,
            AutomationRuleRepository automationRuleRepository,
            InteractionRepository interactionRepository) {

        this.templateRepository = templateRepository;
        this.customerRepository = customerRepository;
        this.automationRuleRepository = automationRuleRepository;
        this.interactionRepository = interactionRepository;
    }

}
