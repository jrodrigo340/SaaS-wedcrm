package com.wedcrm.controller;

import com.wedcrm.dto.filter.UserFilterDTO;
import com.wedcrm.dto.request.UserRequestDTO;
import com.wedcrm.dto.response.UserResponseDTO;
import com.wedcrm.dto.dashboard.UserSummaryDTO;
import com.wedcrm.service.UserService;
import com.wedcrm.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Usuários", description = "Gestão de usuários do sistema")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Listar usuários", description = "Retorna lista paginada de usuários com filtros")
    public ResponseEntity<ApiResponse<Page<UserSummaryDTO>>> listUsers(
            @ModelAttribute UserFilterDTO filter,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(userService.listUsers(filter, pageable)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Criar usuário", description = "Cria um novo usuário (apenas ADMIN)")
    public ResponseEntity<ApiResponse<UserResponseDTO>> createUser(@Valid @RequestBody UserRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Usuário criado", userService.createUser(request)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Buscar usuário por ID")
    public ResponseEntity<ApiResponse<UserResponseDTO>> getUserById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(userService.getUserById(id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Atualizar usuário")
    public ResponseEntity<ApiResponse<UserResponseDTO>> updateUser(@PathVariable UUID id, @Valid @RequestBody UserRequestDTO request) {
        return ResponseEntity.ok(ApiResponse.success("Usuário atualizado", userService.updateUser(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Remover usuário", description = "Soft delete (apenas ADMIN)")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("Usuário removido", null));
    }

    @PatchMapping("/{id}/password")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Alterar senha")
    public ResponseEntity<ApiResponse<Void>> changePassword(@PathVariable UUID id, @RequestParam String newPassword) {
        userService.changePassword(id, newPassword);
        return ResponseEntity.ok(ApiResponse.success("Senha alterada", null));
    }

    @PostMapping("/avatar")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Upload de avatar")
    public ResponseEntity<ApiResponse<String>> uploadAvatar(@RequestParam MultipartFile file, @AuthenticationPrincipal UserDetails userDetails) {
        String avatarUrl = userService.uploadAvatar(userDetails.getUsername(), file);
        return ResponseEntity.ok(ApiResponse.success("Avatar enviado", avatarUrl));
    }
}