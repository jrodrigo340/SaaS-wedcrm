package com.wedcrm.enums;

public enum Direction {
    INBOUND("Recebido"),
    OUTBOUND("Enviado");

    private final String description;

    Direction(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}