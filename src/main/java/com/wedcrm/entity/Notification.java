package com.wedcrm.entity;

import com.wedcrm.enums.NotificationType;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "notifications")
public class Notification extends AbstractEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 2000)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NotificationType type;

    @Column(nullable = false)
    private Boolean isRead = false;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Column(name = "reference_id")
    private UUID referenceId;

    @Column(name = "reference_type", length = 50)
    private String referenceType;

    public Notification() {
    }

    public Notification(User user, String title, String message, NotificationType type) {
        this.user = user;
        this.title = title;
        this.message = message;
        this.type = type;
        this.isRead = false;
    }

    public Notification(User user, String title, String message,
                        NotificationType type, UUID referenceId, String referenceType) {
        this.user = user;
        this.title = title;
        this.message = message;
        this.type = type;
        this.referenceId = referenceId;
        this.referenceType = referenceType;
        this.isRead = false;
    }

    // ========== Getters E Setters ==========

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public Boolean getIsRead() {
        return isRead;
    }

    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
    }

    public LocalDateTime getReadAt() {
        return readAt;
    }

    public void setReadAt(LocalDateTime readAt) {
        this.readAt = readAt;
    }

    public UUID getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(UUID referenceId) {
        this.referenceId = referenceId;
    }

    public String getReferenceType() {
        return referenceType;
    }

    public void setReferenceType(String referenceType) {
        this.referenceType = referenceType;
    }

    // ========== Métodos de Negóciod ==========

    /**
     * Marca a notificação como lida
     */
    public void markAsRead() {
        if (!Boolean.TRUE.equals(this.isRead)) {
            this.isRead = true;
            this.readAt = LocalDateTime.now();
        }
    }

    /**
     * Marca a notificação como não lida
     */
    public void markAsUnread() {
        this.isRead = false;
        this.readAt = null;
    }

    /**
     * Verifica se a notificação foi lida
     */
    public boolean isRead() {
        return Boolean.TRUE.equals(isRead);
    }

    /**
     * Retorna o ícone baseado no tipo
     */
    public String getIcon() {
        return type.getIcon();
    }

    /**
     * Retorna a cor baseada no tipo
     */
    public String getTypeColor() {
        return switch (type) {
            case ACTIVITY_DUE -> "#ffc107";  // Amarelo
            case BIRTHDAY -> "#fd7e14";      // Laranja
            case DEAL_WON -> "#28a745";      // Verde
            case REMINDER -> "#17a2b8";      // Azul claro
            case SYSTEM -> "#6c757d";        // Cinza
            case CUSTOMER_ASSIGNED -> "#007bff"; // Azul
            case OPPORTUNITY_STAGE_CHANGED -> "#6f42c1"; // Roxo
            case FOLLOW_UP -> "#20c997";     // Verde água
            case WEEKLY_REPORT -> "#dc3545"; // Vermelho
        };
    }

    /**
     * Retorna o tempo desde a criação da notificação
     */
    public String getTimeAgo() {
        LocalDateTime now = LocalDateTime.now();
        long minutes = java.time.Duration.between(getCreatedAt(), now).toMinutes();

        if (minutes < 1) return "agora mesmo";
        if (minutes < 60) return minutes + " min atrás";

        long hours = minutes / 60;
        if (hours < 24) return hours + "h atrás";

        long days = hours / 24;
        if (days < 30) return days + " dias atrás";

        long months = days / 30;
        if (months < 12) return months + " meses atrás";

        long years = months / 12;
        return years + " anos atrás";
    }

    /**
     * Cria uma notificação de atividade pendente
     */
    public static Notification createActivityDueNotification(User user, Activity activity) {
        String title = "Atividade Pendente: " + activity.getTitle();
        String message = String.format(
                "A atividade \"%s\" está programada para %s",
                activity.getTitle(),
                activity.getDueDate().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
        );

        return new Notification(
                user, title, message, NotificationType.ACTIVITY_DUE,
                activity.getId(), "Activity"
        );
    }

    /**
     * Cria uma notificação de aniversário
     */
    public static Notification createBirthdayNotification(User user, Customer customer) {
        String title = "🎂 Aniversário do Cliente!";
        String message = String.format(
                "Hoje é aniversário de %s! Envie uma mensagem de parabéns.",
                customer.getFullName()
        );

        return new Notification(
                user, title, message, NotificationType.BIRTHDAY,
                customer.getId(), "Customer"
        );
    }

    /**
     * Cria uma notificação de negócio ganho
     */
    public static Notification createDealWonNotification(User user, Opportunity opportunity) {
        String title = "🏆 Negócio Fechado!";
        String message = String.format(
                "Parabéns! A oportunidade \"%s\" foi fechada no valor de %s",
                opportunity.getTitle(),
                opportunity.getFormattedValue()
        );

        return new Notification(
                user, title, message, NotificationType.DEAL_WON,
                opportunity.getId(), "Opportunity"
        );
    }

    /**
     * Cria uma notificação de cliente atribuído
     */
    public static Notification createCustomerAssignedNotification(User user, Customer customer) {
        String title = "Novo Cliente Atribuído";
        String message = String.format(
                "O cliente %s foi atribuído a você",
                customer.getFullName()
        );

        return new Notification(
                user, title, message, NotificationType.CUSTOMER_ASSIGNED,
                customer.getId(), "Customer"
        );
    }

    @Override
    public String toString() {
        return "Notification{" +
                "id=" + getId() +
                ", userId=" + (user != null ? user.getId() : null) +
                ", title='" + title + '\'' +
                ", type=" + type +
                ", isRead=" + isRead +
                ", createdAt=" + getCreatedAt() +
                '}';
    }
}