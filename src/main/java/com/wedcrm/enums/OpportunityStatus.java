package com.wedcrm.enums;

public enum OpportunityStatus {
    OPEN("Aberta - Em negociação"),
    WON("Ganha - Fechada com sucesso"),
    LOST("Perdida - Não fechada"),
    ABANDONED("Abandonada - Sem progresso");

    private final String description;

    OpportunityStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}