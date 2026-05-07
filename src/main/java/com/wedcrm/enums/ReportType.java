package com.wedcrm.enums;

public enum ReportType {
    SALES("Relatório de Vendas"),
    CUSTOMERS("Relatório de Clientes"),
    ACTIVITIES("Relatório de Atividades"),
    PIPELINE("Relatório de Pipeline"),
    PERFORMANCE("Relatório de Performance");

    private final String description;

    ReportType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
