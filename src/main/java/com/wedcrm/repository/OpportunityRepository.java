package com.wedcrm.repository;

import com.wedcrm.entity.Customer;
import com.wedcrm.entity.Opportunity;
import com.wedcrm.entity.PipelineStage;
import com.wedcrm.entity.User;
import com.wedcrm.enums.OpportunityStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OpportunityRepository extends JpaRepository<Opportunity, UUID>, JpaSpecificationExecutor<Opportunity> {

    // ========== MÉTODOS DA DOCUMENTAÇÃO ==========

    /**
     * Oportunidades de um cliente
     */
    List<Opportunity> findByCustomer(Customer customer);

    /**
     * Oportunidades em um estágio específico
     */
    List<Opportunity> findByStage(PipelineStage stage);

    /**
     * Oportunidades de um vendedor
     */
    List<Opportunity> findByAssignedTo(User user);

    /**
     * Filtra por status (OPEN, WON, LOST, ABANDONED)
     */
    List<Opportunity> findByStatus(OpportunityStatus status);

    /**
     * Oportunidades com fechamento previsto até uma data
     */
    @Query("SELECT o FROM Opportunity o WHERE o.expectedCloseDate <= :date AND o.status = 'OPEN'")
    List<Opportunity> findExpectedToCloseUntil(@Param("date") LocalDate date);

    /**
     * Soma o valor total por status (para métricas)
     */
    @Query("SELECT COALESCE(SUM(o.value), 0) FROM Opportunity o WHERE o.status = :status")
    BigDecimal sumValueByStatus(@Param("status") OpportunityStatus status);

    /**
     * Oportunidades em atraso (data passada e ainda abertas)
     */
    @Query("SELECT o FROM Opportunity o WHERE o.status = 'OPEN' AND o.expectedCloseDate < CURRENT_DATE")
    List<Opportunity> findByStatusAndExpectedCloseDateBefore();

    // ========== MÉTODOS ADICIONAIS ÚTEIS ==========

    /**
     * Oportunidades por cliente e status
     */
    List<Opportunity> findByCustomerAndStatus(Customer customer, OpportunityStatus status);

    /**
     * Oportunidades por vendedor e status
     */
    List<Opportunity> findByAssignedToAndStatus(User assignedTo, OpportunityStatus status);

    /**
     * Oportunidades por estágio e status
     */
    List<Opportunity> findByStageAndStatus(PipelineStage stage, OpportunityStatus status);

    /**
     * Oportunidades com valor acima de um limite
     */
    List<Opportunity> findByValueGreaterThan(BigDecimal value);

    /**
     * Oportunidades com valor entre dois limites
     */
    List<Opportunity> findByValueBetween(BigDecimal minValue, BigDecimal maxValue);

    /**
     * Oportunidades com alta probabilidade de fechamento
     */
    List<Opportunity> findByProbabilityGreaterThanEqual(int probability);

    /**
     * Busca oportunidades por título (contém)
     */
    List<Opportunity> findByTitleContainingIgnoreCase(String title);

    /**
     * Oportunidades paginadas por cliente
     */
    Page<Opportunity> findByCustomer(Customer customer, Pageable pageable);

    /**
     * Oportunidades paginadas por vendedor
     */
    Page<Opportunity> findByAssignedTo(User assignedTo, Pageable pageable);

    /**
     * Oportunidades a vencer nos próximos 7 dias
     */
    @Query("SELECT o FROM Opportunity o WHERE o.status = 'OPEN' " +
            "AND o.expectedCloseDate BETWEEN CURRENT_DATE AND CURRENT_DATE + 7")
    List<Opportunity> findOpportunitiesDueNextWeek();

    /**
     * Oportunidades a vencer nos próximos 30 dias
     */
    @Query("SELECT o FROM Opportunity o WHERE o.status = 'OPEN' " +
            "AND o.expectedCloseDate BETWEEN CURRENT_DATE AND CURRENT_DATE + 30")
    List<Opportunity> findOpportunitiesDueNextMonth();

    /**
     * Conta oportunidades por estágio
     */
    long countByStage(PipelineStage stage);

    /**
     * Conta oportunidades por status
     */
    long countByStatus(OpportunityStatus status);

    /**
     * Valor total de oportunidades abertas por vendedor
     */
    @Query("SELECT o.assignedTo, COALESCE(SUM(o.value), 0) FROM Opportunity o " +
            "WHERE o.status = 'OPEN' AND o.assignedTo IS NOT NULL GROUP BY o.assignedTo")
    List<Object[]> getPipelineValueBySeller();

    /**
     * Valor total de oportunidades ganhas por vendedor
     */
    @Query("SELECT o.assignedTo, COALESCE(SUM(o.value), 0) FROM Opportunity o " +
            "WHERE o.status = 'WON' AND o.assignedTo IS NOT NULL GROUP BY o.assignedTo")
    List<Object[]> getWonValueBySeller();

    /**
     * Quantidade de oportunidades por estágio (para pipeline)
     */
    @Query("SELECT o.stage, COUNT(o), COALESCE(SUM(o.value), 0) FROM Opportunity o " +
            "WHERE o.status = 'OPEN' GROUP BY o.stage ORDER BY o.stage.order ASC")
    List<Object[]> getPipelineSummary();

    /**
     * Taxa de conversão por vendedor
     */
    @Query("SELECT o.assignedTo, " +
            "COUNT(CASE WHEN o.status = 'WON' THEN 1 END) as won, " +
            "COUNT(o) as total, " +
            "ROUND(COUNT(CASE WHEN o.status = 'WON' THEN 1 END) * 100.0 / COUNT(o), 2) as conversionRate " +
            "FROM Opportunity o WHERE o.assignedTo IS NOT NULL GROUP BY o.assignedTo")
    List<Object[]> getConversionRateBySeller();

    /**
     * Taxa de conversão por estágio
     */
    @Query("SELECT o.stage, COUNT(o), " +
            "COUNT(CASE WHEN o.status = 'WON' THEN 1 END) " +
            "FROM Opportunity o GROUP BY o.stage")
    List<Object[]> getConversionRateByStage();

    /**
     * Tempo médio de fechamento (dias)
     */
    @Query("SELECT AVG(FUNCTION('DATEDIFF', o.closedAt, o.createdAt)) " +
            "FROM Opportunity o WHERE o.status = 'WON' AND o.closedAt IS NOT NULL")
    Double getAverageClosingTime();

    /**
     * Perda de oportunidades por motivo
     */
    @Query("SELECT o.lostReason, COUNT(o) FROM Opportunity o " +
            "WHERE o.status = 'LOST' AND o.lostReason IS NOT NULL GROUP BY o.lostReason")
    List<Object[]> getLostReasons();

    /**
     * Oportunidades ganhas no período
     */
    @Query("SELECT o FROM Opportunity o WHERE o.status = 'WON' " +
            "AND o.closedAt BETWEEN :startDate AND :endDate")
    List<Opportunity> findWonInPeriod(@Param("startDate") LocalDateTime startDate,
                                      @Param("endDate") LocalDateTime endDate);

    /**
     * Valor total ganho no período
     */
    @Query("SELECT COALESCE(SUM(o.value), 0) FROM Opportunity o " +
            "WHERE o.status = 'WON' AND o.closedAt BETWEEN :startDate AND :endDate")
    BigDecimal getWonValueInPeriod(@Param("startDate") LocalDateTime startDate,
                                   @Param("endDate") LocalDateTime endDate);

    /**
     * Pipeline Value - Soma do valor por estágio
     */
    @Query("SELECT o.stage.name, o.stage.order, o.stage.color, " +
            "COUNT(o), COALESCE(SUM(o.value), 0) FROM Opportunity o " +
            "WHERE o.status = 'OPEN' GROUP BY o.stage.name, o.stage.order, o.stage.color " +
            "ORDER BY o.stage.order ASC")
    List<Object[]> getPipelineKanban();

    /**
     * Atualiza probabilidade de oportunidades vencidas
     */
    @Modifying
    @Transactional
    @Query("UPDATE Opportunity o SET o.probability = o.probability - 10 " +
            "WHERE o.status = 'OPEN' AND o.expectedCloseDate < CURRENT_DATE AND o.probability > 0")
    int reduceProbabilityForOverdue();

    /**
     * Fecha oportunidades há muito tempo abertas
     */
    @Modifying
    @Transactional
    @Query("UPDATE Opportunity o SET o.status = 'ABANDONED', o.closedAt = CURRENT_TIMESTAMP " +
            "WHERE o.status = 'OPEN' AND o.updatedAt < :date")
    int abandonOldOpportunities(@Param("date") LocalDateTime date);

    /**
     * Oportunidades por cliente (paginação)
     */
    Page<Opportunity> findByCustomerId(UUID customerId, Pageable pageable);

    /**
     * Oportunidades recentes (últimos 30 dias)
     */
    @Query("SELECT o FROM Opportunity o WHERE o.createdAt >= :since ORDER BY o.createdAt DESC")
    List<Opportunity> findRecentOpportunities(@Param("since") LocalDateTime since);

    /**
     * Oportunidades com maior valor
     */
    List<Opportunity> findTop10ByOrderByValueDesc();

    /**
     * Média de valor por oportunidade (apenas ganhas)
     */
    @Query("SELECT AVG(o.value) FROM Opportunity o WHERE o.status = 'WON'")
    Double getAverageWonValue();

    /**
     * Total de oportunidades abertas
     */
    @Query("SELECT COUNT(o) FROM Opportunity o WHERE o.status = 'OPEN'")
    long countOpenOpportunities();

    /**
     * Soma total de todas as oportunidades abertas
     */
    @Query("SELECT COALESCE(SUM(o.value), 0) FROM Opportunity o WHERE o.status = 'OPEN'")
    BigDecimal getTotalPipelineValue();

    /**
     * Estatísticas completas do pipeline
     */
    @Query("SELECT " +
            "COUNT(CASE WHEN o.status = 'OPEN' THEN 1 END) as openCount, " +
            "COALESCE(SUM(CASE WHEN o.status = 'OPEN' THEN o.value END), 0) as openValue, " +
            "COUNT(CASE WHEN o.status = 'WON' THEN 1 END) as wonCount, " +
            "COALESCE(SUM(CASE WHEN o.status = 'WON' THEN o.value END), 0) as wonValue, " +
            "COUNT(CASE WHEN o.status = 'LOST' THEN 1 END) as lostCount, " +
            "COUNT(CASE WHEN o.status = 'ABANDONED' THEN 1 END) as abandonedCount " +
            "FROM Opportunity o")
    Object[] getCompleteStats();

    /**
     * Oportunidades por cliente com mais valor
     */
    @Query("SELECT o.customer, COUNT(o), COALESCE(SUM(o.value), 0) FROM Opportunity o " +
            "WHERE o.status = 'WON' GROUP BY o.customer ORDER BY SUM(o.value) DESC")
    List<Object[]> getTopCustomersByValue();

    /**
     * Oportunidades que estão em estágio final
     */
    @Query("SELECT o FROM Opportunity o WHERE o.stage.isWon = true OR o.stage.isLost = true")
    List<Opportunity> findOpportunitiesInFinalStage();
}