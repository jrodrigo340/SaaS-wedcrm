package com.wedcrm.automation.listener;

@Component
@RequiredArgsConstructor
public class AutomationEventListener {

    private final AutomationRuleService ruleService;

    @EventListener
    public void onDealWon(DealWonEvent event) {

        ruleService.processTrigger(
                AutomationTrigger.DEAL_WON,
                event.getOpportunity().getCustomer()
        );

    }

}
