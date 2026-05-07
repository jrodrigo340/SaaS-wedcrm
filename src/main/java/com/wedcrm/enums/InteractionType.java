package com.wedcrm.enums;

public enum InteractionType {
    EMAIL("E-mail"),
    CALL("Ligação"),
    SMS("SMS"),
    WHATSAPP("WhatsApp"),
    MEETING("Reunião"),
    NOTE("Anotação"),
    AUTO_MESSAGE("Mensagem Automática");

    private final String description;

    InteractionType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}