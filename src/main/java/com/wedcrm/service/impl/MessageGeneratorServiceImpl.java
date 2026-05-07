package com.wedcrm.service.impl;

import com.wedcrm.dto.Assistants.PreviewMessageDTO;
import com.wedcrm.entity.*;
import com.wedcrm.enums.*;
import com.wedcrm.repository.*;
import com.wedcrm.service.EmailService;
import com.wedcrm.service.MessageGeneratorService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Transactional
public class MessageGeneratorServiceImpl implements MessageGeneratorService {

    private final MessageTemplateRepository templateRepository;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final OpportunityRepository opportunityRepository;
    private final AutomationRuleRepository automationRuleRepository;
    private final InteractionRepository interactionRepository;
    private final EmailService emailService;

    private final String frontendUrl;

    // Fila de mensagens agendadas (produção: use Redis ou RabbitMQ)
    private final Map<UUID, ScheduledMessage> scheduledMessages = new ConcurrentHashMap<>();

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final Pattern variablePattern = Pattern.compile("\\{\\{(.*?)\\}\\}");

    public MessageGeneratorServiceImpl(
            MessageTemplateRepository templateRepository,
            CustomerRepository customerRepository,
            UserRepository userRepository,
            OpportunityRepository opportunityRepository,
            AutomationRuleRepository automationRuleRepository,
            InteractionRepository interactionRepository,
            EmailService emailService,
            @Value("${wedcrm.frontend-url}") String frontendUrl) {

        this.templateRepository = templateRepository;
        this.customerRepository = customerRepository;
        this.userRepository = userRepository;
        this.opportunityRepository = opportunityRepository;
        this.automationRuleRepository = automationRuleRepository;
        this.interactionRepository = interactionRepository;
        this.emailService = emailService;
        this.frontendUrl = frontendUrl;
    }

    // =========================================================================
    //  GERAÇÃO E ENVIO DE MENSAGENS
    // =========================================================================

