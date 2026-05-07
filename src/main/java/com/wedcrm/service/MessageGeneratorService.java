package com.wedcrm.service;

import com.wedcrm.dto.Assistants.PreviewMessageDTO;
import com.wedcrm.enums.AutomationTrigger;
import com.wedcrm.enums.MessageChannel;

import java.time.LocalDateTime;
import java.util.UUID;

public interface MessageGeneratorService {

    String generateMessage(UUID templateId, UUID customerId);

    void sendMessage(UUID customerId, UUID templateId, MessageChannel channel);

    void processAutomationTrigger(AutomationTrigger trigger, UUID customerId);

    String resolveVariables(String template, com.wedcrm.entity.Customer customer);

    void scheduleMessage(UUID customerId, UUID templateId, LocalDateTime scheduledTime);

    void processScheduledMessages();

    void sendBirthdayMessages();

    void sendReengagementMessages();

    PreviewMessageDTO previewMessage(UUID templateId, UUID customerId);
}