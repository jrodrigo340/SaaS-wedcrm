package com.wedcrm.scheduler;

@Component
@RequiredArgsConstructor
public class CleanNotificationsJob {

    private final NotificationService notificationService;

    @Scheduled(cron = "0 0 3 * * SUN")
    public void run() {

        notificationService.cleanOldNotifications();

    }

}
