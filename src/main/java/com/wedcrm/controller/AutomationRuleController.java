package com.wedcrm.controller;

import com.wedcrm.dto.filter.AutomationRuleFilterDTO;
import com.wedcrm.dto.request.AutomationRuleRequestDTO;
import com.wedcrm.dto.response.AutomationRuleResponseDTO;
import com.wedcrm.dto.Assistants.RuleExecutionHistoryDTO;
import com.wedcrm.service.AutomationRuleService;
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
@RequestMapping("/api/v1/automation-rules")
@Tag(name = "Automação", description = "Endpoints para gestão de regras de automação")
public class AutomationRuleController {

    @Autowired
    private AutomationRuleService ruleService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Listar regras", description = "Retorna lista paginada de regras de automação")
    public ResponseEntity<ApiResponse<Page<AutomationRuleResponseDTO>>> listRules(
            @ModelAttribute AutomationRuleFilterDTO filter,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<AutomationRuleResponseDTO> rules = ruleService.listRules(filter, pageable);
        return ResponseEntity.ok(ApiResponse.success(rules));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Criar regra", description = "Cadastra uma nova regra de automação")
    public ResponseEntity<ApiResponse<AutomationRuleResponseDTO>> createRule(
            @Valid @RequestBody AutomationRuleRequestDTO request) {
        AutomationRuleResponseDTO created = ruleService.createRule(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Regra criada com sucesso", created));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Buscar regra por ID", description = "Retorna detalhes da regra")
    public ResponseEntity<ApiResponse<AutomationRuleResponseDTO>> getRuleById(@PathVariable UUID id) {
        AutomationRuleResponseDTO rule = ruleService.getRuleById(id);
        return ResponseEntity.ok(ApiResponse.success(rule));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Atualizar regra", description = "Atualiza dados de uma regra existente")
    public ResponseEntity<ApiResponse<AutomationRuleResponseDTO>> updateRule(
            @PathVariable UUID id,
            @Valid @RequestBody AutomationRuleRequestDTO request) {
        AutomationRuleResponseDTO updated = ruleService.updateRule(id, request);
        return ResponseEntity.ok(ApiResponse.success("Regra atualizada com sucesso", updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Remover regra", description = "Remove uma regra (apenas ADMIN)")
    public ResponseEntity<ApiResponse<Void>> deleteRule(@PathVariable UUID id) {
        ruleService.deleteRule(id);
        return ResponseEntity.ok(ApiResponse.success("Regra removida com sucesso", null));
    }

    @PatchMapping("/{id}/toggle")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Ativar/Desativar regra", description = "Alterna o status ativo/inativo da regra")
    public ResponseEntity<ApiResponse<AutomationRuleResponseDTO>> toggleRule(@PathVariable UUID id) {
        AutomationRuleResponseDTO updated = ruleService.toggleRule(id);
        String message = updated.isActive() ? "Regra ativada" : "Regra desativada";
        return ResponseEntity.ok(ApiResponse.success(message, updated));
    }

    @GetMapping("/{id}/history")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Histórico de execuções", description = "Retorna o histórico de execuções da regra")
    public ResponseEntity<ApiResponse<List<RuleExecutionHistoryDTO>>> getRuleHistory(@PathVariable UUID id) {
        List<RuleExecutionHistoryDTO> history = ruleService.getRuleExecutionHistory(id);
        return ResponseEntity.ok(ApiResponse.success(history));
    }
}