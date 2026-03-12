package com.wedcrm.service;

import java.util.List;
import java.util.UUID;

public interface NotificationService {

    Notification createNotification(UUID userId, NotificationRequest request);

    List<Notification> getUnreadNotifications(UUID userId);

    void markAsRead(UUID notificationId);

    void markAllAsRead(UUID userId);

    void sendPushNotification(UUID userId, String title, String body);

    void cleanOldNotifications();

}
