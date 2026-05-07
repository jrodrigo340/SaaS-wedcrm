package com.wedcrm.enums;

public enum InteractionStatus {
    SENT("Enviado"),
    DELIVERED("Entregue"),
    READ("Lido"),
    BOUNCED("Devolvido"),
    FAILED("Falhou");

    private final String description;

    InteractionStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}