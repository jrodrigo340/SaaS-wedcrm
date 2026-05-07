package com.wedcrm.enums;

public enum Role {
    ADMIN("Administrador - Acesso total ao sistema"),
    MANAGER("Gerente - Acesso a relatórios e gestão de equipe"),
    SALES("Vendedor - Acesso a seus próprios clientes e atividades"),
    VIEWER("Visualizador - Acesso apenas leitura");

    private final String description;

    Role(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}