package com.wedcrm.controller;

public class InteractionController {

    @GetMapping("/customer/{customerId}")
    Page<InteractionDTO> getCustomerInteractions(UUID customerId, Pageable pageable)

    @PostMapping
    InteractionDTO createInteraction(InteractionRequest)

    @GetMapping("/{id}")
    InteractionDTO getInteraction(UUID id)

    @DeleteMapping("/{id}")
    void deleteInteraction(UUID id)

}
