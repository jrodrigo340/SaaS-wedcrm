package com.wedcrm.entity;

import com.wedcrm.enums.Direction;
import com.wedcrm.enums.InteractionStatus;
import com.wedcrm.enums.InteractionType;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "interactions")
public class Interaction extends AbstractEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InteractionType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Direction direction;

    @Column(length = 300)
    private String subject;

    @Column(nullable = false, length = 10000)
    private String content;

    @Column(length = 50)
    private String channel;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private InteractionStatus status;

    @Column(name = "is_automatic", nullable = false)
    private Boolean isAutomatic = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id")
    private MessageTemplate templateUsed;

    public Interaction() {
        // Construtor padrão obrigatório para JPA
    }

    // Construtor para interações manuais
    public Interaction(Customer customer, User user, InteractionType type,
                       Direction direction, String content) {
        this.customer = customer;
        this.user = user;
        this.type = type;
        this.direction = direction;
        this.content = content;
        this.isAutomatic = false;
        this.sentAt = LocalDateTime.now();
        this.status = InteractionStatus.SENT;
    }

    // Construtor para interações automáticas
    public Interaction(Customer customer, InteractionType type,
                       Direction direction, String content, MessageTemplate template) {
        this.customer = customer;
        this.type = type;
        this.direction = direction;
        this.content = content;
        this.templateUsed = template;
        this.isAutomatic = true;
        this.sentAt = LocalDateTime.now();
        this.status = InteractionStatus.SENT;
    }

    // ========== Getters E Setters ==========

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public InteractionType getType() {
        return type;
    }

    public void setType(InteractionType type) {
        this.type = type;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }

    public LocalDateTime getReadAt() {
        return readAt;
    }

    public void setReadAt(LocalDateTime readAt) {
        this.readAt = readAt;
    }

    public InteractionStatus getStatus() {
        return status;
    }

    public void setStatus(InteractionStatus status) {
        this.status = status;
    }

    public Boolean getIsAutomatic() {
        return isAutomatic;
    }

    public void setIsAutomatic(Boolean isAutomatic) {
        this.isAutomatic = isAutomatic;
    }

    public MessageTemplate getTemplateUsed() {
        return templateUsed;
    }

    public void setTemplateUsed(MessageTemplate templateUsed) {
        this.templateUsed = templateUsed;
    }

    // ========== MÉTODOS DE NEGÓCIO ==========

    /**
     * Marca a interação como lida
     */
    public void markAsRead() {
        this.readAt = LocalDateTime.now();
        this.status = InteractionStatus.READ;
    }

    /**
     * Marca a interação como entregue
     */
    public void markAsDelivered() {
        this.status = InteractionStatus.DELIVERED;
    }

    /**
     * Marca a interação como falha
     */
    public void markAsFailed() {
        this.status = InteractionStatus.FAILED;
    }

    /**
     * Verifica se a interação foi lida
     */
    public boolean isRead() {
        return this.readAt != null || InteractionStatus.READ.equals(this.status);
    }

    /**
     * Verifica se é uma interação de e-mail
     */
    public boolean isEmail() {
        return InteractionType.EMAIL.equals(this.type);
    }

    /**
     * Verifica se é uma interação de WhatsApp
     */
    public boolean isWhatsApp() {
        return InteractionType.WHATSAPP.equals(this.type);
    }

    /**
     * Retorna o ícone baseado no tipo de interação
     */
    public String getTypeIcon() {
        return switch (type) {
            case EMAIL -> "📧";
            case CALL -> "📞";
            case SMS -> "📱";
            case WHATSAPP -> "💬";
            case MEETING -> "🤝";
            case NOTE -> "📝";
            case AUTO_MESSAGE -> "🤖";
        };
    }

    /**
     * Retorna a cor baseada no status
     */
    public String getStatusColor() {
        return switch (status) {
            case SENT -> "#17a2b8";    // Azul claro
            case DELIVERED -> "#28a745"; // Verde
            case READ -> "#007bff";     // Azul
            case BOUNCED -> "#ffc107";   // Amarelo
            case FAILED -> "#dc3545";    // Vermelho
        };
    }

    /**
     * Retorna um resumo do conteúdo (primeiros caracteres)
     */
    public String getContentSummary(int maxLength) {
        if (content == null || content.length() <= maxLength) {
            return content;
        }
        return content.substring(0, maxLength) + "...";
    }

    /**
     * Retorna o tempo desde o envio
     */
    public String getTimeAgo() {
        LocalDateTime now = LocalDateTime.now();
        long minutes = java.time.Duration.between(sentAt, now).toMinutes();

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

    @Override
    public String toString() {
        return "Interaction{" +
                "id=" + getId() +
                ", type=" + type +
                ", direction=" + direction +
                ", customer=" + (customer != null ? customer.getId() : null) +
                ", sentAt=" + sentAt +
                ", status=" + status +
                '}';
    }
}