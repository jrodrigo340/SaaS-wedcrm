package com.wedcrm.controller;

import com.wedcrm.dto.request.WebhookConfigRequestDTO;
import com.wedcrm.dto.Assistants.WebhookConfigDTO;
import com.wedcrm.service.WebhookService;
import com.wedcrm.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/webhooks")
@Tag(name = "Webhooks", description = "Recepção e disparo de webhooks")
public class WebhookController {

    @Autowired
    private WebhookService webhookService;

    @PostMapping("/incoming")
    @Operation(summary = "Receber webhook externo", description = "Endpoint público para receber eventos de sistemas externos")
    public ResponseEntity<ApiResponse<Void>> receiveWebhook(
            @RequestHeader("X-Webhook-Secret") String secret,
            @RequestBody Map<String, Object> payload) {
        webhookService.processIncomingWebhook(secret, payload);
        return ResponseEntity.ok(ApiResponse.success("Webhook processado", null));
    }

    @PostMapping("/outgoing")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Disparar webhook manualmente", description = "Envia um webhook para testes")
    public ResponseEntity<ApiResponse<Void>> triggerWebhook(@Valid @RequestBody WebhookConfigRequestDTO request) {
        webhookService.dispatchWebhook(request.eventType(), request);
        return ResponseEntity.ok(ApiResponse.success("Webhook disparado", null));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar webhooks configurados", description = "Retorna todos os webhooks de saída cadastrados")
    public ResponseEntity<ApiResponse<List<WebhookConfigDTO>>> listWebhooks() {
        List<WebhookConfigDTO> webhooks = webhookService.listOutgoingWebhooks();
        return ResponseEntity.ok(ApiResponse.success(webhooks));
    }

    @PostMapping("/register")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Registrar webhook", description = "Cadastra um novo webhook de saída")
    public ResponseEntity<ApiResponse<WebhookConfigDTO>> registerWebhook(@Valid @RequestBody WebhookConfigRequestDTO request) {
        WebhookConfigDTO created = webhookService.registerOutgoingWebhook(request);
        return ResponseEntity.ok(ApiResponse.success("Webhook registrado", created));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Remover webhook", description = "Remove um webhook de saída")
    public ResponseEntity<ApiResponse<Void>> deleteWebhook(@PathVariable UUID id) {
        webhookService.removeOutgoingWebhook(id);
        return ResponseEntity.ok(ApiResponse.success("Webhook removido", null));
    }
}