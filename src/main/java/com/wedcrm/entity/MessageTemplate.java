package com.wedcrm.entity;

import com.wedcrm.enums.MessageChannel;
import com.wedcrm.enums.TemplateCategory;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Entity
@Table(name = "message_templates")
public class MessageTemplate extends AbstractEntity {

    @Column(nullable = false, length = 200, unique = true)
    private String name;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MessageChannel channel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TemplateCategory category;

    @Column(length = 300)
    private String subject; // Apenas para EMAIL

    @Column(nullable = false, length = 10000)
    private String body;

    @ElementCollection
    @CollectionTable(name = "template_variables",
            joinColumns = @JoinColumn(name = "template_id"))
    @Column(name = "variable_name", length = 50)
    private List<String> variables = new ArrayList<>();

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(length = 5)
    private String language = "pt-BR";

    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL)
    private List<AutomationRule> automationRules = new ArrayList<>();

    @Column(name = "usage_count", nullable = false)
    private Integer usageCount = 0;

    public MessageTemplate() {

    }

    public MessageTemplate(String name, MessageChannel channel,
                           TemplateCategory category, String body) {
        this.name = name;
        this.channel = channel;
        this.category = category;
        this.body = body;
        this.isActive = true;
        this.language = "pt-BR";
        this.usageCount = 0;
        extractVariables();
    }

    // ========== Getters E Setters ==========

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public MessageChannel getChannel() {
        return channel;
    }

    public void setChannel(MessageChannel channel) {
        this.channel = channel;
    }

    public TemplateCategory getCategory() {
        return category;
    }

    public void setCategory(TemplateCategory category) {
        this.category = category;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
        extractVariables(); // Atualiza a lista de variáveis
    }

    public List<String> getVariables() {
        return variables;
    }

    public void setVariables(List<String> variables) {
        this.variables = variables;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public List<AutomationRule> getAutomationRules() {
        return automationRules;
    }

    public void setAutomationRules(List<AutomationRule> automationRules) {
        this.automationRules = automationRules;
    }

    public Integer getUsageCount() {
        return usageCount;
    }

    public void setUsageCount(Integer usageCount) {
        this.usageCount = usageCount;
    }

    // ========== Métodos De Negocio ==========

    /**
     * Extrai automaticamente as variáveis do template no formato {{variavel}}
     */
    private void extractVariables() {
        if (body == null) return;

        Pattern pattern = Pattern.compile("\\{\\{(.*?)\\}\\}");
        Matcher matcher = pattern.matcher(body);

        variables.clear();
        while (matcher.find()) {
            String variable = matcher.group(1).trim();
            if (!variables.contains(variable)) {
                variables.add(variable);
            }
        }
    }

    /**
     * Incrementa o contador de uso
     */
    public void incrementUsage() {
        this.usageCount++;
    }

    /**
     * Ativa o template
     */
    public void activate() {
        this.isActive = true;
    }

    /**
     * Desativa o template
     */
    public void deactivate() {
        this.isActive = false;
    }

    /**
     * Verifica se é um template de e-mail
     */
    public boolean isEmailTemplate() {
        return MessageChannel.EMAIL.equals(channel);
    }

    /**
     * Verifica se o template tem assunto (obrigatório para e-mail)
     */
    public boolean hasValidSubject() {
        return !isEmailTemplate() || (subject != null && !subject.trim().isEmpty());
    }

    /**
     * Retorna uma prévia do template com as variáveis substituídas por exemplos
     */
    public String getPreview() {
        String preview = body;

        // Substitui variáveis por exemplos
        preview = preview.replace("{{nome}}", "João Silva");
        preview = preview.replace("{{nomeCompleto}}", "João da Silva Santos");
        preview = preview.replace("{{empresa}}", "Empresa Exemplo Ltda");
        preview = preview.replace("{{vendedor}}", "Carlos Vendedor");
        preview = preview.replace("{{emailVendedor}}", "carlos@wedcrm.com");
        preview = preview.replace("{{telefoneVendedor}}", "(11) 99999-9999");
        preview = preview.replace("{{dataAtual}}", "15/03/2024");
        preview = preview.replace("{{valorOportunidade}}", "R$ 15.000,00");
        preview = preview.replace("{{estagio}}", "Negociação");
        preview = preview.replace("{{diasSemContato}}", "5");
        preview = preview.replace("{{linkProposta}}", "https://wedcrm.com/proposta/123");
        preview = preview.replace("{{linkReuniao}}", "https://meet.google.com/abc-xyz");

        return preview;
    }

    /**
     * Valida se todas as variáveis necessárias estão presentes
     */
    public boolean validateVariables(List<String> requiredVariables) {
        return variables.containsAll(requiredVariables);
    }

    /**
     * Retorna o ícone do canal
     */
    public String getChannelIcon() {
        return switch (channel) {
            case EMAIL -> "📧";
            case SMS -> "📱";
            case WHATSAPP -> "💬";
            case PUSH -> "🔔";
        };
    }

    /**
     * Retorna a cor da categoria
     */
    public String getCategoryColor() {
        return switch (category) {
            case WELCOME -> "#28a745";  // Verde
            case FOLLOWUP -> "#007bff"; // Azul
            case BIRTHDAY -> "#ffc107"; // Amarelo
            case PROPOSAL -> "#17a2b8"; // Azul claro
            case REMINDER -> "#fd7e14"; // Laranja
            case NURTURING -> "#6f42c1"; // Roxo
            case WIN_BACK -> "#dc3545"; // Vermelho
            case CUSTOM -> "#6c757d";   // Cinza
        };
    }

    @Override
    public String toString() {
        return "MessageTemplate{" +
                "id=" + getId() +
                ", name='" + name + '\'' +
                ", channel=" + channel +
                ", category=" + category +
                ", isActive=" + isActive +
                ", usageCount=" + usageCount +
                '}';
    }
}