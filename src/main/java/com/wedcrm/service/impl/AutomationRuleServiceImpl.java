package com.wedcrm.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wedcrm.dto.filter.AutomationRuleFilterDTO;
import com.wedcrm.dto.request.AutomationRuleRequestDTO;
import com.wedcrm.dto.response.AutomationRuleResponseDTO;
import com.wedcrm.dto.Assistants.RuleExecutionHistoryDTO;
import com.wedcrm.entity.AutomationRule;
import com.wedcrm.entity.Customer;
import com.wedcrm.entity.MessageTemplate;
import com.wedcrm.enums.AutomationTrigger;
import com.wedcrm.enums.CustomerStatus;
import com.wedcrm.repository.AutomationRuleRepository;
import com.wedcrm.repository.MessageTemplateRepository;
import com.wedcrm.service.AutomationRuleService;
import com.wedcrm.service.MessageGeneratorService;
import com.wedcrm.specification.AutomationRuleSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Transactional
public class AutomationRuleServiceImpl implements AutomationRuleService {

    @Autowired
    private AutomationRuleRepository ruleRepository;

    @Autowired
    private MessageTemplateRepository templateRepository;

    @Autowired
    private MessageGeneratorService messageGeneratorService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Histórico de execuções (em produção usar banco de dados)
    private final Map<UUID, List<RuleExecutionHistoryDTO>> executionHistory = new ConcurrentHashMap<>();

    // ========== CRUD BÁSICO ==========

    @Override
    public AutomationRuleResponseDTO createRule(AutomationRuleRequestDTO request) {
        // 1. Verifica se nome já existe
        if (ruleRepository.existsByName(request.name())) {
            throw new IllegalArgumentException("Regra com nome '" + request.name() + "' já existe");
        }

        // 2. Busca template
        MessageTemplate template = templateRepository.findById(request.templateId())
                .orElseThrow(() -> new RuntimeException("Template não encontrado"));

        // 3. Cria regra
        AutomationRule rule = new AutomationRule();
        rule.setName(request.name());
        rule.setDescription(request.description());
        rule.setTrigger(request.trigger());
        rule.setTemplate(template);
        rule.setDelayMinutes(request.delayMinutes() != null ? request.delayMinutes() : 0);
        rule.setChannel(request.channel());
        rule.setIsActive(request.isActive() != null ? request.isActive() : true);
        rule.setExecutionCount(0L);

        // 4. Define condições
        if (request.conditions() != null && !request.conditions().isEmpty()) {
            try {
                rule.setConditions(objectMapper.writeValueAsString(request.conditions()));
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Erro ao serializar condições", e);
            }
        }

        // 5. Valida regra
        validateRule(rule);

        AutomationRule savedRule = ruleRepository.save(rule);
        return toResponseDTO(savedRule);
    }

    @Override
    public AutomationRuleResponseDTO updateRule(UUID id, AutomationRuleRequestDTO request) {
        AutomationRule rule = ruleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Regra não encontrada"));

        // Verifica se nome já existe (excluindo a própria)
        if (!rule.getName().equals(request.name()) && ruleRepository.existsByName(request.name())) {
            throw new IllegalArgumentException("Regra com nome '" + request.name() + "' já existe");
        }

        // Atualiza dados
        rule.setName(request.name());
        rule.setDescription(request.description());
        rule.setTrigger(request.trigger());
        rule.setDelayMinutes(request.delayMinutes() != null ? request.delayMinutes() : 0);
        rule.setChannel(request.channel());

        if (request.isActive() != null) {
            rule.setIsActive(request.isActive());
        }

        // Atualiza template se necessário
        if (!rule.getTemplate().getId().equals(request.templateId())) {
            MessageTemplate template = templateRepository.findById(request.templateId())
                    .orElseThrow(() -> new RuntimeException("Template não encontrado"));
            rule.setTemplate(template);
        }

        // Atualiza condições
        if (request.conditions() != null) {
            try {
                rule.setConditions(objectMapper.writeValueAsString(request.conditions()));
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Erro ao serializar condições", e);
            }
        }

        // Valida regra
        validateRule(rule);

        AutomationRule savedRule = ruleRepository.save(rule);
        return toResponseDTO(savedRule);
    }

