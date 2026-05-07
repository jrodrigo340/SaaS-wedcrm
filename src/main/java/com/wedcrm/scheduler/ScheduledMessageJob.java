package com.wedcrm.scheduler;

@Component
@RequiredArgsConstructor
public class ScheduledMessageJob {

    private final MessageQueueService queueService;

    @Scheduled(cron = "0 * * * * *")
    public void run() {

        queueService.processPendingMessages();

    }

}
