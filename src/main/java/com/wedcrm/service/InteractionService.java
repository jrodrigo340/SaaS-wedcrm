package com.wedcrm.service;

import com.wedcrm.dto.request.InteractionRequestDTO;
import com.wedcrm.dto.response.InteractionResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface InteractionService {
    Page<InteractionResponseDTO> getCustomerInteractions(UUID customerId, Pageable pageable);
    InteractionResponseDTO createManualInteraction(InteractionRequestDTO request, UUID userId);
    InteractionResponseDTO getInteractionById(UUID id);
    void markAsRead(UUID id);
}