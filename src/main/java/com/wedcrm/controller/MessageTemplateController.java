package com.wedcrm.controller;

import com.wedcrm.dto.*;
import com.wedcrm.dto.Assistants.PreviewMessageDTO;
import com.wedcrm.dto.filter.MessageTemplateFilterDTO;
import com.wedcrm.dto.request.MessageTemplateRequestDTO;
import com.wedcrm.dto.response.MessageTemplateResponseDTO;
import com.wedcrm.service.MessageTemplateService;
import com.wedcrm.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/templates")
@Tag(name = "Templates de Mensagem", description = "Endpoints para gestão de templates de mensagens")
public class MessageTemplateController {

    @Autowired
    private MessageTemplateService templateService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SALES', 'VIEWER')")
    @Operation(summary = "Listar templates", description = "Retorna lista paginada de templates com filtros")
    public ResponseEntity<ApiResponse<Page<MessageTemplateResponseDTO>>> listTemplates(
            @ModelAttribute MessageTemplateFilterDTO filter,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<MessageTemplateResponseDTO> templates = templateService.listTemplates(filter, pageable);
        return ResponseEntity.ok(ApiResponse.success(templates));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Criar template", description = "Cadastra um novo template de mensagem")
    public ResponseEntity<ApiResponse<MessageTemplateResponseDTO>> createTemplate(
            @Valid @RequestBody MessageTemplateRequestDTO request) {
        MessageTemplateResponseDTO created = templateService.createTemplate(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Template criado com sucesso", created));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SALES', 'VIEWER')")
    @Operation(summary = "Buscar template por ID", description = "Retorna detalhes do template")
    public ResponseEntity<ApiResponse<MessageTemplateResponseDTO>> getTemplateById(@PathVariable UUID id) {
        MessageTemplateResponseDTO template = templateService.getTemplateById(id);
        return ResponseEntity.ok(ApiResponse.success(template));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Atualizar template", description = "Atualiza dados de um template existente")
    public ResponseEntity<ApiResponse<MessageTemplateResponseDTO>> updateTemplate(
            @PathVariable UUID id,
            @Valid @RequestBody MessageTemplateRequestDTO request) {
        MessageTemplateResponseDTO updated = templateService.updateTemplate(id, request);
        return ResponseEntity.ok(ApiResponse.success("Template atualizado com sucesso", updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Remover template", description = "Remove um template (apenas ADMIN)")
    public ResponseEntity<ApiResponse<Void>> deleteTemplate(@PathVariable UUID id) {
        templateService.deleteTemplate(id);
        return ResponseEntity.ok(ApiResponse.success("Template removido com sucesso", null));
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Ativar template", description = "Ativa um template para uso")
    public ResponseEntity<ApiResponse<MessageTemplateResponseDTO>> activateTemplate(@PathVariable UUID id) {
        MessageTemplateResponseDTO updated = templateService.activateTemplate(id);
        return ResponseEntity.ok(ApiResponse.success("Template ativado", updated));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Desativar template", description = "Desativa um template")
    public ResponseEntity<ApiResponse<MessageTemplateResponseDTO>> deactivateTemplate(@PathVariable UUID id) {
        MessageTemplateResponseDTO updated = templateService.deactivateTemplate(id);
        return ResponseEntity.ok(ApiResponse.success("Template desativado", updated));
    }

    @PostMapping("/{id}/preview")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SALES')")
    @Operation(summary = "Pré-visualizar template", description = "Visualiza o template com dados de exemplo ou cliente específico")
    public ResponseEntity<ApiResponse<PreviewMessageDTO>> previewTemplate(
            @PathVariable UUID id,
            @RequestParam(required = false) UUID customerId) {
        PreviewMessageDTO preview = templateService.previewTemplate(id, customerId);
        return ResponseEntity.ok(ApiResponse.success(preview));
    }

    @PostMapping("/{id}/send/{customerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SALES')")
    @Operation(summary = "Enviar mensagem via template", description = "Envia mensagem usando o template para um cliente")
    public ResponseEntity<ApiResponse<Void>> sendTemplateToCustomer(
            @PathVariable UUID id,
            @PathVariable UUID customerId) {
        templateService.sendTemplateToCustomer(id, customerId);
        return ResponseEntity.ok(ApiResponse.success("Mensagem enviada com sucesso", null));
    }

    @GetMapping("/most-used")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Templates mais usados", description = "Retorna os templates com maior contagem de uso")
    public ResponseEntity<ApiResponse<List<MessageTemplateResponseDTO>>> getMostUsedTemplates() {
        List<MessageTemplateResponseDTO> templates = templateService.getMostUsedTemplates();
        return ResponseEntity.ok(ApiResponse.success(templates));
    }
}