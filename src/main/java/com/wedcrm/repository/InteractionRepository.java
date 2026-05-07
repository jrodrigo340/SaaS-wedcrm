package com.wedcrm.repository;

import com.wedcrm.entity.Customer;
import com.wedcrm.entity.Interaction;
import com.wedcrm.entity.User;
import com.wedcrm.enums.Direction;
import com.wedcrm.enums.InteractionStatus;
import com.wedcrm.enums.InteractionType;
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
import java.util.UUID;

@Repository
public interface InteractionRepository extends JpaRepository<Interaction, UUID>, JpaSpecificationExecutor<Interaction> {

    // ========== MÉTODOS DA DOCUMENTAÇÃO ==========

    /**
     * Histórico paginado de um cliente
     */
    Page<Interaction> findByCustomerOrderByCreatedAtDesc(Customer customer, Pageable pageable);

    /**
     * Interações filtradas por tipo
     */
    List<Interaction> findByCustomerAndType(Customer customer, InteractionType type);

    /**
     * Contagem de interações em um período
     */
    long countByCustomerAndCreatedAtBetween(Customer customer, LocalDateTime start, LocalDateTime end);

    /**
     * Todas as interações geradas automaticamente
     */
    List<Interaction> findByIsAutomaticTrue();

    // ========== MÉTODOS ADICIONAIS ÚTEIS ==========

    /**
     * Histórico paginado de um cliente (por ID)
     */
    Page<Interaction> findByCustomerIdOrderByCreatedAtDesc(UUID customerId, Pageable pageable);

    /**
     * Todas interações de um cliente ordenadas por data (descendente)
     */
    List<Interaction> findByCustomerOrderByCreatedAtDesc(Customer customer);

    /**
     * Interações por cliente e direção (recebido/enviado)
     */
    List<Interaction> findByCustomerAndDirection(Customer customer, Direction direction);

    /**
     * Interações por cliente e status
     */
    List<Interaction> findByCustomerAndStatus(Customer customer, InteractionStatus status);

    /**
     * Interações não lidas de um cliente (inbound)
     */
    @Query("SELECT i FROM Interaction i WHERE i.customer = :customer " +
            "AND i.direction = 'INBOUND' AND (i.readAt IS NULL OR i.status != 'READ')")
    List<Interaction> findUnreadInboundInteractions(@Param("customer") Customer customer);

    /**
     * Interações por usuário que registrou
     */
    List<Interaction> findByUserOrderByCreatedAtDesc(User user);

    /**
     * Interações por usuário em um período
     */
    List<Interaction> findByUserAndCreatedAtBetween(User user, LocalDateTime start, LocalDateTime end);

    /**
     * Últimas N interações de um cliente
     */
    @Query("SELECT i FROM Interaction i WHERE i.customer = :customer ORDER BY i.createdAt DESC")
    List<Interaction> findLastInteractions(@Param("customer") Customer customer, Pageable pageable);

    /**
     * Interações por tipo e período
     */
    @Query("SELECT i FROM Interaction i WHERE i.type = :type AND i.createdAt BETWEEN :start AND :end")
    List<Interaction> findByTypeAndDateRange(@Param("type") InteractionType type,
                                             @Param("start") LocalDateTime start,
                                             @Param("end") LocalDateTime end);

    /**
     * Interações automáticas por template
     */
    @Query("SELECT i FROM Interaction i WHERE i.isAutomatic = true AND i.templateUsed.id = :templateId")
    List<Interaction> findAutomaticByTemplate(@Param("templateId") UUID templateId);

    /**
     * Contagem de interações por tipo para um cliente
     */
    @Query("SELECT i.type, COUNT(i) FROM Interaction i WHERE i.customer = :customer GROUP BY i.type")
    List<Object[]> countByTypeForCustomer(@Param("customer") Customer customer);

    /**
     * Contagem de interações por direção para um cliente
     */
    @Query("SELECT i.direction, COUNT(i) FROM Interaction i WHERE i.customer = :customer GROUP BY i.direction")
    List<Object[]> countByDirectionForCustomer(@Param("customer") Customer customer);

    /**
     * Contagem de interações por status para um cliente
     */
    @Query("SELECT i.status, COUNT(i) FROM Interaction i WHERE i.customer = :customer GROUP BY i.status")
    List<Object[]> countByStatusForCustomer(@Param("customer") Customer customer);

    /**
     * Contagem de interações automáticas vs manuais
     */
    @Query("SELECT i.isAutomatic, COUNT(i) FROM Interaction i WHERE i.customer = :customer GROUP BY i.isAutomatic")
    List<Object[]> countAutomaticVsManual(@Param("customer") Customer customer);

    /**
     * Estatísticas de interações por cliente (últimos 30 dias)
     */
    @Query("SELECT i.customer, COUNT(i) FROM Interaction i " +
            "WHERE i.createdAt >= :since GROUP BY i.customer ORDER BY COUNT(i) DESC")
    List<Object[]> getRecentInteractionStats(@Param("since") LocalDateTime since, Pageable pageable);

    /**
     * Média de interações por cliente
     */
    @Query("SELECT AVG(interactionCount) FROM (SELECT COUNT(i) as interactionCount FROM Interaction i GROUP BY i.customer)")
    Double getAverageInteractionsPerCustomer();

    /**
     * Interações por canal
     */
    @Query("SELECT i.channel, COUNT(i) FROM Interaction i WHERE i.channel IS NOT NULL GROUP BY i.channel")
    List<Object[]> countByChannel();

