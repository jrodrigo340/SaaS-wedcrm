package com.wedcrm.service;

import com.wedcrm.dto.auth.*;

public interface AuthService {

    AuthResponse login(LoginRequest request);

    void register(RegisterRequest request);

    TokenResponse refreshToken(String refreshToken);

    void logout(String userId);

    void forgotPassword(String email);

    void resetPassword(ResetPasswordRequest request);

    void verifyEmail(String token);

}
