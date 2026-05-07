package com.wedcrm.controller;

import com.wedcrm.dto.request.PipelineStageRequestDTO;
import com.wedcrm.dto.response.PipelineStageResponseDTO;
import com.wedcrm.dto.request.StageOrderRequestDTO;
import com.wedcrm.service.PipelineStageService;
import com.wedcrm.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pipeline-stages")
@Tag(name = "Estágios do Pipeline", description = "Configuração do funil de vendas")
public class PipelineStageController {

    @Autowired
    private PipelineStageService stageService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SALES', 'VIEWER')")
    @Operation(summary = "Listar estágios", description = "Retorna todos os estágios ordenados")
    public ResponseEntity<ApiResponse<List<PipelineStageResponseDTO>>> listStages() {
        List<PipelineStageResponseDTO> stages = stageService.getAllStages();
        return ResponseEntity.ok(ApiResponse.success(stages));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Criar estágio", description = "Cadastra um novo estágio no pipeline")
    public ResponseEntity<ApiResponse<PipelineStageResponseDTO>> createStage(@Valid @RequestBody PipelineStageRequestDTO request) {
        PipelineStageResponseDTO created = stageService.createStage(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Estágio criado", created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Atualizar estágio", description = "Atualiza dados de um estágio existente")
    public ResponseEntity<ApiResponse<PipelineStageResponseDTO>> updateStage(
            @PathVariable UUID id,
            @Valid @RequestBody PipelineStageRequestDTO request) {
        PipelineStageResponseDTO updated = stageService.updateStage(id, request);
        return ResponseEntity.ok(ApiResponse.success("Estágio atualizado", updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Remover estágio", description = "Remove um estágio (apenas se não tiver oportunidades)")
    public ResponseEntity<ApiResponse<Void>> deleteStage(@PathVariable UUID id) {
        stageService.deleteStage(id);
        return ResponseEntity.ok(ApiResponse.success("Estágio removido", null));
    }

    @PatchMapping("/reorder")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Reordenar estágios", description = "Move um estágio para nova posição na ordem")
    public ResponseEntity<ApiResponse<Void>> reorderStages(@Valid @RequestBody StageOrderRequestDTO request) {
        stageService.reorderStage(request.stageId(), request.newOrder());
        return ResponseEntity.ok(ApiResponse.success("Ordem atualizada", null));
    }
}