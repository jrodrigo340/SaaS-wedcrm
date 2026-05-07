package com.wedcrm.enums;

public enum AutomationTrigger {
    CUSTOMER_CREATED("Cliente Criado"),
    CUSTOMER_STATUS_CHANGED("Status do Cliente Alterado"),
    DEAL_CREATED("Oportunidade Criada"),
    DEAL_STAGE_CHANGED("Estágio da Oportunidade Alterado"),
    DEAL_WON("Oportunidade Ganha"),
    DEAL_LOST("Oportunidade Perdida"),
    ACTIVITY_COMPLETED("Atividade Concluída"),
    CUSTOMER_BIRTHDAY("Aniversário do Cliente"),
    CUSTOMER_INACTIVE_30D("Cliente Inativo por 30 Dias"),
    CUSTOMER_INACTIVE_60D("Cliente Inativo por 60 Dias"),
    PROPOSAL_SENT("Proposta Enviada"),
    MANUAL_TRIGGER("Disparo Manual");

    private final String description;

    AutomationTrigger(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}