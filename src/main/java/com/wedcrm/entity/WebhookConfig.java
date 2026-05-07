package com.wedcrm.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "webhook_configs")
public class WebhookConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String url;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(nullable = false)
    private boolean active = true;

    // Construtores
    public WebhookConfig() {}

    public WebhookConfig(String url, String eventType, boolean active) {
        this.url = url;
        this.eventType = eventType;
        this.active = active;
    }

    // Getters e Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
