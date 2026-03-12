package com.wedcrm.service.impl;

@Service
@Transactional
public class OpportunityServiceImpl implements OpportunityService {

    private final OpportunityRepository opportunityRepository;
    private final PipelineStageRepository stageRepository;
    private final ProductRepository productRepository;
    private final AutomationService automationService;

    public OpportunityServiceImpl(
            OpportunityRepository opportunityRepository,
            PipelineStageRepository stageRepository,
            ProductRepository productRepository,
            AutomationService automationService) {

        this.opportunityRepository = opportunityRepository;
        this.stageRepository = stageRepository;
        this.productRepository = productRepository;
        this.automationService = automationService;
    }

    @Override
    public Opportunity createOpportunity(OpportunityRequest request) {

        Opportunity opp = new Opportunity();
        opp.setTitle(request.getTitle());
        opp.setExpectedCloseDate(request.getExpectedCloseDate());

        Opportunity saved = opportunityRepository.save(opp);

        automationService.triggerDealCreated(saved);

        return saved;
    }
}
