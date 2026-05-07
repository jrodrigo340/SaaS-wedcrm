package com.wedcrm.enums;

public enum LeadSource {
    WEBSITE("Website"),
    REFERRAL("Indicação"),
    SOCIAL_MEDIA("Rede Social"),
    EMAIL_CAMPAIGN("Campanha de E-mail"),
    COLD_CALL("Ligação Ativa"),
    EVENT("Evento/Feira"),
    PARTNER("Parceiro"),
    OTHER("Outro");

    private final String description;

    LeadSource(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}