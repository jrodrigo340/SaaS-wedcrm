package com.wedcrm.service.impl;

import com.wedcrm.dto.request.InteractionRequestDTO;
import com.wedcrm.dto.response.InteractionResponseDTO;
import com.wedcrm.entity.Customer;
import com.wedcrm.entity.Interaction;
import com.wedcrm.entity.User;
import com.wedcrm.enums.Direction;
import com.wedcrm.mapper.InteractionMapper;
import com.wedcrm.repository.CustomerRepository;
import com.wedcrm.repository.InteractionRepository;
import com.wedcrm.repository.UserRepository;
import com.wedcrm.service.InteractionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
public class InteractionServiceImpl implements InteractionService {

    private final InteractionRepository interactionRepository;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final InteractionMapper interactionMapper;

    public InteractionServiceImpl(InteractionRepository interactionRepository,
                                  CustomerRepository customerRepository,
                                  UserRepository userRepository,
                                  InteractionMapper interactionMapper) {
        this.interactionRepository = interactionRepository;
        this.customerRepository = customerRepository;
        this.userRepository = userRepository;
        this.interactionMapper = interactionMapper;
    }

    @Override
    public Page<InteractionResponseDTO> getCustomerInteractions(UUID customerId, Pageable pageable) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));
        return interactionRepository.findByCustomerOrderByCreatedAtDesc(customer, pageable)
                .map(interactionMapper::toResponseDTO);
    }

    @Override
    public InteractionResponseDTO createManualInteraction(InteractionRequestDTO request, UUID userId) {
        Customer customer = customerRepository.findById(request.customerId())
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        Interaction interaction = interactionMapper.toEntity(request);
        interaction.setCustomer(customer);
        interaction.setUser(user);
        interaction.setDirection(Direction.OUTBOUND);
        interaction.setIsAutomatic(false);
        interaction.setSentAt(LocalDateTime.now());
        interaction.setStatus(com.wedcrm.enums.InteractionStatus.SENT);
        interaction = interactionRepository.save(interaction);
        // atualiza último contato do cliente
        customer.setLastContactDate(LocalDateTime.now().toLocalDate());
        customerRepository.save(customer);
        return interactionMapper.toResponseDTO(interaction);
    }

    @Override
    public InteractionResponseDTO getInteractionById(UUID id) {
        Interaction interaction = interactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Interação não encontrada"));
        return interactionMapper.toResponseDTO(interaction);
    }

    @Override
    public void markAsRead(UUID id) {
        Interaction interaction = interactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Interação não encontrada"));
        interaction.markAsRead();
        interactionRepository.save(interaction);
    }
}
