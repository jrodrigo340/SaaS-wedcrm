package com.wedcrm.enums;

public enum Priority {
    LOW("Baixa", "#28a745"),
    MEDIUM("Média", "#ffc107"),
    HIGH("Alta", "#fd7e14"),
    URGENT("Urgente", "#dc3545");

    private final String description;
    private final String color;

    Priority(String description, String color) {
        this.description = description;
        this.color = color;
    }

    public String getDescription() { return description; }
    public String getColor() { return color; }
}