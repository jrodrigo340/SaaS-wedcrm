package com.wedcrm.controller;

import org.springframework.boot.data.autoconfigure.web.DataWebProperties;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import java.util.UUID;
import com.wedcrm.dto.request.InteractionRequestDTO;
import com.wedcrm.dto.response.InteractionResponseDTO;
import com.wedcrm.service.InteractionService;
import com.wedcrm.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/interactions")
@Tag(name = "Interações", description = "Histórico de comunicação com clientes")
public class InteractionController {

    @Autowired
    private InteractionService interactionService;

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SALES', 'VIEWER')")
    @Operation(summary = "Histórico de interações do cliente")
    public ResponseEntity<ApiResponse<Page<InteractionResponseDTO>>> getCustomerInteractions(
            @PathVariable UUID customerId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<InteractionResponseDTO> interactions = interactionService.getCustomerInteractions(customerId, pageable);
        return ResponseEntity.ok(ApiResponse.success(interactions));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SALES')")
    @Operation(summary = "Registrar interação manual")
    public ResponseEntity<ApiResponse<InteractionResponseDTO>> createInteraction(
            @Valid @RequestBody InteractionRequestDTO request,
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        InteractionResponseDTO created = interactionService.createManualInteraction(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Interação registrada", created));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SALES', 'VIEWER')")
    @Operation(summary = "Buscar interação por ID")
    public ResponseEntity<ApiResponse<InteractionResponseDTO>> getInteraction(@PathVariable UUID id) {
        InteractionResponseDTO interaction = interactionService.getInteractionById(id);
        return ResponseEntity.ok(ApiResponse.success(interaction));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Remover interação", description = "Apenas ADMIN pode remover")
    public ResponseEntity<ApiResponse<Void>> deleteInteraction(@PathVariable UUID id) {
        interactionService.deleteInteraction(id);
        return ResponseEntity.ok(ApiResponse.success("Interação removida", null));
    }

}
