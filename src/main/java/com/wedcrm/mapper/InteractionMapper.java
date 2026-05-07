package com.wedcrm.mapper;

import com.wedcrm.dto.request.InteractionRequestDTO;
import com.wedcrm.dto.response.InteractionResponseDTO;
import com.wedcrm.entity.Interaction;
import com.wedcrm.enums.InteractionStatus;
import com.wedcrm.enums.InteractionType;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class InteractionMapper {

    public Interaction toEntity(InteractionRequestDTO dto) {
        if (dto == null) return null;
        Interaction interaction = new Interaction();
        interaction.setType(dto.type());
        interaction.setContent(dto.content());
        interaction.setSubject(dto.subject());
        interaction.setChannel(dto.channel());
        // customer, user, direction, sentAt serão definidos no service
        return interaction;
    }

    public InteractionResponseDTO toResponseDTO(Interaction entity) {
        if (entity == null) return null;
        return InteractionResponseDTO.builder()
                .id(entity.getId())
                .customerId(entity.getCustomer() != null ? entity.getCustomer().getId() : null)
                .customerName(entity.getCustomer() != null ? entity.getCustomer().getFullName() : null)
                .userId(entity.getUser() != null ? entity.getUser().getId() : null)
                .userName(entity.getUser() != null ? entity.getUser().getName() : null)
                .type(entity.getType())
                .typeIcon(entity.getTypeIcon())
                .direction(entity.getDirection())
                .subject(entity.getSubject())
                .content(entity.getContent())
                .contentSummary(entity.getContentSummary(100))
                .channel(entity.getChannel())
                .sentAt(entity.getSentAt())
                .readAt(entity.getReadAt())
                .status(entity.getStatus())
                .statusColor(entity.getStatusColor())
                .isAutomatic(entity.getIsAutomatic())
                .templateId(entity.getTemplateUsed() != null ? entity.getTemplateUsed().getId() : null)
                .timeAgo(entity.getTimeAgo())
                .build();
    }
}
