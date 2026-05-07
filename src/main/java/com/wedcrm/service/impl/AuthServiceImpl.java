package com.wedcrm.service.impl;

import com.wedcrm.dto.auth.*;
import com.wedcrm.dto.request.*;
import com.wedcrm.dto.response.LoginResponseDTO;
import com.wedcrm.dto.response.TokenResponseDTO;
import com.wedcrm.entity.User;
import com.wedcrm.enums.Role;
import com.wedcrm.repository.UserRepository;
import com.wedcrm.security.JwtTokenProvider;
import com.wedcrm.service.AuthService;
import com.wedcrm.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Value("${wedcrm.frontend-url}")
    private String frontendUrl;

    // Cache para tokens (em produção usar Redis)
    private final Map<String, String> resetTokens = new HashMap<>();
    private final Map<String, String> verificationTokens = new HashMap<>();

    @Override
    public LoginResponseDTO login(LoginRequestDTO request) {
        // 1. Autentica o usuário
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 2. Busca o usuário no banco
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        // 3. Verifica se o usuário está ativo
        if (!user.getActive()) {
            throw new RuntimeException("Usuário inativo. Contate o administrador.");
        }

        // 4. Gera tokens
        String accessToken = tokenProvider.generateAccessToken(user.getId(), user.getEmail(), user.getRole().name());
        String refreshToken = tokenProvider.generateRefreshToken(user.getId(), user.getEmail());

        // 5. Salva refresh token e atualiza lastLogin
        user.setRefreshToken(refreshToken);
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        // 6. Constrói resposta
        UserInfoDTO userInfo = new UserInfoDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getPhone(),
                user.getAvatarUrl(),
                user.getEmailVerified()
        );

        return new LoginResponseDTO(
                accessToken,
                refreshToken,
                "Bearer",
                tokenProvider.getAccessTokenExpiration(),
                userInfo
        );
    }

    @Override
    public UserInfoDTO register(RegisterRequestDTO request) {
        // 1. Valida se e-mail já existe
        if (userRepository.existsByEmail(request.email())) {
            throw new RuntimeException("E-mail já cadastrado");
        }

        // 2. Valida se o papel é permitido (apenas ADMIN pode criar ADMIN via outro endpoint)
        if (request.role() == Role.ADMIN) {
            throw new RuntimeException("Não é possível criar um usuário ADMIN diretamente");
        }

        // 3. Cria novo usuário
        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(request.role());
        user.setPhone(request.phone());
        user.setEmailVerified(false);
        user.setActive(true);

        User savedUser = userRepository.save(user);

        // 4. Gera token de verificação
        String verificationToken = UUID.randomUUID().toString();
        verificationTokens.put(verificationToken, savedUser.getEmail());

        // 5. Envia e-mail de boas-vindas com link de verificação
        emailService.sendWelcomeEmail(savedUser, verificationToken);

        // 6. Retorna resposta
        return new UserInfoDTO(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getRole(),
                savedUser.getPhone(),
                savedUser.getAvatarUrl(),
                savedUser.getEmailVerified()
        );
    }

    @Override
    public TokenResponseDTO refreshToken(RefreshTokenRequestDTO request) {
        String refreshToken = request.refreshToken();

        // 1. Valida o token
        if (!tokenProvider.validateToken(refreshToken) || !tokenProvider.isRefreshToken(refreshToken)) {
            throw new RuntimeException("Refresh token inválido");
        }

        // 2. Busca usuário pelo refresh token
        User user = userRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("Refresh token inválido ou expirado"));

        // 3. Verifica se o usuário está ativo
        if (!user.getActive()) {
            throw new RuntimeException("Usuário inativo");
        }

        // 4. Gera novos tokens (token rotation)
        String newAccessToken = tokenProvider.generateAccessToken(user.getId(), user.getEmail(), user.getRole().name());
        String newRefreshToken = tokenProvider.generateRefreshToken(user.getId(), user.getEmail());

        // 5. Atualiza refresh token no banco (invalida o antigo)
        user.setRefreshToken(newRefreshToken);
        userRepository.save(user);

        // 6. Retorna resposta
        return new TokenResponseDTO(
                newAccessToken,
                newRefreshToken,
                "Bearer",
                tokenProvider.getAccessTokenExpiration()
        );
    }

    @Override
    public void logout(String userId) {
        User user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        // Invalida o refresh token
        user.setRefreshToken(null);
        userRepository.save(user);
    }

    @Override
    public void forgotPassword(ForgotPasswordRequestDTO request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("E-mail não encontrado"));

        // Gera token de reset
        String resetToken = UUID.randomUUID().toString();
        resetTokens.put(resetToken, user.getEmail());

        // Envia e-mail com link de reset (validade 1 hora)
        emailService.sendPasswordResetEmail(user, resetToken);
    }

    @Override
    public void resetPassword(ResetPasswordRequestDTO request) {
        // 1. Busca o email pelo token
        String email = resetTokens.get(request.token());
        if (email == null) {
            throw new RuntimeException("Token inválido ou expirado");
        }

        // 2. Busca o usuário
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        // 3. Atualiza senha com novo hash BCrypt
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        // 4. Remove token usado
        resetTokens.remove(request.token());

        // 5. Invalida todos os refresh tokens do usuário (força reautenticação)
        user.setRefreshToken(null);
        userRepository.save(user);
    }

    @Override
    public void verifyEmail(String token) {
        // 1. Busca o email pelo token
        String email = verificationTokens.get(token);
        if (email == null) {
            throw new RuntimeException("Token de verificação inválido ou expirado");
        }

        // 2. Busca o usuário
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        // 3. Confirma o e-mail
        user.setEmailVerified(true);
        userRepository.save(user);

        // 4. Remove token usado
        verificationTokens.remove(token);
    }

    @Override
    public UserInfoDTO getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        return new UserInfoDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getPhone(),
                user.getAvatarUrl(),
                user.getEmailVerified()
        );
    }
}0