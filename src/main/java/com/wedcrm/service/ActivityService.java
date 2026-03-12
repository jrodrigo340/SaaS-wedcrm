package com.wedcrm.service;

import com.wedcrm.dto.activity.*;
import com.wedcrm.entity.Activity;

import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

public interface ActivityService {

    Activity createActivity(ActivityRequest request);

    Activity updateActivity(UUID id, ActivityRequest request);

    void completeActivity(UUID id);

    void cancelActivity(UUID id, String reason);

    List<Activity> getTodayActivities(UUID userId);

    List<Activity> getOverdueActivities(UUID userId);

    void processActivityReminders();

    ActivityCalendarResponse getActivityCalendar(UUID userId, YearMonth month);
}
