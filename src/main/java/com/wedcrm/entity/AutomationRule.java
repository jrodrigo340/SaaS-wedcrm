package com.wedcrm.entity;

import com.wedcrm.enums.AutomationTrigger;
import com.wedcrm.enums.MessageChannel;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "automation_rules")
public class AutomationRule extends AbstractEntity {

    @Column(nullable = false, length = 200, unique = true)
    private String name;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AutomationTrigger trigger;

    @Column(columnDefinition = "TEXT")
    private String conditions; // JSON com condições adicionais

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "template_id", nullable = false)
    private MessageTemplate template;

    @Column(name = "delay_minutes", nullable = false)
    private Integer delayMinutes = 0;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "last_executed_at")
    private LocalDateTime lastExecutedAt;

    @Column(name = "execution_count", nullable = false)
    private Long executionCount = 0L;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MessageChannel channel;

    @Transient
    private ObjectMapper objectMapper = new ObjectMapper();

    public AutomationRule() {

    }

    public AutomationRule(String name, AutomationTrigger trigger,
                          MessageTemplate template, MessageChannel channel) {
        this.name = name;
        this.trigger = trigger;
        this.template = template;
        this.channel = channel;
        this.delayMinutes = 0;
        this.isActive = true;
        this.executionCount = 0L;
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

    public AutomationTrigger getTrigger() {
        return trigger;
    }

    public void setTrigger(AutomationTrigger trigger) {
        this.trigger = trigger;
    }

    public String getConditions() {
        return conditions;
    }

    public void setConditions(String conditions) {
        this.conditions = conditions;
    }

    public MessageTemplate getTemplate() {
        return template;
    }

    public void setTemplate(MessageTemplate template) {
        this.template = template;
    }

    public Integer getDelayMinutes() {
        return delayMinutes;
    }

    public void setDelayMinutes(Integer delayMinutes) {
        this.delayMinutes = delayMinutes != null ? delayMinutes : 0;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public LocalDateTime getLastExecutedAt() {
        return lastExecutedAt;
    }

    public void setLastExecutedAt(LocalDateTime lastExecutedAt) {
        this.lastExecutedAt = lastExecutedAt;
    }

    public Long getExecutionCount() {
        return executionCount;
    }

    public void setExecutionCount(Long executionCount) {
        this.executionCount = executionCount;
    }

    public MessageChannel getChannel() {
        return channel;
    }

    public void setChannel(MessageChannel channel) {
        this.channel = channel;
    }

    // ========== Métodos para Gerencia Condições ==========

    /**
     * Define as condições a partir de um Map
     */
    public void setConditionsFromMap(Map<String, Object> conditionsMap) {
        try {
            this.conditions = objectMapper.writeValueAsString(conditionsMap);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Erro ao serializar condições", e);
        }
    }

    /**
     * Obtém as condições como Map
     */
    public Map<String, Object> getConditionsAsMap() {
        if (conditions == null || conditions.isEmpty()) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(conditions, new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            return new HashMap<>();
        }
    }

    /**
     * Adiciona uma condição
     */
    public void addCondition(String key, Object value) {
        Map<String, Object> conditionsMap = getConditionsAsMap();
        conditionsMap.put(key, value);
        setConditionsFromMap(conditionsMap);
    }

    /**
     * Remove uma condição
     */
    public void removeCondition(String key) {
        Map<String, Object> conditionsMap = getConditionsAsMap();
        conditionsMap.remove(key);
        setConditionsFromMap(conditionsMap);
    }

    /**
     * Obtém o valor de uma condição específica
     */
    public Object getCondition(String key) {
        return getConditionsAsMap().get(key);
    }

    // ========== Métodos De Negocio ==========

    /**
     * Registra uma execução da regra
     */
    public void registerExecution() {
        this.lastExecutedAt = LocalDateTime.now();
        this.executionCount++;

        // Incrementa o contador de uso do template também
        if (template != null) {
            template.incrementUsage();
        }
    }

    /**
     * Ativa a regra
     */
    public void activate() {
        this.isActive = true;
    }

    /**
     * Desativa a regra
     */
    public void deactivate() {
        this.isActive = false;
    }

    /**
     * Verifica se a regra tem delay
     */
    public boolean hasDelay() {
        return delayMinutes != null && delayMinutes > 0;
    }

    /**
     * Calcula quando a regra deve ser executada
     */
    public LocalDateTime getScheduledTime(LocalDateTime triggerTime) {
        if (!hasDelay()) {
            return triggerTime;
        }
        return triggerTime.plusMinutes(delayMinutes);
    }

    /**
     * Valida se o template é compatível com o canal
     */
    public boolean isTemplateCompatible() {
        return template != null && template.getChannel().equals(channel);
    }

    /**
     * Retorna a descrição do gatilho
     */
    public String getTriggerDescription() {
        return trigger.getDescription();
    }

    /**
     * Verifica se é uma regra de aniversário
     */
    public boolean isBirthdayRule() {
        return AutomationTrigger.CUSTOMER_BIRTHDAY.equals(trigger);
    }

    /**
     * Verifica se é uma regra de inatividade
     */
    public boolean isInactivityRule() {
        return trigger == AutomationTrigger.CUSTOMER_INACTIVE_30D ||
                trigger == AutomationTrigger.CUSTOMER_INACTIVE_60D;
    }

    /**
     * Retorna o ícone baseado no gatilho
     */
    public String getTriggerIcon() {
        return switch (trigger) {
            case CUSTOMER_CREATED -> "👤";
            case CUSTOMER_STATUS_CHANGED -> "🔄";
            case DEAL_CREATED -> "💰";
            case DEAL_STAGE_CHANGED -> "📊";
            case DEAL_WON -> "🏆";
            case DEAL_LOST -> "❌";
            case ACTIVITY_COMPLETED -> "✅";
            case CUSTOMER_BIRTHDAY -> "🎂";
            case CUSTOMER_INACTIVE_30D, CUSTOMER_INACTIVE_60D -> "⏰";
            case PROPOSAL_SENT -> "📄";
            case MANUAL_TRIGGER -> "✋";
        };
    }

    @Override
    public String toString() {
        return "AutomationRule{" +
                "id=" + getId() +
                ", name='" + name + '\'' +
                ", trigger=" + trigger +
                ", isActive=" + isActive +
                ", executionCount=" + executionCount +
                '}';
    }
}