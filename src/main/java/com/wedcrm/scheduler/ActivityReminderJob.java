package com.wedcrm.scheduler;

@Component
@RequiredArgsConstructor
public class ActivityReminderJob {

    private final ActivityService activityService;
    private final NotificationService notificationService;

    @Scheduled(cron = "0 */5 * * * *")
    public void run() {

        List<Activity> activities =
                activityService.findActivitiesWithPendingReminder();

        for (Activity activity : activities) {

            notificationService.sendReminder(activity);

        }

    }

}
