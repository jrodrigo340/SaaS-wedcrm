package com.wedcrm.dto.response;

public record TokenResponseDTO(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn
) {}