    /**
     * Média de tempo de resposta (em minutos)
     */
    @Query("SELECT AVG(FUNCTION('TIMESTAMPDIFF', MINUTE, i.sentAt, i.readAt)) " +
            "FROM Interaction i WHERE i.readAt IS NOT NULL")
    Double getAverageResponseTime();

    /**
     * Tempo médio de resposta por cliente
     */
    @Query("SELECT i.customer, AVG(FUNCTION('TIMESTAMPDIFF', MINUTE, i.sentAt, i.readAt)) " +
            "FROM Interaction i WHERE i.readAt IS NOT NULL GROUP BY i.customer")
    List<Object[]> getAverageResponseTimeByCustomer();

    /**
     * Interações com falha (bounced ou failed)
     */
    @Query("SELECT i FROM Interaction i WHERE i.status IN ('BOUNCED', 'FAILED')")
    List<Interaction> findFailedInteractions();

    /**
     * Reagendar interações com falha
     */
    @Modifying
    @Transactional
    @Query("UPDATE Interaction i SET i.status = 'SENT', i.sentAt = CURRENT_TIMESTAMP " +
            "WHERE i.status IN ('BOUNCED', 'FAILED') AND i.createdAt > :since")
    int retryFailedInteractions(@Param("since") LocalDateTime since);

    /**
     * Marca interações como lidas em lote
     */
    @Modifying
    @Transactional
    @Query("UPDATE Interaction i SET i.status = 'READ', i.readAt = :now " +
            "WHERE i.customer = :customer AND i.direction = 'INBOUND' AND i.status != 'READ'")
    int markAllAsReadForCustomer(@Param("customer") Customer customer, @Param("now") LocalDateTime now);

    /**
     * Interações por período (resumo)
     */
    @Query("SELECT FUNCTION('DATE', i.createdAt) as date, COUNT(i), " +
            "SUM(CASE WHEN i.direction = 'OUTBOUND' THEN 1 ELSE 0 END) as outbound, " +
            "SUM(CASE WHEN i.direction = 'INBOUND' THEN 1 ELSE 0 END) as inbound " +
            "FROM Interaction i WHERE i.createdAt BETWEEN :start AND :end " +
            "GROUP BY FUNCTION('DATE', i.createdAt) ORDER BY date")
    List<Object[]> getDailyInteractionSummary(@Param("start") LocalDateTime start,
                                              @Param("end") LocalDateTime end);

    /**
     * Interações por mês (para dashboard)
     */
    @Query("SELECT YEAR(i.createdAt) as year, MONTH(i.createdAt) as month, COUNT(i) " +
            "FROM Interaction i WHERE i.createdAt >= :since GROUP BY YEAR(i.createdAt), MONTH(i.createdAt) " +
            "ORDER BY year DESC, month DESC")
    List<Object[]> getMonthlyInteractionStats(@Param("since") LocalDateTime since);

    /**
     * Busca interações por conteúdo (full-text)
     */
    @Query("SELECT i FROM Interaction i WHERE i.customer = :customer AND " +
            "LOWER(i.content) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(i.subject) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Interaction> searchInteractions(@Param("customer") Customer customer,
                                         @Param("searchTerm") String searchTerm);

    /**
     * Interações automáticas por categoria de template
     */
    @Query("SELECT i FROM Interaction i WHERE i.isAutomatic = true AND i.templateUsed.category = :category")
    List<Interaction> findAutomaticByTemplateCategory(@Param("category") String category);

    /**
     * Última interação de cada cliente
     */
    @Query("SELECT i FROM Interaction i WHERE i.createdAt = " +
            "(SELECT MAX(i2.createdAt) FROM Interaction i2 WHERE i2.customer = i.customer)")
    List<Interaction> findLastInteractionPerCustomer();

    /**
     * Clientes sem interação há mais de X dias
     */
    @Query("SELECT DISTINCT i.customer FROM Interaction i WHERE i.customer NOT IN " +
            "(SELECT i2.customer FROM Interaction i2 WHERE i2.createdAt > :since)")
    List<Customer> findCustomersWithoutInteractionSince(@Param("since") LocalDateTime since);

    /**
     * Contagem total de interações no sistema
     */
    @Query("SELECT COUNT(i) FROM Interaction i")
    long getTotalInteractionCount();

    /**
     * Interações por hora do dia (para análise de melhor horário)
     */
    @Query("SELECT HOUR(i.createdAt) as hour, COUNT(i) FROM Interaction i " +
            "WHERE i.direction = 'OUTBOUND' GROUP BY HOUR(i.createdAt) ORDER BY hour")
    List<Object[]> getInteractionByHourOfDay();

    /**
     * Taxa de abertura de mensagens (leitura vs enviadas)
     */
    @Query("SELECT " +
            "COUNT(i) as total, " +
            "SUM(CASE WHEN i.status = 'READ' THEN 1 ELSE 0 END) as read, " +
            "ROUND(SUM(CASE WHEN i.status = 'READ' THEN 1 ELSE 0 END) * 100.0 / COUNT(i), 2) as openRate " +
            "FROM Interaction i WHERE i.type = 'EMAIL'")
    Object[] getEmailOpenRate();

    /**
     * Remove interações antigas (mais de 1 ano)
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM Interaction i WHERE i.createdAt < :date")
    int deleteOldInteractions(@Param("date") LocalDateTime date);
}