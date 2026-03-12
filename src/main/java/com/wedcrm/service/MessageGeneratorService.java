package com.wedcrm.service;

import java.time.LocalDateTime;
import java.util.UUID;

public interface MessageGeneratorService {

    String generateMessage(UUID templateId, UUID customerId);

    void sendMessage(UUID customerId, UUID templateId, MessageChannel channel);

    void processAutomationTrigger(AutomationTrigger trigger, UUID customerId);

    String resolveVariables(String template, Customer customer);

    void scheduleMessage(UUID customerId, UUID templateId, LocalDateTime dateTime);

    void processScheduledMessages();

    void sendBirthdayMessages();

    void sendReengagementMessages();

    String previewMessage(UUID templateId, UUID customerId);

}
