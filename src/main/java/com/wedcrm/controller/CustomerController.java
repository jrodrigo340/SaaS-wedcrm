package com.wedcrm.controller;

import com.wedcrm.dto.Assistants.ImportResultDTO;
import com.wedcrm.dto.Assistants.TimelineEventDTO;
import com.wedcrm.dto.request.CustomerFilterDTO;
import com.wedcrm.dto.request.CustomerRequestDTO;
import com.wedcrm.dto.response.CustomerResponse;
import com.wedcrm.dto.response.CustomerSummaryResponse;
import com.wedcrm.service.CustomerService;
import com.wedcrm.util.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customers")
@Tag(name = "Clientes", description = "Endpoints para gestão de clientes/leads")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SALES', 'VIEWER')")
    @Operation(summary = "Listar clientes", description = "Retorna lista paginada de clientes com filtros")
    public ResponseEntity<ApiResponse<Page<CustomerSummaryResponse>>> listCustomers(
            @ModelAttribute CustomerFilterDTO filter,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal UserDetails userDetails) {

        // Se o usuário for SALES, força o filtro para seus próprios clientes
        if (userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_SALES"))) {
            UUID userId = getUserIdFromUserDetails(userDetails);
            filter.setAssignedToId(userId);
        }

        Page<CustomerSummaryResponse> customers = customerService.listCustomers(filter, pageable);
        return ResponseEntity.ok(ApiResponse.success(customers));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SALES')")
    @Operation(summary = "Criar cliente", description = "Cadastra um novo cliente/lead")
    public ResponseEntity<ApiResponse<CustomerResponse>> createCustomer(
            @Valid @RequestBody CustomerRequestDTO request) {
        CustomerResponseDTO created = customerService.createCustomer(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Cliente criado com sucesso", created));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SALES', 'VIEWER')")
    @Operation(summary = "Buscar cliente por ID", description = "Retorna cliente com todos os relacionamentos")
    public ResponseEntity<ApiResponse<CustomerResponse>> getCustomerById(@PathVariable UUID id) {
        CustomerResponse customer = customerService.getCustomerById(id);
        return ResponseEntity.ok(ApiResponse.success(customer));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SALES')")
    @Operation(summary = "Atualizar cliente", description = "Atualiza dados de um cliente existente")
    public ResponseEntity<ApiResponse<CustomerResponseDTO>> updateCustomer(
            @PathVariable UUID id,
            @Valid @RequestBody CustomerRequestDTO request) {
        CustomerResponse updated = customerService.updateCustomer(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cliente atualizado com sucesso", updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Soft delete cliente", description = "Desativa o cliente (active = false)")
    public ResponseEntity<ApiResponse<Void>> deleteCustomer(@PathVariable UUID id) {
        customerService.deleteCustomer(id);
        return ResponseEntity.ok(ApiResponse.success("Cliente removido com sucesso", null));
    }

    @PatchMapping("/{id}/assign/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Atribuir cliente a vendedor", description = "Transfere cliente para outro vendedor")
    public ResponseEntity<ApiResponse<CustomerResponseDTO>> assignCustomer(
            @PathVariable UUID id,
            @PathVariable UUID userId) {
        CustomerResponse updated = customerService.assignCustomer(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Cliente atribuído com sucesso", updated));
    }

    @PostMapping("/{id}/tags/{tagId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SALES')")
    @Operation(summary = "Adicionar tag", description = "Adiciona uma tag ao cliente")
    public ResponseEntity<ApiResponse<CustomerResponse>> addTag(
            @PathVariable UUID id,
            @PathVariable UUID tagId) {
        CustomerResponseDTO updated = customerService.addTag(id, tagId);
        return ResponseEntity.ok(ApiResponse.success("Tag adicionada com sucesso", updated));
    }

    @DeleteMapping("/{id}/tags/{tagId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SALES')")
    @Operation(summary = "Remover tag", description = "Remove uma tag do cliente")
    public ResponseEntity<ApiResponse<CustomerResponse>> removeTag(
            @PathVariable UUID id,
            @PathVariable UUID tagId) {
        CustomerResponseDTO updated = customerService.removeTag(id, tagId);
        return ResponseEntity.ok(ApiResponse.success("Tag removida com sucesso", updated));
    }

    @GetMapping("/{id}/timeline")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SALES', 'VIEWER')")
    @Operation(summary = "Timeline do cliente", description = "Retorna histórico de interações e atividades")
    public ResponseEntity<ApiResponse<java.util.List<TimelineEventDTO>>> getCustomerTimeline(@PathVariable UUID id) {
        java.util.List<TimelineEventDTO> timeline = customerService.getCustomerTimeline(id);
        return ResponseEntity.ok(ApiResponse.success(timeline));
    }

    @PostMapping("/import")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Importar clientes via CSV", description = "Importa em lote a partir de arquivo CSV")
    public ResponseEntity<ApiResponse<ImportResultDTO>> importCustomers(@RequestParam("file") MultipartFile file) {
        ImportResultDTO result = customerService.importCustomers(file);
        if (result.errors().isEmpty()) {
            return ResponseEntity.ok(ApiResponse.success("Importação concluída com sucesso", result));
        } else {
            return ResponseEntity.status(HttpStatus.MULTI_STATUS)
                    .body(ApiResponse.success("Importação concluída com alguns erros", result));
        }
    }

    @GetMapping("/export")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Exportar clientes", description = "Exporta clientes filtrados para CSV")
    public ResponseEntity<byte[]> exportCustomers(@ModelAttribute CustomerFilterDTO filter) {
        byte[] csvData = customerService.exportCustomers(filter);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=customers.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csvData);
    }

    @GetMapping("/{id}/score")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SALES')")
    @Operation(summary = "Calcular lead score", description = "Recalcula e retorna o score do cliente")
    public ResponseEntity<ApiResponse<Integer>> recalculateScore(@PathVariable UUID id) {
        CustomerResponseDTO customer = customerService.recalculateScore(id);
        return ResponseEntity.ok(ApiResponse.success(customer.score()));
    }

    // Método auxiliar para extrair o ID do usuário do token JWT
    private UUID getUserIdFromUserDetails(UserDetails userDetails) {
        // Implementação conforme sua estratégia (ex: userDetails.getUsername() retorna o ID)
        return UUID.fromString(userDetails.getUsername());
    }
}