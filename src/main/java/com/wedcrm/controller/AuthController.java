package com.wedcrm.controller;

import com.wedcrm.dto.auth.*;
import com.wedcrm.dto.request.*;
import com.wedcrm.dto.response.LoginResponseDTO;
import com.wedcrm.dto.response.TokenResponseDTO;
import com.wedcrm.service.AuthService;
import com.wedcrm.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Autenticação", description = "Endpoints para autenticação e gerenciamento de usuários")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Autenticar usuário", description = "Retorna access token e refresh token")
    public ResponseEntity<ApiResponse<LoginResponseDTO>> login(@Valid @RequestBody LoginRequestDTO request) {
        LoginResponseDTO response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/register")
    @Operation(summary = "Registrar novo usuário", description = "Cria uma nova conta de usuário (não administrador)")
    public ResponseEntity<ApiResponse<UserInfoDTO>> register(@Valid @RequestBody RegisterRequestDTO request) {
        UserInfoDTO response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Renovar access token", description = "Utiliza refresh token para gerar novo par de tokens")
    public ResponseEntity<ApiResponse<TokenResponseDTO>> refresh(@Valid @RequestBody RefreshTokenRequestDTO request) {
        TokenResponseDTO response = authService.refreshToken(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout", description = "Invalida o refresh token do usuário")
    public ResponseEntity<ApiResponse<Void>> logout(@AuthenticationPrincipal UserDetails userDetails) {
        authService.logout(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Solicitar reset de senha", description = "Envia e-mail com link para redefinição de senha")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequestDTO request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Redefinir senha", description = "Redefine a senha utilizando token enviado por e-mail")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequestDTO request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/verify-email/{token}")
    @Operation(summary = "Confirmar e-mail", description = "Valida o token de confirmação de e-mail")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(@PathVariable String token) {
        authService.verifyEmail(token);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/me")
    @Operation(summary = "Dados do usuário logado", description = "Retorna as informações do usuário autenticado")
    public ResponseEntity<ApiResponse<UserInfoDTO>> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        UserInfoDTO userInfo = authService.getCurrentUser(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(userInfo));
    }
}