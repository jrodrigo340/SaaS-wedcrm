package com.wedcrm.service;

import com.wedcrm.dto.auth.*;

public interface AuthService {

    LoginResponseDTO login(LoginRequestDTO request);

    UserInfoDTO register(RegisterRequestDTO request);

    TokenResponseDTO refreshToken(RefreshTokenRequestDTO request);

    void logout(String userId);

    void forgotPassword(ForgotPasswordRequestDTO request);

    void resetPassword(ResetPasswordRequestDTO request);

    void verifyEmail(String token);

    UserInfoDTO getCurrentUser(String email);
}