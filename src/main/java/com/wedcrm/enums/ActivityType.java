package com.wedcrm.enums;

public enum ActivityType {
    CALL("Ligação", "📞"),
    EMAIL("E-mail", "📧"),
    MEETING("Reunião", "🤝"),
    TASK("Tarefa", "✅"),
    WHATSAPP("WhatsApp", "💬"),
    NOTE("Anotação", "📝"),
    DEMO("Demonstração", "🎯"),
    PROPOSAL("Proposta", "📄");

    private final String description;
    private final String icon;

    ActivityType(String description, String icon) {
        this.description = description;
        this.icon = icon;
    }

    public String getDescription() { return description; }
    public String getIcon() { return icon; }
}