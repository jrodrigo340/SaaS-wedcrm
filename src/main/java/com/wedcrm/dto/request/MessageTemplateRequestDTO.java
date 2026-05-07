package com.wedcrm.dto.request;

import com.wedcrm.enums.MessageChannel;
import com.wedcrm.enums.TemplateCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.util.List;

@Builder
public record MessageTemplateRequestDTO(
        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 200, message = "Nome deve ter no máximo 200 caracteres")
        String name,

        @Size(max = 500, message = "Descrição deve ter no máximo 500 caracteres")
        String description,

        @NotNull(message = "Canal é obrigatório")
        MessageChannel channel,

        @NotNull(message = "Categoria é obrigatória")
        TemplateCategory category,

        @Size(max = 300, message = "Assunto deve ter no máximo 300 caracteres")
        String subject, // apenas para EMAIL

        @NotBlank(message = "Corpo da mensagem é obrigatório")
        String body,

        List<String> variables, // opcional, será extraído automaticamente

        String language, // padrão "pt-BR"

        Boolean isActive
) {}
