package com.wedcrm.scheduler;

@Component
@RequiredArgsConstructor
public class LeadScoreUpdateJob {

    private final LeadScoreService scoreService;

    @Scheduled(cron = "0 0 2 * * *")
    public void run() {

        scoreService.recalculateAll();

    }

}