    @Override
    public AutomationRuleResponseDTO getRuleById(UUID id) {
        AutomationRule rule = ruleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Regra não encontrada"));
        return toResponseDTO(rule);
    }

    @Override
    public Page<AutomationRuleResponseDTO> listRules(AutomationRuleFilterDTO filter, Pageable pageable) {
        Specification<AutomationRule> spec = buildSpecification(filter);
        Page<AutomationRule> rules = ruleRepository.findAll(spec, pageable);
        return rules.map(this::toResponseDTO);
    }

    @Override
    public void deleteRule(UUID id) {
        AutomationRule rule = ruleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Regra não encontrada"));

        // Verifica se pode deletar (apenas inativas ou nunca executadas)
        if (rule.getIsActive() && rule.getExecutionCount() > 0) {
            throw new IllegalStateException("Não é possível deletar uma regra ativa que já foi executada. Desative-a primeiro.");
        }

        ruleRepository.delete(rule);
    }

    // ========== ATIVAÇÃO/DESATIVAÇÃO ==========

    @Override
    public AutomationRuleResponseDTO toggleRule(UUID id) {
        AutomationRule rule = ruleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Regra não encontrada"));

        if (rule.getIsActive()) {
            rule.deactivate();
        } else {
            rule.activate();
        }

        AutomationRule savedRule = ruleRepository.save(rule);
        return toResponseDTO(savedRule);
    }

    // ========== CONSULTAS POR GATILHO ==========

