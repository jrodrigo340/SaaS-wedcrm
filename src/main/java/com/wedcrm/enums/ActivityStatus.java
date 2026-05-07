package com.wedcrm.enums;

public enum ActivityStatus {
    PENDING("Pendente"),
    IN_PROGRESS("Em Andamento"),
    DONE("Concluída"),
    CANCELLED("Cancelada");

    private final String description;

    ActivityStatus(String description) { this.description = description; }
    public String getDescription() { return description; }
}