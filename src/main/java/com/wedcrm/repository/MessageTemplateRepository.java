package com.wedcrm.repository;

import com.wedcrm.entity.MessageTemplate;
import com.wedcrm.enums.MessageChannel;
import com.wedcrm.enums.TemplateCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MessageTemplateRepository extends JpaRepository<MessageTemplate, UUID>, JpaSpecificationExecutor<MessageTemplate> {

    // ========== MÉTODOS DA DOCUMENTAÇÃO ==========

    /**
     * Templates ativos por canal
     */
    List<MessageTemplate> findByChannelAndIsActiveTrue(MessageChannel channel);

    /**
     * Templates por categoria e canal (ambos ativos)
     */
    List<MessageTemplate> findByCategoryAndChannelAndIsActiveTrue(TemplateCategory category, MessageChannel channel);

    /**
     * Templates mais utilizados (ordenados por usageCount)
     */
    @Query("SELECT t FROM MessageTemplate t WHERE t.isActive = true ORDER BY t.usageCount DESC")
    List<MessageTemplate> findMostUsed(Pageable pageable);

    // ========== MÉTODOS ADICIONAIS ÚTEIS ==========

    /**
     * Busca template por nome (único)
     */
    Optional<MessageTemplate> findByName(String name);

    /**
     * Busca template por nome (case insensitive)
     */
    Optional<MessageTemplate> findByNameIgnoreCase(String name);

    /**
     * Verifica se nome já existe
     */
    boolean existsByName(String name);

    /**
     * Verifica se nome já existe (excluindo um template específico - útil para updates)
     */
    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END FROM MessageTemplate t " +
            "WHERE t.name = :name AND t.id != :excludeId")
    boolean existsByNameAndIdNot(@Param("name") String name, @Param("excludeId") UUID excludeId);

    /**
     * Templates ativos por categoria
     */
    List<MessageTemplate> findByCategoryAndIsActiveTrue(TemplateCategory category);

    /**
     * Templates ativos por canal (com paginação)
     */
    Page<MessageTemplate> findByChannelAndIsActiveTrue(MessageChannel channel, Pageable pageable);

    /**
     * Templates ativos por categoria (com paginação)
     */
    Page<MessageTemplate> findByCategoryAndIsActiveTrue(TemplateCategory category, Pageable pageable);

    /**
     * Todos os templates ativos
     */
    List<MessageTemplate> findByIsActiveTrue();

    /**
     * Todos os templates inativos
     */
    List<MessageTemplate> findByIsActiveFalse();

    /**
     * Templates por idioma
     */
    List<MessageTemplate> findByLanguageAndIsActiveTrue(String language);

    /**
     * Busca templates por nome contendo (case insensitive)
     */
    List<MessageTemplate> findByNameContainingIgnoreCase(String name);

    /**
     * Busca templates por descrição contendo
     */
    List<MessageTemplate> findByDescriptionContainingIgnoreCase(String description);

    /**
     * Templates que utilizam uma variável específica
     */
    @Query("SELECT t FROM MessageTemplate t WHERE :variable MEMBER OF t.variables")
    List<MessageTemplate> findByVariable(@Param("variable") String variable);

    /**
     * Templates que utilizam pelo menos uma das variáveis
     */
    @Query("SELECT t FROM MessageTemplate t WHERE " +
            "EXISTS (SELECT v FROM t.variables v WHERE v IN :variables)")
    List<MessageTemplate> findByAnyVariable(@Param("variables") List<String> variables);

    /**
     * Templates sem variáveis
     */
    @Query("SELECT t FROM MessageTemplate t WHERE SIZE(t.variables) = 0")
    List<MessageTemplate> findTemplatesWithoutVariables();

    /**
     * Contagem de templates por canal
     */
    @Query("SELECT t.channel, COUNT(t) FROM MessageTemplate t GROUP BY t.channel")
    List<Object[]> countByChannel();

    /**
     * Contagem de templates por categoria
     */
    @Query("SELECT t.category, COUNT(t) FROM MessageTemplate t GROUP BY t.category")
    List<Object[]> countByCategory();

    /**
     * Estatísticas de uso por categoria
     */
    @Query("SELECT t.category, SUM(t.usageCount) FROM MessageTemplate t " +
            "WHERE t.isActive = true GROUP BY t.category ORDER BY SUM(t.usageCount) DESC")
    List<Object[]> getUsageStatsByCategory();

    /**
     * Estatísticas de uso por canal
     */
    @Query("SELECT t.channel, SUM(t.usageCount) FROM MessageTemplate t " +
            "WHERE t.isActive = true GROUP BY t.channel ORDER BY SUM(t.usageCount) DESC")
    List<Object[]> getUsageStatsByChannel();

    /**
     * Template mais utilizado globalmente
     */
    @Query("SELECT t FROM MessageTemplate t WHERE t.isActive = true " +
            "ORDER BY t.usageCount DESC")
    List<MessageTemplate> findTopTemplate(Pageable pageable);

    /**
     * Templates menos utilizados (para possível remoção)
     */
    @Query("SELECT t FROM MessageTemplate t WHERE t.isActive = true " +
            "AND t.usageCount = 0")
    List<MessageTemplate> findUnusedTemplates();

    /**
     * Templates por categoria e idioma
     */
    List<MessageTemplate> findByCategoryAndLanguage(TemplateCategory category, String language);

    /**
     * Templates de e-mail com assunto válido
     */
    @Query("SELECT t FROM MessageTemplate t WHERE t.channel = 'EMAIL' " +
            "AND t.subject IS NOT NULL AND t.subject != ''")
    List<MessageTemplate> findEmailTemplatesWithSubject();

    /**
     * Templates sem assunto (apenas e-mail)
     */
    @Query("SELECT t FROM MessageTemplate t WHERE t.channel = 'EMAIL' " +
            "AND (t.subject IS NULL OR t.subject = '')")
    List<MessageTemplate> findEmailTemplatesWithoutSubject();

    /**
     * Incrementa o contador de uso de um template
     */
    @Modifying
    @Transactional
    @Query("UPDATE MessageTemplate t SET t.usageCount = t.usageCount + 1 WHERE t.id = :id")
    void incrementUsageCount(@Param("id") UUID id);

    /**
     * Reseta contadores de uso (útil para relatórios periódicos)
     */
    @Modifying
    @Transactional
    @Query("UPDATE MessageTemplate t SET t.usageCount = 0")
    void resetAllUsageCounts();

    /**
     * Ativa um template
     */
    @Modifying
    @Transactional
    @Query("UPDATE MessageTemplate t SET t.isActive = true WHERE t.id = :id")
    void activateTemplate(@Param("id") UUID id);

    /**
     * Desativa um template
     */
    @Modifying
    @Transactional
    @Query("UPDATE MessageTemplate t SET t.isActive = false WHERE t.id = :id")
    void deactivateTemplate(@Param("id") UUID id);

    /**
     * Busca templates por múltiplas categorias
     */
    List<MessageTemplate> findByCategoryIn(List<TemplateCategory> categories);

    /**
     * Busca templates por múltiplos canais
     */
    List<MessageTemplate> findByChannelIn(List<MessageChannel> channels);

    /**
     * Templates criados recentemente
     */
    List<MessageTemplate> findTop10ByOrderByCreatedAtDesc();

    /**
     * Templates mais antigos
     */
    List<MessageTemplate> findTop10ByOrderByCreatedAtAsc();

    /**
     * Conta templates por status (ativo/inativo)
     */
    @Query("SELECT " +
            "SUM(CASE WHEN t.isActive = true THEN 1 ELSE 0 END) as active, " +
            "SUM(CASE WHEN t.isActive = false THEN 1 ELSE 0 END) as inactive " +
            "FROM MessageTemplate t")
    Object[] countByActiveStatus();

    /**
     * Média de uso dos templates
     */
    @Query("SELECT AVG(t.usageCount) FROM MessageTemplate t")
    Double getAverageUsageCount();

    /**
     * Templates com uso acima da média
     */
    @Query("SELECT t FROM MessageTemplate t WHERE t.usageCount > (SELECT AVG(usageCount) FROM MessageTemplate)")
    List<MessageTemplate> findTemplatesAboveAverageUsage();
}