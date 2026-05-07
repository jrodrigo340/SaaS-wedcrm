package com.wedcrm.service.impl;

import com.wedcrm.dto.NotificationRequestDTO;
import com.wedcrm.dto.NotificationResponseDTO;
import com.wedcrm.dto.NotificationSummaryDTO;
import com.wedcrm.entity.Notification;
import com.wedcrm.entity.User;
import com.wedcrm.enums.NotificationType;
import com.wedcrm.repository.NotificationRepository;
import com.wedcrm.repository.UserRepository;
import com.wedcrm.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Value("${wedcrm.push.enabled:false}")
    private boolean pushEnabled;

    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // ========== CRIAÇÃO DE NOTIFICAÇÕES ==========

    @Override
    public NotificationResponseDTO createNotification(NotificationRequestDTO request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle(request.title());
        notification.setMessage(request.message());
        notification.setType(request.type());
        notification.setIsRead(false);
        notification.setReferenceId(request.referenceId());
        notification.setReferenceType(request.referenceType());

        Notification saved = notificationRepository.save(notification);

        // Envia push notification se configurado
        if (pushEnabled) {
            sendPushNotification(user.getId(), request.title(), request.message());
        }

        return toResponseDTO(saved);
    }

    @Override
    public NotificationResponseDTO createNotification(UUID userId, String title, String message, NotificationType type) {
        return createNotification(NotificationRequestDTO.builder()
                .userId(userId)
                .title(title)
                .message(message)
                .type(type)
                .build());
    }

    @Override
    public NotificationResponseDTO createNotification(UUID userId, String title, String message,
                                                      NotificationType type, UUID referenceId, String referenceType) {
        return createNotification(NotificationRequestDTO.builder()
                .userId(userId)
                .title(title)
                .message(message)
                .type(type)
                .referenceId(referenceId)
                .referenceType(referenceType)
                .build());
    }

    // ========== CONSULTAS ==========

    @Override
    public List<NotificationResponseDTO> getUnreadNotifications(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        List<Notification> notifications = notificationRepository.findByUserAndIsReadFalseOrderByCreatedAtDesc(user);
        return notifications.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public NotificationSummaryDTO getNotificationSummary(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        long unreadCount = notificationRepository.countByUserAndIsReadFalse(user);
        long totalCount = notificationRepository.countByUser(user);

        // Últimas 5 notificações não lidas
        List<Notification> recentUnread = notificationRepository.findByUserAndIsReadFalseOrderByCreatedAtDesc(user)
                .stream()
                .limit(5)
                .collect(Collectors.toList());

        return NotificationSummaryDTO.builder()
                .unreadCount(unreadCount)
                .totalCount(totalCount)
                .hasUnread(unreadCount > 0)
                .recentUnread(recentUnread.stream().map(this::toResponseDTO).collect(Collectors.toList()))
                .build();
    }

    @Override
    public Page<NotificationResponseDTO> getUserNotifications(UUID userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Page<Notification> notifications = notificationRepository.findByUser(user, pageable);
        return notifications.map(this::toResponseDTO);
    }

    // ========== AÇÕES ==========

    @Override
    public void markAsRead(UUID notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notificação não encontrada"));

        notification.markAsRead();
        notificationRepository.save(notification);
    }

    @Override
    public void markAllAsRead(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        notificationRepository.markAllAsRead(user, LocalDateTime.now());
    }

    @Override
    public void deleteNotification(UUID notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notificação não encontrada"));
        notificationRepository.delete(notification);
    }

    @Override
    public void deleteReadNotifications(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        notificationRepository.deleteReadNotifications(user);
    }

    // ========== PUSH NOTIFICATIONS ==========

    @Override
    public void sendPushNotification(UUID userId, String title, String body) {
        if (!pushEnabled) {
            return;
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        // Aqui seria implementada a integração com FCM (Firebase Cloud Messaging)
        // ou APNs (Apple Push Notification service)

        // Exemplo de implementação com FCM:
        // String deviceToken = user.getDeviceToken();
        // if (deviceToken != null && !deviceToken.isEmpty()) {
        //     fcmService.sendNotification(deviceToken, title, body);
        // }

        // Log para debug
        System.out.println("Push notification para " + user.getEmail() + ": " + title + " - " + body);
    }

    // ========== MENSAGENS PROGRAMADAS ==========

    @Override
    @Scheduled(cron = "0 0 2 * * *") // Todos os dias às 2:00 AM
    public void cleanOldNotifications() {
        LocalDateTime ninetyDaysAgo = LocalDateTime.now().minusDays(90);
        int deleted = notificationRepository.deleteOldNotifications(ninetyDaysAgo);
        System.out.println("Removidas " + deleted + " notificações com mais de 90 dias");
    }

    // ========== MÉTODOS DE CONVENIÊNCIA ==========

    @Override
    public void notifyActivityDue(UUID userId, String activityTitle, LocalDateTime dueDate, UUID activityId) {
        String formattedDueDate = dueDate.format(timeFormatter);
        String title = "🔔 Atividade Pendente";
        String message = String.format("A atividade \"%s\" está programada para %s",
                activityTitle, formattedDueDate);

        createNotification(userId, title, message, NotificationType.ACTIVITY_DUE, activityId, "Activity");
    }

    @Override
    public void notifyBirthday(UUID userId, String customerName, UUID customerId) {
        String title = "🎂 Aniversário do Cliente";
        String message = String.format("Hoje é aniversário de %s! Envie uma mensagem de parabéns.",
                customerName);

        createNotification(userId, title, message, NotificationType.BIRTHDAY, customerId, "Customer");
    }

    @Override
    public void notifyDealWon(UUID userId, String opportunityTitle, BigDecimal value, UUID opportunityId) {
        String formattedValue = String.format("R$ %,.2f", value);
        String title = "🏆 Negócio Fechado!";
        String message = String.format("Parabéns! A oportunidade \"%s\" foi fechada no valor de %s",
                opportunityTitle, formattedValue);

        createNotification(userId, title, message, NotificationType.DEAL_WON, opportunityId, "Opportunity");
    }

    @Override
    public void notifyCustomerAssigned(UUID userId, String customerName, UUID customerId) {
        String title = "👤 Novo Cliente Atribuído";
        String message = String.format("O cliente %s foi atribuído a você.", customerName);

        createNotification(userId, title, message, NotificationType.CUSTOMER_ASSIGNED, customerId, "Customer");
    }

    @Override
    public void notifySystemAlert(UUID userId, String message) {
        String title = "ℹ️ Alerta do Sistema";
        createNotification(userId, title, message, NotificationType.SYSTEM);
    }

    // ========== MÉTODOS PRIVADOS ==========

    private NotificationResponseDTO toResponseDTO(Notification notification) {
        return NotificationResponseDTO.builder()
                .id(notification.getId())
                .userId(notification.getUser().getId())
                .userName(notification.getUser().getName())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .type(notification.getType())
                .icon(notification.getIcon())
                .typeColor(notification.getTypeColor())
                .isRead(notification.getIsRead())
                .readAt(notification.getReadAt())
                .createdAt(notification.getCreatedAt())
                .timeAgo(notification.getTimeAgo())
                .referenceId(notification.getReferenceId())
                .referenceType(notification.getReferenceType())
                .build();
    }
}