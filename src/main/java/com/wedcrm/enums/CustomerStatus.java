package com.wedcrm.enums;

public enum CustomerStatus {
    LEAD("Lead - Primeiro contato"),
    PROSPECT("Prospect - Em negociação"),
    CUSTOMER("Cliente - Negócio fechado"),
    INACTIVE("Inativo - Sem movimento"),
    LOST("Perdido - Não virou cliente");

    private final String description;

    CustomerStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}