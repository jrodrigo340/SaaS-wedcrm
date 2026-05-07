package com.wedcrm.repository;

import com.wedcrm.entity.Activity;
import com.wedcrm.entity.Customer;
import com.wedcrm.entity.Opportunity;
import com.wedcrm.entity.User;
import com.wedcrm.enums.ActivityStatus;
import com.wedcrm.enums.ActivityType;
import com.wedcrm.enums.Priority;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, UUID>, JpaSpecificationExecutor<Activity> {

    // ========== MÉTODOS DA DOCUMENTAÇÃO ==========

    /**
     * Atividades de um usuário por status
     */
    List<Activity> findByAssignedToAndStatus(User assignedTo, ActivityStatus status);

    /**
     * Atividades vencidas não concluídas
     */
    @Query("SELECT a FROM Activity a WHERE a.status != 'DONE' AND a.status != 'CANCELLED' " +
            "AND a.dueDate < :now")
    List<Activity> findOverdueActivities(@Param("now") LocalDateTime now);

    /**
     * Atividades que vencem hoje para um usuário
     */
    @Query("SELECT a FROM Activity a WHERE a.assignedTo = :user AND DATE(a.dueDate) = CURRENT_DATE " +
            "AND a.status != 'DONE' AND a.status != 'CANCELLED'")
    List<Activity> findActivitiesDueToday(@Param("user") User user);

    /**
     * Atividades de um cliente ordenadas por data
     */
    List<Activity> findByCustomerOrderByDueDateDesc(Customer customer);

    List<Activity> findByCustomerIdOrderByDueDateDesc(UUID customerId);

    /**
     * Atividades com lembrete pendente de envio
     */
    @Query("SELECT a FROM Activity a WHERE a.reminderAt IS NOT NULL " +
            "AND a.reminderSent = false AND a.reminderAt <= :now")
    List<Activity> findPendingReminders(@Param("now") LocalDateTime now);

    // ========== MÉTODOS ADICIONAIS ÚTEIS ==========

    /**
     * Atividades por status (sem filtro de usuário)
     */
    List<Activity> findByStatus(ActivityStatus status);

    /**
     * Atividades por prioridade
     */
    List<Activity> findByAssignedToAndPriority(User assignedTo, Priority priority);

    /**
     * Atividades por tipo
     */
    List<Activity> findByAssignedToAndType(User assignedTo, ActivityType type);

    /**
     * Atividades por cliente e status
     */
    List<Activity> findByCustomerAndStatus(Customer customer, ActivityStatus status);

    /**
     * Atividades por oportunidade e status
     */
    List<Activity> findByOpportunityAndStatus(Opportunity opportunity, ActivityStatus status);

    /**
     * Atividades de uma oportunidade ordenadas por data
     */
    List<Activity> findByOpportunityIdOrderByDueDateAsc(UUID opportunityId);

    /**
     * Conta atividades por status para um usuário
     */
    long countByAssignedToAndStatus(User assignedTo, ActivityStatus status);

    /**
     * Conta todas as atividades pendentes de um usuário
     */
    @Query("SELECT COUNT(a) FROM Activity a WHERE a.assignedTo = :user " +
            "AND a.status != 'DONE' AND a.status != 'CANCELLED'")
    long countPendingActivities(@Param("user") User user);

    /**
     * Atividades concluídas em um período
     */
    @Query("SELECT a FROM Activity a WHERE a.assignedTo = :user " +
            "AND a.status = 'DONE' AND a.completedAt BETWEEN :start AND :end")
    List<Activity> findCompletedInPeriod(@Param("user") User user,
                                         @Param("start") LocalDateTime start,
                                         @Param("end") LocalDateTime end);

    /**
     * Atividades da semana para um usuário (dashboard)
     */
    @Query("SELECT a FROM Activity a WHERE a.assignedTo = :user " +
            "AND a.dueDate BETWEEN :startOfWeek AND :endOfWeek " +
            "ORDER BY a.dueDate ASC")
    List<Activity> findWeekActivities(@Param("user") User user,
                                      @Param("startOfWeek") LocalDateTime startOfWeek,
                                      @Param("endOfWeek") LocalDateTime endOfWeek);

    /**
     * Atividades do mês para um usuário (calendário)
     */
    @Query("SELECT a FROM Activity a WHERE a.assignedTo = :user " +
            "AND FUNCTION('YEAR', a.dueDate) = :year AND FUNCTION('MONTH', a.dueDate) = :month " +
            "ORDER BY a.dueDate ASC")
    List<Activity> findMonthActivities(@Param("user") User user,
                                       @Param("year") int year,
                                       @Param("month") int month);

    /**
     * Atividades com alta prioridade (HIGH ou URGENT) e pendentes
     */
    @Query("SELECT a FROM Activity a WHERE a.assignedTo = :user " +
            "AND a.priority IN ('HIGH', 'URGENT') AND a.status = 'PENDING' " +
            "ORDER BY a.priority DESC, a.dueDate ASC")
    List<Activity> findHighPriorityPendingActivities(@Param("user") User user);

    /**
     * Atividades atrasadas de um usuário específico
     */
    @Query("SELECT a FROM Activity a WHERE a.assignedTo = :user " +
            "AND a.status != 'DONE' AND a.status != 'CANCELLED' AND a.dueDate < :now")
    List<Activity> findOverdueActivitiesForUser(@Param("user") User user,
                                                @Param("now") LocalDateTime now);

    /**
     * Quantidade de atividades atrasadas por usuário
     */
    @Query("SELECT a.assignedTo, COUNT(a) FROM Activity a " +
            "WHERE a.status != 'DONE' AND a.status != 'CANCELLED' AND a.dueDate < :now " +
            "GROUP BY a.assignedTo")
    List<Object[]> countOverdueActivitiesByUser(@Param("now") LocalDateTime now);

    /**
     * Conclui atividades vencidas automaticamente (cancela)
     */
    @Modifying
    @Transactional
    @Query("UPDATE Activity a SET a.status = 'CANCELLED', a.completedAt = :now " +
            "WHERE a.dueDate < :now AND a.status = 'PENDING'")
    int autoCancelOverdueActivities(@Param("now") LocalDateTime now);

    /**
     * Atividades por cliente (paginado)
     */
    Page<Activity> findByCustomerId(UUID customerId, Pageable pageable);

    /**
     * Atividades por vendedor (paginado)
     */
    Page<Activity> findByAssignedToId(UUID assignedToId, Pageable pageable);

    /**
     * Busca atividades por título (contém)
     */
    List<Activity> findByTitleContainingIgnoreCase(String title);

    /**
     * Atividades com vencimento entre datas
     */
    List<Activity> findByDueDateBetween(LocalDateTime start, LocalDateTime end);

    /**
     * Estatísticas de atividades por tipo para um usuário
     */
    @Query("SELECT a.type, COUNT(a) FROM Activity a WHERE a.assignedTo = :user GROUP BY a.type")
    List<Object[]> getStatsByTypeForUser(@Param("user") User user);

    /**
     * Estatísticas de atividades por prioridade para um usuário
     */
    @Query("SELECT a.priority, COUNT(a) FROM Activity a WHERE a.assignedTo = :user GROUP BY a.priority")
    List<Object[]> getStatsByPriorityForUser(@Param("user") User user);

    /**
     * Taxa de conclusão de atividades por usuário
     */
    @Query("SELECT a.assignedTo, " +
            "COUNT(CASE WHEN a.status = 'DONE' THEN 1 END) as completed, " +
            "COUNT(a) as total, " +
            "ROUND(COUNT(CASE WHEN a.status = 'DONE' THEN 1 END) * 100.0 / COUNT(a), 2) as completionRate " +
            "FROM Activity a WHERE a.assignedTo IS NOT NULL GROUP BY a.assignedTo")
    List<Object[]> getCompletionRateByUser();

    /**
     * Média de tempo para conclusão de atividades (em horas)
     */
    @Query("SELECT AVG(FUNCTION('TIMESTAMPDIFF', hour, a.createdAt, a.completedAt)) " +
            "FROM Activity a WHERE a.status = 'DONE' AND a.completedAt IS NOT NULL")
    Double getAverageCompletionTime();

    /**
     * Próximas atividades de um usuário (limitado)
     */
    @Query("SELECT a FROM Activity a WHERE a.assignedTo = :user " +
            "AND a.status = 'PENDING' AND a.dueDate > :now " +
            "ORDER BY a.dueDate ASC")
    List<Activity> findUpcomingActivities(@Param("user") User user,
                                          @Param("now") LocalDateTime now,
                                          Pageable pageable);

    /**
     * Atividades de hoje para todos os usuários (para job de notificação)
     */
    @Query("SELECT a FROM Activity a WHERE DATE(a.dueDate) = CURRENT_DATE " +
            "AND a.status != 'DONE' AND a.status != 'CANCELLED'")
    List<Activity> findAllActivitiesDueToday();

    /**
     * Lembretes pendentes para hoje
     */
    @Query("SELECT a FROM Activity a WHERE a.reminderAt IS NOT NULL " +
            "AND a.reminderSent = false AND DATE(a.reminderAt) <= CURRENT_DATE")
    List<Activity> findRemindersForToday();

    /**
     * Atualiza status de atividades para 'IN_PROGRESS'
     */
    @Modifying
    @Transactional
    @Query("UPDATE Activity a SET a.status = 'IN_PROGRESS' WHERE a.id = :id AND a.status = 'PENDING'")
    int startActivity(@Param("id") UUID id);

    /**
     * Remove atividades antigas (mais de 90 dias concluídas ou canceladas)
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM Activity a WHERE a.completedAt < :date " +
            "AND (a.status = 'DONE' OR a.status = 'CANCELLED')")
    int deleteOldCompletedActivities(@Param("date") LocalDateTime date);
}