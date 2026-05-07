package com.wedcrm.service;

import com.wedcrm.dto.Assistants.WebhookConfigDTO;
import com.wedcrm.dto.request.WebhookConfigRequestDTO;

import java.util.List;
import java.util.UUID;

public interface WebhookService {
    void processIncomingWebhook(String secret, String payload);
    List<WebhookConfigDTO> listOutgoingWebhooks();
    WebhookConfigDTO registerOutgoingWebhook(WebhookConfigRequestDTO request);
    void removeOutgoingWebhook(UUID id);
    void dispatchWebhook(String eventType, Object payload);
}
