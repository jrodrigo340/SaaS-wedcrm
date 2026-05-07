package com.wedcrm.automation.event;

public class DealWonEvent {

    private final Opportunity opportunity;

    public DealWonEvent(Opportunity opportunity) {
        this.opportunity = opportunity;
    }

    public Opportunity getOpportunity() {
        return opportunity;
    }

}
