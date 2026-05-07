package com.wedcrm.service;

import com.wedcrm.dto.NotificationRequestDTO;
import com.wedcrm.dto.NotificationResponseDTO;
import com.wedcrm.dto.NotificationSummaryDTO;
import com.wedcrm.enums.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface NotificationService {

    NotificationResponseDTO createNotification(NotificationRequestDTO request);

    NotificationResponseDTO createNotification(UUID userId, String title, String message, NotificationType type);

    NotificationResponseDTO createNotification(UUID userId, String title, String message,
                                               NotificationType type, UUID referenceId, String referenceType);

    List<NotificationResponseDTO> getUnreadNotifications(UUID userId);

    NotificationSummaryDTO getNotificationSummary(UUID userId);

    Page<NotificationResponseDTO> getUserNotifications(UUID userId, Pageable pageable);

    void markAsRead(UUID notificationId);

    void markAllAsRead(UUID userId);

    void deleteNotification(UUID notificationId);

    void deleteReadNotifications(UUID userId);

    void sendPushNotification(UUID userId, String title, String body);

    void cleanOldNotifications();

    // Métodos de conveniência para criar notificações específicas
    void notifyActivityDue(UUID userId, String activityTitle, LocalDateTime dueDate, UUID activityId);

    void notifyBirthday(UUID userId, String customerName, UUID customerId);

    void notifyDealWon(UUID userId, String opportunityTitle, BigDecimal value, UUID opportunityId);

    void notifyCustomerAssigned(UUID userId, String customerName, UUID customerId);

    void notifySystemAlert(UUID userId, String message);
}