package com.wedcrm.entity;

import com.wedcrm.enums.ActivityStatus;
import com.wedcrm.enums.ActivityType;
import com.wedcrm.enums.Priority;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "activities")
public class Activity extends AbstractEntity {

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ActivityType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ActivityStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Priority priority;

    @Column(name = "due_date", nullable = false)
    private LocalDateTime dueDate;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "opportunity_id")
    private Opportunity opportunity;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assigned_to_id", nullable = false)
    private User assignedTo;

    @Column(name = "reminder_at")
    private LocalDateTime reminderAt;

    @Column(name = "reminder_sent", nullable = false)
    private Boolean reminderSent = false;

    public Activity() {

    }

    public Activity(String title, ActivityType type, Priority priority,
                    LocalDateTime dueDate, User assignedTo) {
        this.title = title;
        this.type = type;
        this.priority = priority;
        this.dueDate = dueDate;
        this.assignedTo = assignedTo;
        this.status = ActivityStatus.PENDING;
        this.reminderSent = false;
    }

    // ========== Getters E Setters ==========

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ActivityType getType() {
        return type;
    }

    public void setType(ActivityType type) {
        this.type = type;
    }

    public ActivityStatus getStatus() {
        return status;
    }

    public void setStatus(ActivityStatus status) {
        this.status = status;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Opportunity getOpportunity() {
        return opportunity;
    }

    public void setOpportunity(Opportunity opportunity) {
        this.opportunity = opportunity;
    }

    public User getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(User assignedTo) {
        this.assignedTo = assignedTo;
    }

    public LocalDateTime getReminderAt() {
        return reminderAt;
    }

    public void setReminderAt(LocalDateTime reminderAt) {
        this.reminderAt = reminderAt;
    }

    public Boolean getReminderSent() {
        return reminderSent;
    }

    public void setReminderSent(Boolean reminderSent) {
        this.reminderSent = reminderSent;
    }

    // ========== Métodos De Negocio ==========

    /**
     * Marca a atividade como concluída
     */
    public void complete() {
        this.status = ActivityStatus.DONE;
        this.completedAt = LocalDateTime.now();
    }

    /**
     * Cancela a atividade
     */
    public void cancel() {
        this.status = ActivityStatus.CANCELLED;
        this.completedAt = LocalDateTime.now();
    }

    /**
     * Inicia a atividade
     */
    public void start() {
        if (this.status == ActivityStatus.PENDING) {
            this.status = ActivityStatus.IN_PROGRESS;
        }
    }

    /**
     * Verifica se a atividade está atrasada
     */
    public boolean isOverdue() {
        return this.status != ActivityStatus.DONE &&
                this.status != ActivityStatus.CANCELLED &&
                this.dueDate.isBefore(LocalDateTime.now());
    }

    /**
     * Verifica se tem lembrete pendente
     */
    public boolean hasPendingReminder() {
        return this.reminderAt != null &&
                !this.reminderSent &&
                this.reminderAt.isBefore(LocalDateTime.now()) ||
                this.reminderAt.isEqual(LocalDateTime.now());
    }

    /**
     * Marca lembrete como enviado
     */
    public void markReminderAsSent() {
        this.reminderSent = true;
    }

    /**
     * Verifica se a atividade pode ser editada
     */
    public boolean isEditable() {
        return this.status != ActivityStatus.DONE &&
                this.status != ActivityStatus.CANCELLED;
    }

    /**
     * Retorna o ícone baseado no tipo de atividade
     */
    public String getTypeIcon() {
        return switch (type) {
            case CALL -> "📞";
            case EMAIL -> "📧";
            case MEETING -> "🤝";
            case TASK -> "✅";
            case WHATSAPP -> "💬";
            case NOTE -> "📝";
            case DEMO -> "🎯";
            case PROPOSAL -> "📄";
        };
    }

    /**
     * Retorna a cor baseada na prioridade
     */
    public String getPriorityColor() {
        return switch (priority) {
            case LOW -> "#28a745";  // Verde
            case MEDIUM -> "#ffc107"; // Amarelo
            case HIGH -> "#fd7e14";   // Laranja
            case URGENT -> "#dc3545"; // Vermelho
        };
    }

    /**
     * Verifica se a atividade é de hoje
     */
    public boolean isDueToday() {
        LocalDateTime today = LocalDateTime.now();
        return dueDate.toLocalDate().isEqual(today.toLocalDate());
    }

    /**
     * Verifica se a atividade é desta semana
     */
    public boolean isDueThisWeek() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endOfWeek = now.plusWeeks(1);
        return dueDate.isAfter(now) && dueDate.isBefore(endOfWeek);
    }

    @Override
    public String toString() {
        return "Activity{" +
                "id=" + getId() +
                ", title='" + title + '\'' +
                ", type=" + type +
                ", status=" + status +
                ", priority=" + priority +
                ", dueDate=" + dueDate +
                '}';
    }
}