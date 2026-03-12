package com.wedcrm.service.impl;

@Service
@Transactional
public class ActivityServiceImpl implements ActivityService {

    private final ActivityRepository activityRepository;
    private final InteractionService interactionService;
    private final NotificationService notificationService;

    public ActivityServiceImpl(
            ActivityRepository activityRepository,
            InteractionService interactionService,
            NotificationService notificationService) {

        this.activityRepository = activityRepository;
        this.interactionService = interactionService;
        this.notificationService = notificationService;
    }

    @Override
    public Activity createActivity(ActivityRequest request) {

        Activity activity = new Activity();
        activity.setTitle(request.getTitle());
        activity.setPriority(request.getPriority());
        activity.setDueDate(request.getDueDate());
        activity.setReminderAt(request.getReminderAt());

        return activityRepository.save(activity);
    }

}
