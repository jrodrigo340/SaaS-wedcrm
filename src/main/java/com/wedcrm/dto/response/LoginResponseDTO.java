package com.wedcrm.dto.response;

public record LoginResponseDTO(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn,
        UserInfoDTO user
) {}
