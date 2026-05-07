package com.wedcrm.service;

import com.wedcrm.dto.Assistants.PreviewMessageDTO;
import com.wedcrm.dto.filter.MessageTemplateFilterDTO;
import com.wedcrm.dto.request.MessageTemplateRequestDTO;
import com.wedcrm.dto.response.MessageTemplateResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface MessageTemplateService {

    MessageTemplateResponseDTO createTemplate(MessageTemplateRequestDTO request);

    MessageTemplateResponseDTO updateTemplate(UUID id, MessageTemplateRequestDTO request);

    MessageTemplateResponseDTO getTemplateById(UUID id);

    Page<MessageTemplateResponseDTO> listTemplates(MessageTemplateFilterDTO filter, Pageable pageable);

    void deleteTemplate(UUID id);

    MessageTemplateResponseDTO activateTemplate(UUID id);

    MessageTemplateResponseDTO deactivateTemplate(UUID id);

    PreviewMessageDTO previewTemplate(UUID id, UUID customerId);

    void sendTemplateToCustomer(UUID templateId, UUID customerId);

    List<MessageTemplateResponseDTO> getMostUsedTemplates();
}
