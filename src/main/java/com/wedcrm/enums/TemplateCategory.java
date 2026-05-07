package com.wedcrm.enums;

public enum TemplateCategory {
    WELCOME("Boas-vindas"),
    FOLLOWUP("Follow-up"),
    BIRTHDAY("Aniversário"),
    PROPOSAL("Proposta"),
    REMINDER("Lembrete"),
    NURTURING("Nutrição"),
    WIN_BACK("Reengajamento"),
    CUSTOM("Personalizado");

    private final String description;

    TemplateCategory(String description) { this.description = description; }
    public String getDescription() { return description; }
}