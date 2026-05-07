package com.wedcrm.service.impl;

import com.wedcrm.entity.*;
import com.wedcrm.enums.AutomationTrigger;
import com.wedcrm.enums.OpportunityStatus;
import com.wedcrm.repository.*;
import com.wedcrm.service.AutomationRuleService;
import com.wedcrm.service.NotificationService;
import com.wedcrm.service.OpportunityService;
import com.wedcrm.specification.OpportunitySpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class OpportunityServiceImpl implements OpportunityService {

    @Autowired
    private OpportunityRepository opportunityRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private PipelineStageRepository stageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OpportunityProductRepository opportunityProductRepository;

    @Autowired
    private AutomationRuleService automationService;

    @Autowired
    private NotificationService notificationService;

    // ========== CRUD BÁSICO ==========

    @Override
    public OpportunityResponseDTO createOpportunity(OpportunityRequestDTO request) {
        // 1. Busca cliente
        Customer customer = customerRepository.findById(request.customerId())
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        // 2. Busca estágio inicial
        PipelineStage stage = stageRepository.findById(request.stageId())
                .orElseThrow(() -> new RuntimeException("Estágio não encontrado"));

        // 3. Busca vendedor responsável
        User assignedTo = null;
        if (request.assignedToId() != null) {
            assignedTo = userRepository.findById(request.assignedToId())
                    .orElseThrow(() -> new RuntimeException("Vendedor não encontrado"));
        }

        // 4. Cria oportunidade
        Opportunity opportunity = new Opportunity();
        opportunity.setTitle(request.title());
        opportunity.setCustomer(customer);
        opportunity.setStage(stage);
        opportunity.setAssignedTo(assignedTo);
        opportunity.setValue(request.value() != null ? request.value() : BigDecimal.ZERO);
        opportunity.setProbability(request.probability() != null ? request.probability() : stage.getProbability());
        opportunity.setExpectedCloseDate(request.expectedCloseDate());
        opportunity.setStatus(OpportunityStatus.OPEN);
        opportunity.setNotes(request.notes());

        Opportunity savedOpportunity = opportunityRepository.save(opportunity);

        // 5. Adiciona produtos
        if (request.products() != null && !request.products().isEmpty()) {
            for (OpportunityProductRequestDTO productRequest : request.products()) {
                addProduct(savedOpportunity.getId(), productRequest);
            }
        }

        // 6. Dispara automação DEAL_CREATED
        automationService.processTrigger(AutomationTrigger.DEAL_CREATED, savedOpportunity);

        // 7. Notifica o vendedor responsável
        if (assignedTo != null) {
            notificationService.createNotification(
                    assignedTo.getId(),
                    "Nova Oportunidade Criada",
                    "Uma nova oportunidade foi criada: " + opportunity.getTitle(),
                    com.wedcrm.enums.NotificationType.OPPORTUNITY_STAGE_CHANGED,
                    savedOpportunity.getId(),
                    "Opportunity"
            );
        }

        return toResponseDTO(savedOpportunity);
    }

    @Override
    public OpportunityResponseDTO updateOpportunity(UUID id, OpportunityRequestDTO request) {
        Opportunity opportunity = opportunityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Oportunidade não encontrada"));

        // Verifica se pode editar (apenas se estiver aberta)
        if (opportunity.getStatus() != OpportunityStatus.OPEN) {
            throw new IllegalStateException("Não é possível editar uma oportunidade fechada");
        }

        // Atualiza dados
        opportunity.setTitle(request.title());

        if (request.stageId() != null && !request.stageId().equals(opportunity.getStage().getId())) {
            PipelineStage newStage = stageRepository.findById(request.stageId())
                    .orElseThrow(() -> new RuntimeException("Estágio não encontrado"));
            opportunity.setStage(newStage);
            opportunity.setProbability(newStage.getProbability());
        }

        if (request.assignedToId() != null) {
            User assignedTo = userRepository.findById(request.assignedToId())
                    .orElseThrow(() -> new RuntimeException("Vendedor não encontrado"));
            opportunity.setAssignedTo(assignedTo);
        }

        opportunity.setValue(request.value());
        opportunity.setExpectedCloseDate(request.expectedCloseDate());
        opportunity.setNotes(request.notes());

        Opportunity savedOpportunity = opportunityRepository.save(opportunity);
        return toResponseDTO(savedOpportunity);
    }

    @Override
    public OpportunityResponseDTO getOpportunityById(UUID id) {
        Opportunity opportunity = opportunityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Oportunidade não encontrada"));
        return toResponseDTO(opportunity);
    }

    @Override
    public Page<OpportunityResponseDTO> listOpportunities(OpportunityFilterDTO filter, Pageable pageable) {
        Specification<Opportunity> spec = buildSpecification(filter);
        Page<Opportunity> opportunities = opportunityRepository.findAll(spec, pageable);
        return opportunities.map(this::toResponseDTO);
    }

    @Override
    public void deleteOpportunity(UUID id) {
        Opportunity opportunity = opportunityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Oportunidade não encontrada"));

        if (opportunity.getStatus() == OpportunityStatus.WON) {
            throw new IllegalStateException("Não é possível excluir uma oportunidade ganha");
        }

        opportunityRepository.delete(opportunity);
    }

    // ========== MOVIMENTAÇÃO DE ESTÁGIOS ==========

    @Override
    public OpportunityResponseDTO moveStage(UUID id, UUID newStageId) {
        Opportunity opportunity = opportunityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Oportunidade não encontrada"));

        // Verifica se pode mover (apenas se estiver aberta)
        if (opportunity.getStatus() != OpportunityStatus.OPEN) {
            throw new IllegalStateException("Não é possível mover uma oportunidade fechada");
        }

        PipelineStage oldStage = opportunity.getStage();
        PipelineStage newStage = stageRepository.findById(newStageId)
                .orElseThrow(() -> new RuntimeException("Estágio não encontrado"));

        // Move para o novo estágio
        opportunity.setStage(newStage);
        opportunity.setProbability(newStage.getProbability());

        // Se for estágio de ganho ou perda, atualiza status
        if (Boolean.TRUE.equals(newStage.getIsWon())) {
            opportunity.closeAsWon();
        } else if (Boolean.TRUE.equals(newStage.getIsLost())) {
            opportunity.closeAsLost("Movido para estágio de perda");
        }

        Opportunity savedOpportunity = opportunityRepository.save(opportunity);

        // Dispara automação DEAL_STAGE_CHANGED
        automationService.processTrigger(AutomationTrigger.DEAL_STAGE_CHANGED, savedOpportunity);

        // Notifica o vendedor
        if (opportunity.getAssignedTo() != null) {
            notificationService.createNotification(
                    opportunity.getAssignedTo().getId(),
                    "Oportunidade Movida",
                    String.format("A oportunidade \"%s\" foi movida de %s para %s",
                            opportunity.getTitle(),
                            oldStage.getName(),
                            newStage.getName()),
                    com.wedcrm.enums.NotificationType.OPPORTUNITY_STAGE_CHANGED,
                    savedOpportunity.getId(),
                    "Opportunity"
            );
        }

        return toResponseDTO(savedOpportunity);
    }

    @Override
    public OpportunityResponseDTO closeAsWon(UUID id, CloseRequestDTO request) {
        Opportunity opportunity = opportunityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Oportunidade não encontrada"));

        if (opportunity.getStatus() != OpportunityStatus.OPEN) {
            throw new IllegalStateException("Oportunidade já está fechada");
        }

        opportunity.closeAsWon();

        if (request.finalValue() != null) {
            opportunity.setValue(request.finalValue());
        }

        Opportunity savedOpportunity = opportunityRepository.save(opportunity);

        // Dispara automação DEAL_WON
        automationService.processTrigger(AutomationTrigger.DEAL_WON, savedOpportunity);

        // Notifica o vendedor
        if (opportunity.getAssignedTo() != null) {
            notificationService.createNotification(
                    opportunity.getAssignedTo().getId(),
                    "🎉 Oportunidade Ganha!",
                    String.format("Parabéns! A oportunidade \"%s\" foi fechada no valor de %s",
                            opportunity.getTitle(),
                            opportunity.getFormattedValue()),
                    com.wedcrm.enums.NotificationType.DEAL_WON,
                    savedOpportunity.getId(),
                    "Opportunity"
            );
        }

        return toResponseDTO(savedOpportunity);
    }

    @Override
    public OpportunityResponseDTO closeAsLost(UUID id, LostRequestDTO request) {
        Opportunity opportunity = opportunityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Oportunidade não encontrada"));

        if (opportunity.getStatus() != OpportunityStatus.OPEN) {
            throw new IllegalStateException("Oportunidade já está fechada");
        }

        opportunity.closeAsLost(request.reason());
        Opportunity savedOpportunity = opportunityRepository.save(opportunity);

        // Dispara automação DEAL_LOST
        automationService.processTrigger(AutomationTrigger.DEAL_LOST, savedOpportunity);

        return toResponseDTO(savedOpportunity);
    }

    // ========== GESTÃO DE PRODUTOS ==========

    @Override
    public OpportunityProductResponseDTO addProduct(UUID oppId, OpportunityProductRequestDTO request) {
        Opportunity opportunity = opportunityRepository.findById(oppId)
                .orElseThrow(() -> new RuntimeException("Oportunidade não encontrada"));

        if (opportunity.getStatus() != OpportunityStatus.OPEN) {
            throw new IllegalStateException("Não é possível adicionar produtos a uma oportunidade fechada");
        }

        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        // Verifica se produto já existe na oportunidade
        if (opportunityProductRepository.existsInOpportunity(oppId, request.productId())) {
            throw new IllegalArgumentException("Produto já adicionado a esta oportunidade");
        }

        OpportunityProduct oppProduct = new OpportunityProduct();
        oppProduct.setOpportunity(opportunity);
        oppProduct.setProduct(product);
        oppProduct.setQuantity(request.quantity());
        oppProduct.setUnitPrice(request.unitPrice() != null ? request.unitPrice() : product.getPrice());
        oppProduct.setDiscount(request.discount() != null ? request.discount() : BigDecimal.ZERO);
        oppProduct.setNotes(request.notes());
        oppProduct.recalculate();

        OpportunityProduct saved = opportunityProductRepository.save(oppProduct);

        // Recalcula valor total da oportunidade
        opportunity.recalculateTotalValue();
        opportunityRepository.save(opportunity);

        return toProductResponseDTO(saved);
    }

    @Override
    public void removeProduct(UUID oppId, UUID productId) {
        OpportunityProduct oppProduct = opportunityProductRepository.findByOpportunityAndProduct(oppId, productId);
        if (oppProduct == null) {
            throw new RuntimeException("Produto não encontrado na oportunidade");
        }

        Opportunity opportunity = oppProduct.getOpportunity();

        if (opportunity.getStatus() != OpportunityStatus.OPEN) {
            throw new IllegalStateException("Não é possível remover produtos de uma oportunidade fechada");
        }

        opportunityProductRepository.delete(oppProduct);

        // Recalcula valor total da oportunidade
        opportunity.recalculateTotalValue();
        opportunityRepository.save(opportunity);
    }

    // ========== PIPELINE KANBAN ==========

    @Override
    public KanbanBoardDTO getPipelineKanban(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        List<PipelineStage> stages = stageRepository.findAllByOrderByOrderAsc();

        List<KanbanColumnDTO> columns = new ArrayList<>();

        for (PipelineStage stage : stages) {
            List<Opportunity> opportunities;

            if (user.isAdmin() || user.isManager()) {
                opportunities = opportunityRepository.findByStage(stage);
            } else {
                opportunities = opportunityRepository.findByStageAndAssignedTo(stage, user);
            }

            List<KanbanCardDTO> cards = opportunities.stream()
                    .filter(opp -> opp.getStatus() == OpportunityStatus.OPEN)
                    .map(this::toKanbanCardDTO)
                    .collect(Collectors.toList());

            columns.add(new KanbanColumnDTO(
                    stage.getId(),
                    stage.getName(),
                    stage.getColor(),
                    cards,
                    (long) cards.size(),
                    cards.stream().map(KanbanCardDTO::value).reduce(BigDecimal.ZERO, BigDecimal::add)
            ));
        }

        return new KanbanBoardDTO(columns);
    }

    // ========== MÉTRICAS DO PIPELINE ==========

    @Override
    public PipelineMetricsDTO getPipelineMetrics(DateRangeDTO dateRange) {
        LocalDateTime startDate = dateRange.getStartDate();
        LocalDateTime endDate = dateRange.getEndDate();

        PipelineMetricsDTO metrics = new PipelineMetricsDTO();

        // Total em aberto
        metrics.setTotalOpenValue(opportunityRepository.getTotalPipelineValue());
        metrics.setOpenOpportunitiesCount(opportunityRepository.countOpenOpportunities());

        // Taxa de conversão
        long totalClosed = opportunityRepository.countByStatus(OpportunityStatus.WON) +
                opportunityRepository.countByStatus(OpportunityStatus.LOST);
        long wonCount = opportunityRepository.countByStatus(OpportunityStatus.WON);
        metrics.setConversionRate(totalClosed > 0 ? (wonCount * 100.0 / totalClosed) : 0.0);

        // Valor médio das oportunidades ganhas
        Double avgWonValue = opportunityRepository.getAverageWonValue();
        metrics.setAverageWonValue(avgWonValue != null ? BigDecimal.valueOf(avgWonValue) : BigDecimal.ZERO);

        // Velocidade de fechamento (dias)
        Double avgClosingTime = opportunityRepository.getAverageClosingTime();
        metrics.setAverageClosingDays(avgClosingTime != null ? Math.round(avgClosingTime) : 0);

        // Valor ganho no período
        BigDecimal wonValueInPeriod = opportunityRepository.getWonValueInPeriod(startDate, endDate);
        metrics.setWonValueInPeriod(wonValueInPeriod);

        // Pipeline por estágio
        List<Object[]> pipelineSummary = opportunityRepository.getPipelineSummary();
        metrics.setPipelineByStage(pipelineSummary.stream()
                .map(row -> new PipelineStageMetricDTO(
                        (String) row[0],
                        ((Number) row[1]).longValue(),
                        (BigDecimal) row[2]
                ))
                .collect(Collectors.toList()));

        // Performance por vendedor
        List<Object[]> sellerPerformance = opportunityRepository.getConversionRateBySeller();
        metrics.setSellerPerformance(sellerPerformance.stream()
                .map(row -> new SellerPerformanceDTO(
                        ((User) row[0]).getId(),
                        ((User) row[0]).getName(),
                        ((Number) row[1]).longValue(),
                        ((Number) row[2]).longValue(),
                        ((Number) row[3]).doubleValue()
                ))
                .collect(Collectors.toList()));

        return metrics;
    }

    @Override
    public List<OpportunityResponseDTO> getOverdueOpportunities(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        List<Opportunity> overdue;

        if (user.isAdmin() || user.isManager()) {
            overdue = opportunityRepository.findByStatusAndExpectedCloseDateBefore();
        } else {
            overdue = opportunityRepository.findByAssignedToAndStatus(user, OpportunityStatus.OPEN)
                    .stream()
                    .filter(opp -> opp.isOverdue())
                    .collect(Collectors.toList());
        }

        return overdue.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    // ========== MÉTODOS PRIVADOS ==========

    private Specification<Opportunity> buildSpecification(OpportunityFilterDTO filter) {
        List<Specification<Opportunity>> specs = new ArrayList<>();

        specs.add(OpportunitySpecification.filterByCustomerId(filter.customerId()));
        specs.add(OpportunitySpecification.filterByStageId(filter.stageId()));
        specs.add(OpportunitySpecification.filterByAssignedToId(filter.assignedToId()));
        specs.add(OpportunitySpecification.filterByStatus(filter.status()));
        specs.add(OpportunitySpecification.filterByMinValue(filter.minValue()));
        specs.add(OpportunitySpecification.filterByMaxValue(filter.maxValue()));
        specs.add(OpportunitySpecification.filterByMinProbability(filter.minProbability()));
        specs.add(OpportunitySpecification.filterByExpectedCloseDateAfter(filter.expectedCloseDateFrom()));
        specs.add(OpportunitySpecification.filterByExpectedCloseDateBefore(filter.expectedCloseDateTo()));
        specs.add(OpportunitySpecification.filterBySearchTerm(filter.searchTerm()));

        if (Boolean.TRUE.equals(filter.onlyOverdue())) {
            specs.add(OpportunitySpecification.filterByOverdue());
        }

        return OpportunitySpecification.combineSpecifications(specs);
    }

    private OpportunityResponseDTO toResponseDTO(Opportunity opportunity) {
        return OpportunityResponseDTO.builder()
                .id(opportunity.getId())
                .title(opportunity.getTitle())
                .customerId(opportunity.getCustomer().getId())
                .customerName(opportunity.getCustomer().getFullName())
                .stageId(opportunity.getStage().getId())
                .stageName(opportunity.getStage().getName())
                .stageColor(opportunity.getStage().getColor())
                .assignedToId(opportunity.getAssignedTo() != null ? opportunity.getAssignedTo().getId() : null)
                .assignedToName(opportunity.getAssignedTo() != null ? opportunity.getAssignedTo().getName() : null)
                .value(opportunity.getValue())
                .formattedValue(opportunity.getFormattedValue())
                .probability(opportunity.getProbability())
                .expectedCloseDate(opportunity.getExpectedCloseDate())
                .closedAt(opportunity.getClosedAt())
                .status(opportunity.getStatus())
                .lostReason(opportunity.getLostReason())
                .notes(opportunity.getNotes())
                .products(opportunity.getProducts().stream().map(this::toProductResponseDTO).collect(Collectors.toList()))
                .totalProducts(opportunity.getProducts().size())
                .isOverdue(opportunity.isOverdue())
                .daysUntilExpectedClose(opportunity.getDaysUntilExpectedClose())
                .createdAt(opportunity.getCreatedAt())
                .createdBy(opportunity.getCreatedBy())
                .build();
    }

    private OpportunityProductResponseDTO toProductResponseDTO(OpportunityProduct product) {
        return OpportunityProductResponseDTO.builder()
                .id(product.getId())
                .productId(product.getProduct().getId())
                .productName(product.getProduct().getName())
                .productSku(product.getProduct().getSku())
                .quantity(product.getQuantity())
                .unitPrice(product.getUnitPrice())
                .formattedUnitPrice(product.getFormattedUnitPrice())
                .discount(product.getDiscount())
                .formattedDiscount(product.getFormattedDiscount())
                .totalPrice(product.getTotalPrice())
                .formattedTotalPrice(product.getFormattedTotalPrice())
                .subtotal(product.getSubtotal())
                .discountAmount(product.getDiscountAmount())
                .hasDiscount(product.hasDiscount())
                .notes(product.getNotes())
                .build();
    }

    private KanbanCardDTO toKanbanCardDTO(Opportunity opportunity) {
        return new KanbanCardDTO(
                opportunity.getId(),
                opportunity.getTitle(),
                opportunity.getCustomer().getFullName(),
                opportunity.getValue(),
                opportunity.getFormattedValue(),
                opportunity.getProbability(),
                opportunity.getExpectedCloseDate(),
                opportunity.isOverdue()
        );
    }
}