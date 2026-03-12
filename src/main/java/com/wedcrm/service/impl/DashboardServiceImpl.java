package com.wedcrm.service.impl;

@Service
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final CustomerRepository customerRepository;
    private final OpportunityRepository opportunityRepository;
    private final ActivityRepository activityRepository;

    public DashboardServiceImpl(
            CustomerRepository customerRepository,
            OpportunityRepository opportunityRepository,
            ActivityRepository activityRepository) {

        this.customerRepository = customerRepository;
        this.opportunityRepository = opportunityRepository;
        this.activityRepository = activityRepository;
    }

}
