package com.wedcrm.enums;

public enum NotificationType {
    ACTIVITY_DUE("Atividade Pendente", "🔔"),
    BIRTHDAY("Aniversário", "🎂"),
    DEAL_WON("Negócio Ganho", "🏆"),
    REMINDER("Lembrete", "⏰"),
    SYSTEM("Sistema", "ℹ️"),
    CUSTOMER_ASSIGNED("Cliente Atribuído", "👤"),
    OPPORTUNITY_STAGE_CHANGED("Mudança de Estágio", "📊"),
    FOLLOW_UP("Follow-up", "📞"),
    WEEKLY_REPORT("Relatório Semanal", "📈");

    private final String description;
    private final String icon;

    NotificationType(String description, String icon) {
        this.description = description;
        this.icon = icon;
    }

    public String getDescription() {
        return description;
    }

    public String getIcon() {
        return icon;
    }
}