    @Override
    public String generateMessage(UUID templateId, UUID customerId) {
        MessageTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new RuntimeException("Template não encontrado"));
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));
        return resolveVariables(template.getBody(), customer);
    }

    @Override
    public void sendMessage(UUID customerId, UUID templateId, MessageChannel channel) {
        MessageTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new RuntimeException("Template não encontrado"));
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        // valida compatibilidade do canal
        if (template.getChannel() != channel) {
            throw new IllegalArgumentException(String.format(
                    "Template '%s' é do canal %s, não compatível com %s",
                    template.getName(), template.getChannel(), channel));
        }

        String processedBody = resolveVariables(template.getBody(), customer);
        String processedSubject = template.isEmailTemplate() ?
                resolveVariables(template.getSubject(), customer) : null;

        // envia pelo canal apropriado (apenas EMAIL implementado)
        sendByChannel(customer, template, processedBody, processedSubject);

        // registra interação
        Interaction interaction = new Interaction();
        interaction.setCustomer(customer);
        interaction.setType(template.isEmailTemplate() ? InteractionType.EMAIL : InteractionType.AUTO_MESSAGE);
        interaction.setDirection(Direction.OUTBOUND);
        interaction.setSubject(processedSubject);
        interaction.setContent(processedBody);
        interaction.setChannel(channel.name());
        interaction.setIsAutomatic(true);
        interaction.setTemplateUsed(template);
        interaction.setSentAt(LocalDateTime.now());
        interaction.setStatus(InteractionStatus.SENT);
        interactionRepository.save(interaction);

        // incrementa contador de uso
        template.incrementUsage();
        templateRepository.save(template);
    }

    // =========================================================================
    //  AUTOMAÇÃO
    // =========================================================================

    @Override
    public void processAutomationTrigger(AutomationTrigger trigger, UUID customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        List<AutomationRule> rules = automationRuleRepository.findByTriggerAndIsActiveTrue(trigger);
        for (AutomationRule rule : rules) {
            try {
                if (!evaluateConditions(rule, customer)) {
                    continue;
                }
                if (rule.hasDelay()) {
                    LocalDateTime scheduledTime = LocalDateTime.now().plusMinutes(rule.getDelayMinutes());
                    scheduleMessage(customerId, rule.getTemplate().getId(), scheduledTime);
                } else {
                    sendMessage(customerId, rule.getTemplate().getId(), rule.getChannel());
                }
                rule.registerExecution();
                automationRuleRepository.save(rule);
            } catch (Exception e) {
                System.err.println("Erro ao executar regra " + rule.getName() + ": " + e.getMessage());
            }
        }
    }

    // =========================================================================
    //  SUBSTITUIÇÃO DE VARIÁVEIS
    // =========================================================================

    @Override
    public String resolveVariables(String template, Customer customer) {
        if (template == null) return null;
        Map<String, String> variables = buildVariableMap(customer);
        StringBuffer result = new StringBuffer();
        Matcher matcher = variablePattern.matcher(template);
        while (matcher.find()) {
            String varName = matcher.group(1).trim();
            String replacement = variables.getOrDefault(varName, "{{" + varName + "}}");
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    private Map<String, String> buildVariableMap(Customer customer) {
        Map<String, String> vars = new HashMap<>();

        // cliente
        vars.put("nome", safe(customer.getFirstName()));
        vars.put("nomeCompleto", safe(customer.getFullName()));
        vars.put("empresa", safe(customer.getCompany()));
        vars.put("email", safe(customer.getEmail()));
        vars.put("telefone", safe(customer.getPhone()));

        // vendedor responsável
        if (customer.getAssignedTo() != null) {
            User seller = customer.getAssignedTo();
            vars.put("vendedor", safe(seller.getName()));
            vars.put("emailVendedor", safe(seller.getEmail()));
            vars.put("telefoneVendedor", safe(seller.getPhone()));
        }

        // data atual
        vars.put("dataAtual", LocalDate.now().format(dateFormatter));

        // oportunidade mais recente (aberta)
        Optional<Opportunity> latestOpenOpp = customer.getOpportunities().stream()
                .filter(o -> o.getStatus() == OpportunityStatus.OPEN)
                .findFirst();
        if (latestOpenOpp.isPresent()) {
            Opportunity opp = latestOpenOpp.get();
            vars.put("valorOportunidade", String.format("R$ %,.2f", opp.getValue()));
            vars.put("estagio", safe(opp.getStage().getName()));
        }

        // dias sem contato
        if (customer.getLastContactDate() != null) {
            long days = ChronoUnit.DAYS.between(customer.getLastContactDate(), LocalDate.now());
            vars.put("diasSemContato", String.valueOf(days));
        } else {
            vars.put("diasSemContato", "0");
        }

        // links dinâmicos
        vars.put("linkProposta", frontendUrl + "/proposta/" + customer.getId());
        vars.put("linkReuniao", frontendUrl + "/agendar/" + customer.getId());

        return vars;
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    // =========================================================================
    //  MENSAGENS AGENDADAS
    // =========================================================================

    @Override
    public void scheduleMessage(UUID customerId, UUID templateId, LocalDateTime scheduledTime) {
        MessageTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new RuntimeException("Template não encontrado"));
        ScheduledMessage msg = new ScheduledMessage(
                UUID.randomUUID(),
                customerId,
                templateId,
                template.getChannel(),
                scheduledTime
        );
        scheduledMessages.put(msg.id(), msg);
    }

    @Override
    @Scheduled(cron = "0 * * * * *") // a cada minuto
    public void processScheduledMessages() {
        LocalDateTime now = LocalDateTime.now();
        List<UUID> toRemove = new ArrayList<>();
        for (ScheduledMessage msg : scheduledMessages.values()) {
            if (!msg.scheduledTime().isAfter(now)) {
                toRemove.add(msg.id());
                try {
                    sendMessage(msg.customerId(), msg.templateId(), msg.channel());
                } catch (Exception e) {
                    System.err.println("Erro ao enviar mensagem agendada: " + e.getMessage());
                }
            }
        }
        toRemove.forEach(scheduledMessages::remove);
    }

    // =========================================================================
    //  MENSAGENS PROGRAMADAS (BIRTHDAY & REENGAGEMENT)
    // =========================================================================

    @Override
    @Scheduled(cron = "0 0 8 * * *") // todos os dias às 8:00
    public void sendBirthdayMessages() {
        LocalDate today = LocalDate.now();
        List<Customer> birthdayCustomers = customerRepository.findByBirthday(
                today.getMonthValue(), today.getDayOfMonth());
        List<AutomationRule> birthdayRules = automationRuleRepository.findBirthdayRules();
        for (Customer customer : birthdayCustomers) {
            for (AutomationRule rule : birthdayRules) {
                try {
                    sendMessage(customer.getId(), rule.getTemplate().getId(), rule.getChannel());
                    rule.registerExecution();
                    automationRuleRepository.save(rule);
                } catch (Exception e) {
                    System.err.println("Erro no aniversário de " + customer.getEmail() + ": " + e.getMessage());
                }
            }
        }
    }

    @Override
    @Scheduled(cron = "0 0 9 * * MON") // toda segunda-feira às 9:00
    public void sendReengagementMessages() {
        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
        List<Customer> inactiveCustomers = customerRepository.findInactiveCustomers(thirtyDaysAgo);
        List<AutomationRule> inactivityRules = automationRuleRepository.findInactivityRules();
        for (Customer customer : inactiveCustomers) {
            for (AutomationRule rule : inactivityRules) {
                try {
                    sendMessage(customer.getId(), rule.getTemplate().getId(), rule.getChannel());
                    rule.registerExecution();
                    automationRuleRepository.save(rule);
                } catch (Exception e) {
                    System.err.println("Erro no reengajamento de " + customer.getEmail() + ": " + e.getMessage());
                }
            }
        }
    }

    // =========================================================================
    //  PRÉ-VISUALIZAÇÃO
    // =========================================================================

    @Override
    public PreviewMessageDTO previewMessage(UUID templateId, UUID customerId) {
        MessageTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new RuntimeException("Template não encontrado"));

        String previewBody;
        String previewSubject;
        String customerName;
        String customerEmail;

        if (customerId != null) {
            Customer customer = customerRepository.findById(customerId)
                    .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));
            previewBody = resolveVariables(template.getBody(), customer);
            previewSubject = template.isEmailTemplate() ? resolveVariables(template.getSubject(), customer) : null;
            customerName = customer.getFullName();
            customerEmail = customer.getEmail();
        } else {
            Customer dummy = createDummyCustomer();
            previewBody = resolveVariables(template.getBody(), dummy);
            previewSubject = template.isEmailTemplate() ? resolveVariables(template.getSubject(), dummy) : null;
            customerName = "Cliente Exemplo";
            customerEmail = "cliente@exemplo.com";
        }

        Set<String> usedVariables = extractVariables(template.getBody());

        return new PreviewMessageDTO(
                template.getId(),
                template.getName(),
                template.getChannel().name(),
                previewSubject,
                previewBody,
                usedVariables,
                customerName,
                customerEmail
        );
    }

    // =========================================================================
    //  MÉTODOS AUXILIARES PRIVADOS
    // =========================================================================

    private void sendByChannel(Customer customer, MessageTemplate template, String body, String subject) {
        switch (template.getChannel()) {
            case EMAIL:
                if (customer.getEmail() == null || customer.getEmail().isBlank())
                    throw new RuntimeException("Cliente não possui e-mail cadastrado");
                emailService.sendTemplateEmail(customer.getEmail(), subject, body);
                break;
            case SMS:
                // TODO: integrar com Twilio ou similar
                System.out.println("SMS enviado para " + customer.getPhone() + ": " + body);
                break;
            case WHATSAPP:
                // TODO: integrar com WhatsApp Business API
                System.out.println("WhatsApp enviado para " + customer.getWhatsapp() + ": " + body);
                break;
            case PUSH:
                // TODO: integrar com FCM/APNs
                System.out.println("Push enviado para cliente " + customer.getId() + ": " + body);
                break;
            default:
                throw new IllegalArgumentException("Canal não suportado: " + template.getChannel());
        }
    }

    private boolean evaluateConditions(AutomationRule rule, Customer customer) {
        Map<String, Object> conditions = rule.getConditionsAsMap();
        for (Map.Entry<String, Object> entry : conditions.entrySet()) {
            switch (entry.getKey()) {
                case "status":
                    if (!customer.getStatus().name().equals(entry.getValue()))
                        return false;
                    break;
                case "minScore":
                    int minScore = ((Number) entry.getValue()).intValue();
                    if (customer.getScore() < minScore) return false;
                    break;
                case "hasOpportunity":
                    boolean hasOpp = (Boolean) entry.getValue();
                    if (hasOpp != !customer.getOpportunities().isEmpty()) return false;
                    break;
                case "tag":
                    String tagName = (String) entry.getValue();
                    boolean hasTag = customer.getTags().stream().anyMatch(t -> t.getName().equals(tagName));
                    if (!hasTag) return false;
                    break;
                // adicione outras condições conforme necessário
            }
        }
        return true;
    }

    private Set<String> extractVariables(String text) {
        Set<String> vars = new HashSet<>();
        if (text == null) return vars;
        Matcher matcher = variablePattern.matcher(text);
        while (matcher.find()) {
            vars.add(matcher.group(1).trim());
        }
        return vars;
    }

    private Customer createDummyCustomer() {
        Customer dummy = new Customer();
        dummy.setFirstName("Cliente");
        dummy.setLastName("Exemplo");
        dummy.setCompany("Empresa Exemplo Ltda");
        dummy.setEmail("cliente@exemplo.com");
        dummy.setPhone("(11) 99999-9999");
        // outros campos necessários conforme variáveis usadas nos templates
        return dummy;
    }

    // Record interno para mensagens agendadas
    private record ScheduledMessage(UUID id, UUID customerId, UUID templateId,
                                    MessageChannel channel, LocalDateTime scheduledTime) {}
}