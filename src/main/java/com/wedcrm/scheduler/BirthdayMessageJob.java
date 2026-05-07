package com.wedcrm.scheduler;

@Component
@RequiredArgsConstructor
public class BirthdayMessageJob {

    private final CustomerService customerService;
    private final MessageGeneratorService messageService;

    @Scheduled(cron = "0 0 8 * * *")
    public void run() {

        List<Customer> customers =
                customerService.findCustomersWithBirthdayToday();

        for (Customer customer : customers) {

            messageService.processAutomationTrigger(
                    AutomationTrigger.CUSTOMER_BIRTHDAY,
                    customer
            );

        }

    }
}