    @Override
    public List<AutomationRuleResponseDTO> getRulesByTrigger(AutomationTrigger trigger) {
        List<AutomationRule> rules = ruleRepository.findByTriggerAndIsActiveTrue(trigger);
        return rules.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    // ========== AVALIAÇÃO DE CONDIÇÕES ==========

    @Override
    public boolean evaluateConditions(AutomationRule rule, Customer customer) {
        if (rule.getConditions() == null || rule.getConditions().isEmpty()) {
            return true;
        }

        Map<String, Object> conditions = rule.getConditionsAsMap();

        for (Map.Entry<String, Object> condition : conditions.entrySet()) {
            if (!evaluateSingleCondition(condition.getKey(), condition.getValue(), customer)) {
                return false;
            }
        }

        return true;
    }

    private boolean evaluateSingleCondition(String key, Object expectedValue, Customer customer) {
        switch (key) {
            case "status":
                return customer.getStatus() != null &&
                        customer.getStatus().name().equals(expectedValue);

            case "statusNot":
                return customer.getStatus() == null ||
                        !customer.getStatus().name().equals(expectedValue);

            case "source":
                return customer.getSource() != null &&
                        customer.getSource().name().equals(expectedValue);

            case "minScore":
                int minScore = ((Number) expectedValue).intValue();
                return customer.getScore() >= minScore;

            case "maxScore":
                int maxScore = ((Number) expectedValue).intValue();
                return customer.getScore() <= maxScore;

            case "hasOpportunity":
                boolean hasOpportunity = (Boolean) expectedValue;
                return hasOpportunity == !customer.getOpportunities().isEmpty();

            case "minOpportunityValue":
                BigDecimal minValue = BigDecimal.valueOf(((Number) expectedValue).doubleValue());
                return customer.getOpportunities().stream()
                        .anyMatch(opp -> opp.getValue() != null && opp.getValue().compareTo(minValue) >= 0);

            case "tag":
                String tagName = (String) expectedValue;
                return customer.getTags().stream()
                        .anyMatch(tag -> tag.getName().equals(tagName));

            case "anyTag":
                @SuppressWarnings("unchecked")
                List<String> tags = (List<String>) expectedValue;
                return customer.getTags().stream()
                        .anyMatch(tag -> tags.contains(tag.getName()));

            case "allTags":
                @SuppressWarnings("unchecked")
                List<String> allTags = (List<String>) expectedValue;
                return customer.getTags().stream()
                        .map(Tag::getName)
                        .collect(Collectors.toSet())
                        .containsAll(allTags);

            case "daysWithoutContact":
                int days = ((Number) expectedValue).intValue();
                if (customer.getLastContactDate() == null) return true;
                long actualDays = java.time.temporal.ChronoUnit.DAYS.between(
                        customer.getLastContactDate(), LocalDate.now());
                return actualDays >= days;

            case "companySize":
                // Exemplo: condições baseadas no tamanho da empresa
                String size = (String) expectedValue;
                // Implementar lógica de tamanho da empresa
                return true;

            default:
                // Campo personalizado
                Object customValue = customer.getCustomField(key);
                return customValue != null && customValue.equals(expectedValue);
        }
    }

    // ========== EXECUÇÃO DA REGRA ==========

    @Override
    public void executeRule(AutomationRule rule, Customer customer) {
        if (!rule.getIsActive()) {
            throw new IllegalStateException("Regra está inativa: " + rule.getName());
        }

        // Avalia condições
        if (!evaluateConditions(rule, customer)) {
            System.out.println("Condições não satisfeitas para regra: " + rule.getName());
            return;
        }

        try {
            // Envia mensagem
            messageGeneratorService.sendMessage(
                    customer.getId(),
                    rule.getTemplate().getId(),
                    rule.getChannel()
            );

            // Registra execução bem-sucedida
            registerExecution(rule, customer, true, null);

        } catch (Exception e) {
            // Registra execução com falha
            registerExecution(rule, customer, false, e.getMessage());
            throw new RuntimeException("Erro ao executar regra: " + e.getMessage(), e);
        }
    }

    private void registerExecution(AutomationRule rule, Customer customer, boolean success, String errorMessage) {
        // Atualiza estatísticas da regra
        rule.registerExecution();
        ruleRepository.save(rule);

        // Registra no histórico
        RuleExecutionHistoryDTO history = RuleExecutionHistoryDTO.builder()
                .ruleId(rule.getId())
                .ruleName(rule.getName())
                .customerId(customer.getId())
                .customerName(customer.getFullName())
                .executedAt(LocalDateTime.now())
                .success(success)
                .errorMessage(errorMessage)
                .build();

        executionHistory.computeIfAbsent(rule.getId(), k -> new ArrayList<>())
                .add(0, history); // Adiciona no início (mais recente primeiro)

        // Limita histórico a 100 registros por regra
        List<RuleExecutionHistoryDTO> historyList = executionHistory.get(rule.getId());
        if (historyList.size() > 100) {
            historyList.remove(historyList.size() - 1);
        }
    }

    // ========== HISTÓRICO DE EXECUÇÃO ==========

    @Override
    public List<RuleExecutionHistoryDTO> getRuleExecutionHistory(UUID ruleId) {
        AutomationRule rule = ruleRepository.findById(ruleId)
                .orElseThrow(() -> new RuntimeException("Regra não encontrada"));

        return executionHistory.getOrDefault(ruleId, Collections.emptyList());
    }

    // ========== DUPLICAÇÃO DE REGRA ==========

    @Override
    public AutomationRuleResponseDTO duplicateRule(UUID id, String newName) {
        AutomationRule original = ruleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Regra não encontrada"));

        // Verifica se novo nome já existe
        if (ruleRepository.existsByName(newName)) {
            throw new IllegalArgumentException("Regra com nome '" + newName + "' já existe");
        }

        AutomationRule duplicate = new AutomationRule();
        duplicate.setName(newName);
        duplicate.setDescription("Cópia de: " + original.getName());
        duplicate.setTrigger(original.getTrigger());
        duplicate.setConditions(original.getConditions());
        duplicate.setTemplate(original.getTemplate());
        duplicate.setDelayMinutes(original.getDelayMinutes());
        duplicate.setChannel(original.getChannel());
        duplicate.setIsActive(false); // Começa inativa
        duplicate.setExecutionCount(0L);

        AutomationRule savedRule = ruleRepository.save(duplicate);
        return toResponseDTO(savedRule);
    }

    // ========== VALIDAÇÃO ==========

    @Override
    public void validateRule(AutomationRule rule) {
        // 1. Verifica se template está ativo
        if (!rule.getTemplate().getIsActive()) {
            throw new IllegalArgumentException("Template associado está inativo");
        }

        // 2. Verifica compatibilidade template/canal
        if (rule.getTemplate().getChannel() != rule.getChannel()) {
            throw new IllegalArgumentException(
                    String.format("Template '%s' é do canal %s, mas regra usa canal %s",
                            rule.getTemplate().getName(),
                            rule.getTemplate().getChannel(),
                            rule.getChannel())
            );
        }

        // 3. Verifica se template de e-mail tem assunto
        if (rule.getChannel() == MessageChannel.EMAIL &&
                (rule.getTemplate().getSubject() == null || rule.getTemplate().getSubject().isEmpty())) {
            throw new IllegalArgumentException("Template de e-mail não possui assunto");
        }

        // 4. Verifica se regra manual tem delay
        if (rule.getTrigger() == AutomationTrigger.MANUAL_TRIGGER && rule.getDelayMinutes() > 0) {
            throw new IllegalArgumentException("Regras manuais não podem ter delay");
        }

        // 5. Verifica se condições são válidas
        if (rule.getConditions() != null && !rule.getConditions().isEmpty()) {
            try {
                objectMapper.readTree(rule.getConditions());
            } catch (JsonProcessingException e) {
                throw new IllegalArgumentException("Condições JSON inválidas", e);
            }
        }
    }

    // ========== MÉTODOS PRIVADOS ==========

    private Specification<AutomationRule> buildSpecification(AutomationRuleFilterDTO filter) {
        List<Specification<AutomationRule>> specs = new ArrayList<>();

        specs.add(AutomationRuleSpecification.filterByTrigger(filter.trigger()));
        specs.add(AutomationRuleSpecification.filterByChannel(filter.channel()));
        specs.add(AutomationRuleSpecification.filterByIsActive(filter.isActive()));
        specs.add(AutomationRuleSpecification.filterByTemplateId(filter.templateId()));
        specs.add(AutomationRuleSpecification.filterByMinDelay(filter.minDelay()));
        specs.add(AutomationRuleSpecification.filterByMaxDelay(filter.maxDelay()));
        specs.add(AutomationRuleSpecification.filterByMinExecutions(filter.minExecutions()));
        specs.add(AutomationRuleSpecification.filterByNameContaining(filter.nameSearch()));

        if (Boolean.TRUE.equals(filter.onlyNeverExecuted())) {
            specs.add(AutomationRuleSpecification.filterByNeverExecuted());
        }

        if (Boolean.TRUE.equals(filter.onlyBirthdayRules())) {
            specs.add(AutomationRuleSpecification.filterByBirthdayRules());
        }

        if (Boolean.TRUE.equals(filter.onlyInactivityRules())) {
            specs.add(AutomationRuleSpecification.filterByInactivityRules());
        }

        return AutomationRuleSpecification.combineSpecifications(specs);
    }

    private AutomationRuleResponseDTO toResponseDTO(AutomationRule rule) {
        return AutomationRuleResponseDTO.builder()
                .id(rule.getId())
                .name(rule.getName())
                .description(rule.getDescription())
                .trigger(rule.getTrigger())
                .triggerDescription(rule.getTriggerDescription())
                .triggerIcon(rule.getTriggerIcon())
                .conditions(rule.getConditionsAsMap())
                .templateId(rule.getTemplate().getId())
                .templateName(rule.getTemplate().getName())
                .delayMinutes(rule.getDelayMinutes())
                .isActive(rule.getIsActive())
                .channel(rule.getChannel())
                .lastExecutedAt(rule.getLastExecutedAt())
                .executionCount(rule.getExecutionCount())
                .createdAt(rule.getCreatedAt())
                .createdBy(rule.getCreatedBy())
                .build();
    }
}