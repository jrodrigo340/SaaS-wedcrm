package com.wedcrm.controller;

public class WebhookController {@PostMapping("/incoming")
void receiveWebhook(@RequestBody Map<String, Object> payload)

    @PostMapping("/outgoing")
    void triggerWebhook(WebhookRequest request)

    @GetMapping
    List<WebhookDTO> listWebhooks()

    @DeleteMapping("/{id}")
    void deleteWebhook(UUID id)



}
