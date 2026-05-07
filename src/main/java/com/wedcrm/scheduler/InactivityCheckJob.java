package com.wedcrm.scheduler;

@Component
@RequiredArgsConstructor
public class InactivityCheckJob {

    private final CustomerService customerService;
    private final MessageGeneratorService messageService;

    @Scheduled(cron = "0 0 9 * * MON")
    public void run() {

        List<Customer> inactive30 =
                customerService.findInactiveCustomers(30);

        List<Customer> inactive60 =
                customerService.findInactiveCustomers(60);

        inactive30.forEach(c ->
                messageService.processAutomationTrigger(
                        AutomationTrigger.CUSTOMER_INACTIVE_30D, c));

        inactive60.forEach(c ->
                messageService.processAutomationTrigger(
                        AutomationTrigger.CUSTOMER_INACTIVE_60D, c));

    }
}
