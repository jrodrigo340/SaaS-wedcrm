package com.wedcrm.controller;

import com.wedcrm.dto.*;
import com.wedcrm.dto.Assistants.DateRangeDTO;
import com.wedcrm.dto.Assistants.KanbanBoardDTO;
import com.wedcrm.dto.Assistants.PipelineMetricsDTO;
import com.wedcrm.dto.filter.OpportunityFilterDTO;
import com.wedcrm.dto.request.CloseRequestDTO;
import com.wedcrm.dto.request.LostRequestDTO;
import com.wedcrm.dto.request.OpportunityProductRequestDTO;
import com.wedcrm.dto.request.OpportunityRequestDTO;
import com.wedcrm.dto.response.OpportunityProductResponseDTO;
import com.wedcrm.dto.response.OpportunityResponseDTO;
import com.wedcrm.service.OpportunityService;
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
@RequestMapping("/api/v1/opportunities")
@Tag(name = "Oportunidades", description = "Endpoints para gestão do pipeline de vendas")
public class OpportunityController {

    @Autowired
    private OpportunityService opportunityService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SALES', 'VIEWER')")
    @Operation(summary = "Listar oportunidades", description = "Retorna lista paginada de oportunidades com filtros")
    public ResponseEntity<ApiResponse<Page<OpportunityResponseDTO>>> listOpportunities(
            @ModelAttribute OpportunityFilterDTO filter,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<OpportunityResponseDTO> opportunities = opportunityService.listOpportunities(filter, pageable);
        return ResponseEntity.ok(ApiResponse.success(opportunities));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SALES')")
    @Operation(summary = "Criar oportunidade", description = "Cadastra uma nova oportunidade de venda")
    public ResponseEntity<ApiResponse<OpportunityResponseDTO>> createOpportunity(
            @Valid @RequestBody OpportunityRequestDTO request) {
        OpportunityResponseDTO created = opportunityService.createOpportunity(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Oportunidade criada com sucesso", created));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SALES', 'VIEWER')")
    @Operation(summary = "Buscar oportunidade por ID", description = "Retorna detalhes completos da oportunidade")
    public ResponseEntity<ApiResponse<OpportunityResponseDTO>> getOpportunityById(@PathVariable UUID id) {
        OpportunityResponseDTO opportunity = opportunityService.getOpportunityById(id);
        return ResponseEntity.ok(ApiResponse.success(opportunity));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SALES')")
    @Operation(summary = "Atualizar oportunidade", description = "Atualiza dados de uma oportunidade existente (apenas se aberta)")
    public ResponseEntity<ApiResponse<OpportunityResponseDTO>> updateOpportunity(
            @PathVariable UUID id,
            @Valid @RequestBody OpportunityRequestDTO request) {
        OpportunityResponseDTO updated = opportunityService.updateOpportunity(id, request);
        return ResponseEntity.ok(ApiResponse.success("Oportunidade atualizada com sucesso", updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Remover oportunidade", description = "Remove uma oportunidade (apenas se não foi ganha)")
    public ResponseEntity<ApiResponse<Void>> deleteOpportunity(@PathVariable UUID id) {
        opportunityService.deleteOpportunity(id);
        return ResponseEntity.ok(ApiResponse.success("Oportunidade removida com sucesso", null));
    }

    @PatchMapping("/{id}/stage/{stageId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SALES')")
    @Operation(summary = "Mover estágio", description = "Move a oportunidade para outro estágio do pipeline")
    public ResponseEntity<ApiResponse<OpportunityResponseDTO>> moveStage(
            @PathVariable UUID id,
            @PathVariable UUID stageId) {
        OpportunityResponseDTO updated = opportunityService.moveStage(id, stageId);
        return ResponseEntity.ok(ApiResponse.success("Estágio atualizado com sucesso", updated));
    }

    @PatchMapping("/{id}/won")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SALES')")
    @Operation(summary = "Encerrar como ganha", description = "Marca a oportunidade como WON e atualiza o cliente")
    public ResponseEntity<ApiResponse<OpportunityResponseDTO>> closeAsWon(
            @PathVariable UUID id,
            @RequestBody(required = false) CloseRequestDTO request) {
        OpportunityResponseDTO updated = opportunityService.closeAsWon(id, request);
        return ResponseEntity.ok(ApiResponse.success("Oportunidade encerrada como GANHA", updated));
    }

    @PatchMapping("/{id}/lost")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SALES')")
    @Operation(summary = "Encerrar como perdida", description = "Marca a oportunidade como LOST com motivo")
    public ResponseEntity<ApiResponse<OpportunityResponseDTO>> closeAsLost(
            @PathVariable UUID id,
            @Valid @RequestBody LostRequestDTO request) {
        OpportunityResponseDTO updated = opportunityService.closeAsLost(id, request);
        return ResponseEntity.ok(ApiResponse.success("Oportunidade encerrada como PERDIDA", updated));
    }

    @PostMapping("/{id}/products")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SALES')")
    @Operation(summary = "Adicionar produto", description = "Adiciona um produto à oportunidade")
    public ResponseEntity<ApiResponse<OpportunityProductResponseDTO>> addProduct(
            @PathVariable UUID id,
            @Valid @RequestBody OpportunityProductRequestDTO request) {
        OpportunityProductResponseDTO product = opportunityService.addProduct(id, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Produto adicionado com sucesso", product));
    }

    @DeleteMapping("/{id}/products/{productId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SALES')")
    @Operation(summary = "Remover produto", description = "Remove um produto da oportunidade")
    public ResponseEntity<ApiResponse<Void>> removeProduct(
            @PathVariable UUID id,
            @PathVariable UUID productId) {
        opportunityService.removeProduct(id, productId);
        return ResponseEntity.ok(ApiResponse.success("Produto removido com sucesso", null));
    }

    @GetMapping("/kanban")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SALES', 'VIEWER')")
    @Operation(summary = "Kanban", description = "Retorna oportunidades agrupadas por estágio para exibição em Kanban")
    public ResponseEntity<ApiResponse<KanbanBoardDTO>> getKanbanBoard() {
        // Obtém o ID do usuário a partir do contexto de segurança (pode ser injetado via Authentication)
        // Para simplificar, você pode passar o userId via parâmetro ou obter do token JWT.
        // Sugestão: receber userId como @RequestHeader ou @AuthenticationPrincipal
        // Vamos assumir que o método getPipelineKanban recebe um userId.
        // Se não tiver userId, considere chamar um método que retorna todos os dados para ADMIN/MANAGER.
        // Implementação flexível: obter userId do SecurityContext.
        UUID userId = getCurrentUserId(); // método auxiliar
        KanbanBoardDTO kanban = opportunityService.getPipelineKanban(userId);
        return ResponseEntity.ok(ApiResponse.success(kanban));
    }

    @GetMapping("/metrics")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Métricas do pipeline", description = "Retorna métricas como total em aberto, conversão, etc.")
    public ResponseEntity<ApiResponse<PipelineMetricsDTO>> getPipelineMetrics(
            @ModelAttribute DateRangeDTO dateRange) {
        PipelineMetricsDTO metrics = opportunityService.getPipelineMetrics(dateRange);
        return ResponseEntity.ok(ApiResponse.success(metrics));
    }

    // Método auxiliar para obter o userId do token (exemplo simples)
    private UUID getCurrentUserId() {
        // Implementar com SecurityContextHolder
        // return UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName());
        // Por enquanto, retornamos null ou um valor padrão; você deve adaptar conforme sua implementação.
        return null;
    }
}