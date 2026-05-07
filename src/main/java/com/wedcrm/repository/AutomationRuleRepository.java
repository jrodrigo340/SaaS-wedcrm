package com.wedcrm.repository;

import com.wedcrm.entity.AutomationRule;
import com.wedcrm.enums.AutomationTrigger;
import com.wedcrm.enums.MessageChannel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AutomationRuleRepository extends JpaRepository<AutomationRule, UUID>, JpaSpecificationExecutor<AutomationRule> {

    // ========== MÉTODOS DA DOCUMENTAÇÃO ==========

    /**
     * Regras ativas para um gatilho específico
     */
    List<AutomationRule> findByTriggerAndIsActiveTrue(AutomationTrigger trigger);

    /**
     * Todas as regras ativas
     */
    List<AutomationRule> findAllByIsActiveTrue();

    // ========== MÉTODOS ADICIONAIS ÚTEIS ==========

    /**
     * Busca regra por nome
     */
    Optional<AutomationRule> findByName(String name);

    /**
     * Busca regra por nome (case insensitive)
     */
    Optional<AutomationRule> findByNameIgnoreCase(String name);

    /**
     * Verifica se nome já existe
     */
    boolean existsByName(String name);

    /**
     * Verifica se nome já existe (excluindo uma regra específica)
     */
    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM AutomationRule r " +
            "WHERE r.name = :name AND r.id != :excludeId")
    boolean existsByNameAndIdNot(@Param("name") String name, @Param("excludeId") UUID excludeId);

    /**
     * Regras ativas por gatilho (com paginação)
     */
    Page<AutomationRule> findByTriggerAndIsActiveTrue(AutomationTrigger trigger, Pageable pageable);

    /**
     * Regras ativas por canal
     */
    List<AutomationRule> findByChannelAndIsActiveTrue(MessageChannel channel);

    /**
     * Regras ativas por gatilho e canal
     */
    List<AutomationRule> findByTriggerAndChannelAndIsActiveTrue(AutomationTrigger trigger, MessageChannel channel);

    /**
     * Regras por template (para saber onde um template é usado)
     */
    List<AutomationRule> findByTemplateId(UUID templateId);

    /**
     * Regras inativas
     */
    List<AutomationRule> findByIsActiveFalse();

    /**
     * Regras com delay positivo (agendadas)
     */
    List<AutomationRule> findByDelayMinutesGreaterThanAndIsActiveTrue(int delayMinutes);

    /**
     * Regras sem delay (execução imediata)
     */
    List<AutomationRule> findByDelayMinutesAndIsActiveTrue(int delayMinutes);

    /**
     * Regras ordenadas por contagem de execução (decrescente)
     */
    List<AutomationRule> findAllByOrderByExecutionCountDesc();

    /**
     * Regras mais executadas (top N)
     */
    List<AutomationRule> findTop10ByOrderByExecutionCountDesc();

    /**
     * Regras nunca executadas
     */
    @Query("SELECT r FROM AutomationRule r WHERE r.executionCount = 0")
    List<AutomationRule> findNeverExecutedRules();

    /**
     * Regras executadas recentemente
     */
    @Query("SELECT r FROM AutomationRule r WHERE r.lastExecutedAt IS NOT NULL " +
            "ORDER BY r.lastExecutedAt DESC")
    List<AutomationRule> findRecentlyExecutedRules(Pageable pageable);

    /**
     * Regras não executadas há mais de X dias
     */
    @Query("SELECT r FROM AutomationRule r WHERE r.lastExecutedAt < :date OR r.lastExecutedAt IS NULL")
    List<AutomationRule> findRulesNotExecutedSince(@Param("date") LocalDateTime date);

    /**
     * Contagem de regras por gatilho
     */
    @Query("SELECT r.trigger, COUNT(r) FROM AutomationRule r " +
            "WHERE r.isActive = true GROUP BY r.trigger")
    List<Object[]> countByTrigger();

    /**
     * Contagem de regras por canal
     */
    @Query("SELECT r.channel, COUNT(r) FROM AutomationRule r " +
            "WHERE r.isActive = true GROUP BY r.channel")
    List<Object[]> countByChannel();

    /**
     * Total de execuções por gatilho
     */
    @Query("SELECT r.trigger, SUM(r.executionCount) FROM AutomationRule r " +
            "GROUP BY r.trigger ORDER BY SUM(r.executionCount) DESC")
    List<Object[]> getExecutionStatsByTrigger();

    /**
     * Total de execuções por canal
     */
    @Query("SELECT r.channel, SUM(r.executionCount) FROM AutomationRule r " +
            "GROUP BY r.channel ORDER BY SUM(r.executionCount) DESC")
    List<Object[]> getExecutionStatsByChannel();

    /**
     * Média de execuções por regra
     */
    @Query("SELECT AVG(r.executionCount) FROM AutomationRule r")
    Double getAverageExecutionCount();

    /**
     * Regras com execução acima da média
     */
    @Query("SELECT r FROM AutomationRule r WHERE r.executionCount > " +
            "(SELECT AVG(executionCount) FROM AutomationRule)")
    List<AutomationRule> findRulesAboveAverageExecution();

    /**
     * Ativa uma regra
     */
    @Modifying
    @Transactional
    @Query("UPDATE AutomationRule r SET r.isActive = true WHERE r.id = :id")
    void activateRule(@Param("id") UUID id);

    /**
     * Desativa uma regra
     */
    @Modifying
    @Transactional
    @Query("UPDATE AutomationRule r SET r.isActive = false WHERE r.id = :id")
    void deactivateRule(@Param("id") UUID id);

    /**
     * Desativa todas as regras de um template
     */
    @Modifying
    @Transactional
    @Query("UPDATE AutomationRule r SET r.isActive = false WHERE r.template.id = :templateId")
    void deactivateByTemplate(@Param("templateId") UUID templateId);

    /**
     * Atualiza o último horário de execução
     */
    @Modifying
    @Transactional
    @Query("UPDATE AutomationRule r SET r.lastExecutedAt = :executedAt, r.executionCount = r.executionCount + 1 " +
            "WHERE r.id = :id")
    void registerExecution(@Param("id") UUID id, @Param("executedAt") LocalDateTime executedAt);

    /**
     * Reseta o contador de execuções de todas as regras
     */
    @Modifying
    @Transactional
    @Query("UPDATE AutomationRule r SET r.executionCount = 0")
    void resetAllExecutionCounts();

    /**
     * Reseta o contador de execuções de uma regra específica
     */
    @Modifying
    @Transactional
    @Query("UPDATE AutomationRule r SET r.executionCount = 0 WHERE r.id = :id")
    void resetExecutionCount(@Param("id") UUID id);

    /**
     * Busca regras por trigger (incluindo inativas)
     */
    List<AutomationRule> findByTrigger(AutomationTrigger trigger);

    /**
     * Regras de aniversário ativas
     */
    @Query("SELECT r FROM AutomationRule r WHERE r.trigger = 'CUSTOMER_BIRTHDAY' AND r.isActive = true")
    List<AutomationRule> findBirthdayRules();

    /**
     * Regras de inatividade ativas
     */
    @Query("SELECT r FROM AutomationRule r WHERE r.trigger IN ('CUSTOMER_INACTIVE_30D', 'CUSTOMER_INACTIVE_60D') " +
            "AND r.isActive = true")
    List<AutomationRule> findInactivityRules();

    /**
     * Regras de mudança de estágio ativas
     */
    @Query("SELECT r FROM AutomationRule r WHERE r.trigger = 'DEAL_STAGE_CHANGED' AND r.isActive = true")
    List<AutomationRule> findStageChangeRules();

    /**
     * Regras manuais (disparo por botão)
     */
    @Query("SELECT r FROM AutomationRule r WHERE r.trigger = 'MANUAL_TRIGGER' AND r.isActive = true")
    List<AutomationRule> findManualRules();

    /**
     * Busca regras por múltiplos triggers
     */
    List<AutomationRule> findByTriggerIn(List<AutomationTrigger> triggers);

    /**
     * Regras com delay específico
     */
    List<AutomationRule> findByDelayMinutes(int delayMinutes);

    /**
     * Estatísticas completas das regras
     */
    @Query("SELECT " +
            "COUNT(r) as total, " +
            "SUM(CASE WHEN r.isActive = true THEN 1 ELSE 0 END) as active, " +
            "SUM(CASE WHEN r.isActive = false THEN 1 ELSE 0 END) as inactive, " +
            "SUM(r.executionCount) as totalExecutions, " +
            "AVG(r.executionCount) as avgExecutions " +
            "FROM AutomationRule r")
    Object[] getCompleteStats();

    /**
     * Regras criadas recentemente
     */
    List<AutomationRule> findTop10ByOrderByCreatedAtDesc();

    /**
     * Regras que usam um canal específico
     */
    List<AutomationRule> findByChannel(MessageChannel channel);

    /**
     * Busca regras por condição específica (usando JSON)
     */
    @Query("SELECT r FROM AutomationRule r WHERE r.conditions LIKE CONCAT('%', :condition, '%')")
    List<AutomationRule> findByConditionContaining(@Param("condition") String condition);
}