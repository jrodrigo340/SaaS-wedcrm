package com.wedcrm.mapper;

import com.wedcrm.dto.request.MessageTemplateRequestDTO;
import com.wedcrm.dto.response.MessageTemplateResponseDTO;
import com.wedcrm.entity.MessageTemplate;
import org.springframework.stereotype.Component;

@Component
public class MessageTemplateMapper {

    public MessageTemplate toEntity(MessageTemplateRequestDTO dto) {
        MessageTemplate entity = new MessageTemplate();
        entity.setName(dto.name());
        entity.setDescription(dto.description());
        entity.setChannel(dto.channel());
        entity.setCategory(dto.category());
        entity.setSubject(dto.subject());
        entity.setBody(dto.body());
        entity.setLanguage(dto.language() != null ? dto.language() : "pt-BR");
        return entity;
    }

    public void updateEntity(MessageTemplateRequestDTO dto, MessageTemplate entity) {
        entity.setName(dto.name());
        entity.setDescription(dto.description());
        entity.setChannel(dto.channel());
        entity.setCategory(dto.category());
        entity.setSubject(dto.subject());
        entity.setBody(dto.body());
        entity.setLanguage(dto.language() != null ? dto.language() : entity.getLanguage());
    }

    public MessageTemplateResponseDTO toResponseDTO(MessageTemplate entity) {
        return MessageTemplateResponseDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .channel(entity.getChannel())
                .category(entity.getCategory())
                .subject(entity.getSubject())
                .body(entity.getBody())
                .variables(entity.getVariables())
                .isActive(entity.getIsActive())
                .language(entity.getLanguage())
                .usageCount(entity.getUsageCount())
                .createdAt(entity.getCreatedAt())
                .createdBy(entity.getCreatedBy())
                .build();
    }
}
