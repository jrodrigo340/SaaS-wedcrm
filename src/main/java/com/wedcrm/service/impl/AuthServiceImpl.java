package com.wedcrm.service.impl;

import com.wedcrm.service.AuthService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    @Override
    public AuthResponse login(LoginRequest request) {
        // validar credenciais
        // gerar JWT
        // gerar refresh token
        // atualizar lastLogin
        return null;
    }

    @Override
    public void register(RegisterRequest request) {
        // validar email único
        // criptografar senha com BCrypt
        // salvar usuário
        // enviar email de boas vindas
    }

    @Override
    public TokenResponse refreshToken(String refreshToken) {
        // validar refresh token
        // gerar novo access token
        // gerar novo refresh token
        return null;
    }

    @Override
    public void logout(String userId) {
        // invalidar refresh token no banco
    }

    @Override
    public void forgotPassword(String email) {
        // gerar token de reset
        // salvar no banco
        // enviar email com link
    }

    @Override
    public void resetPassword(ResetPasswordRequest request) {
        // validar token
        // atualizar senha com BCrypt
    }

    @Override
    public void verifyEmail(String token) {
        // validar token de verificação
        // marcar email como verificado
    }
}