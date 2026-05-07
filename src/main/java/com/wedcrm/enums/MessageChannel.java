package com.wedcrm.enums;

public enum MessageChannel {
    EMAIL("E-mail"),
    SMS("SMS"),
    WHATSAPP("WhatsApp"),
    PUSH("Push Notification");

    private final String description;

    MessageChannel(String description) { this.description = description; }
    public String getDescription() { return description; }
}