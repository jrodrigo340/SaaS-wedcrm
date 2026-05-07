package com.wedcrm.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wedcrm.dto.Assistants.WebhookConfigDTO;
import com.wedcrm.dto.request.WebhookConfigRequestDTO;
import com.wedcrm.entity.WebhookConfig;
import com.wedcrm.repository.WebhookConfigRepository;
import com.wedcrm.service.WebhookService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class WebhookServiceImpl implements WebhookService {

    private final WebhookConfigRepository webhookConfigRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${wedcrm.webhook.secret}")
    private String webhookSecret;

    public WebhookServiceImpl(WebhookConfigRepository webhookConfigRepository,
                              RestTemplate restTemplate,
                              ObjectMapper objectMapper) {
        this.webhookConfigRepository = webhookConfigRepository;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void processIncomingWebhook(String secret, String payload) {
        if (!webhookSecret.equals(secret)) {
            throw new SecurityException("Secret inválido");
        }
        // Processar payload (ex: atualizar status de pagamento, etc.)
        System.out.println("Webhook recebido: " + payload);
    }

    @Override
    public List<WebhookConfigDTO> listOutgoingWebhooks() {
        return webhookConfigRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public WebhookConfigDTO registerOutgoingWebhook(WebhookConfigRequestDTO request) {
        WebhookConfig config = new WebhookConfig();
        config.setUrl(request.url());
        config.setEventType(request.eventType());
        config.setActive(true);
        config = webhookConfigRepository.save(config);
        return toDTO(config);
    }

    @Override
    public void removeOutgoingWebhook(UUID id) {
        webhookConfigRepository.deleteById(id);
    }

    @Override
    public void dispatchWebhook(String eventType, Object payload) {
        List<WebhookConfig> configs = webhookConfigRepository.findByEventTypeAndActiveTrue(eventType);
        for (WebhookConfig config : configs) {
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(payload), headers);
                restTemplate.postForEntity(config.getUrl(), entity, String.class);
            } catch (Exception e) {
                // log error
                e.printStackTrace();
            }
        }
    }

    private WebhookConfigDTO toDTO(WebhookConfig config) {
        return new WebhookConfigDTO(config.getId(), config.getUrl(), config.getEventType(), config.getActive());
    }
}
