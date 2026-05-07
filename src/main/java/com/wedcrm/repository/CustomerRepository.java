package com.wedcrm.repository;

import com.wedcrm.entity.Customer;
import com.wedcrm.entity.Tag;
import com.wedcrm.entity.User;
import com.wedcrm.enums.CustomerStatus;
import com.wedcrm.enums.LeadSource;
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
import java.time.MonthDay;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID>, JpaSpecificationExecutor<Customer> {

    // ========== MÉTODOS DA DOCUMENTAÇÃO ==========

    /**
     * Filtra clientes pelo status (LEAD, PROSPECT, CUSTOMER, INACTIVE, LOST)
     */
    List<Customer> findByStatus(CustomerStatus status);

    /**
     * Clientes atribuídos a um vendedor específico
     */
    List<Customer> findByAssignedTo(User user);

    /**
     * Busca por e-mail (case insensitive)
     */
    Optional<Customer> findByEmailIgnoreCase(String email);

    /**
     * Busca full-text em nome, e-mail e empresa
     */
    @Query("SELECT c FROM Customer c WHERE " +
            "LOWER(c.firstName) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
            "LOWER(c.lastName) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
            "LOWER(c.email) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
            "LOWER(c.company) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
            "LOWER(c.phone) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
            "LOWER(c.cpfCnpj) LIKE LOWER(CONCAT('%', :term, '%'))")
    Page<Customer> searchCustomers(@Param("term") String term, Pageable pageable);

    /**
     * Clientes que fazem aniversário em uma data
     */
    @Query("SELECT c FROM Customer c WHERE " +
            "MONTH(c.birthday) = :month AND DAY(c.birthday) = :day")
    List<Customer> findByBirthday(@Param("month") int month, @Param("day") int day);

    /**
     * Clientes sem contato desde uma data
     */
    @Query("SELECT c FROM Customer c WHERE c.lastContactDate < :since OR c.lastContactDate IS NULL")
    List<Customer> findInactiveCustomers(@Param("since") LocalDate since);

    /**
     * Clientes com uma tag específica
     */
    @Query("SELECT c FROM Customer c JOIN c.tags t WHERE t = :tag")
    List<Customer> findByTagsContaining(@Param("tag") Tag tag);

    /**
     * Contagem de clientes por status
     */
    long countByStatus(CustomerStatus status);

    /**
     * Clientes com lead score acima de um valor
     */
    List<Customer> findByScoreGreaterThan(int score);

    // ========== MÉTODOS ADICIONAIS ÚTEIS ==========

    /**
     * Busca cliente por CPF/CNPJ
     */
    Optional<Customer> findByCpfCnpj(String cpfCnpj);

    /**
     * Busca cliente por telefone ou WhatsApp
     */
    @Query("SELECT c FROM Customer c WHERE c.phone = :phone OR c.whatsapp = :phone")
    Optional<Customer> findByPhoneOrWhatsapp(@Param("phone") String phone);

    /**
     * Verifica se e-mail já existe
     */
    boolean existsByEmail(String email);

    /**
     * Verifica se e-mail já existe (excluindo um cliente específico - útil para updates)
     */
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Customer c " +
            "WHERE c.email = :email AND c.id != :excludeId")
    boolean existsByEmailAndIdNot(@Param("email") String email, @Param("excludeId") UUID excludeId);

    /**
     * Verifica se CPF/CNPJ já existe
     */
    boolean existsByCpfCnpj(String cpfCnpj);

    /**
     * Verifica se CPF/CNPJ já existe (excluindo um cliente específico)
     */
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Customer c " +
            "WHERE c.cpfCnpj = :cpfCnpj AND c.id != :excludeId")
    boolean existsByCpfCnpjAndIdNot(@Param("cpfCnpj") String cpfCnpj, @Param("excludeId") UUID excludeId);

    /**
     * Clientes por vendedor e status
     */
    List<Customer> findByAssignedToAndStatus(User assignedTo, CustomerStatus status);

    /**
     * Clientes por lead source
     */
    List<Customer> findBySource(LeadSource source);

    /**
     * Clientes ordenados por score (decrescente)
     */
    List<Customer> findAllByOrderByScoreDesc();

    /**
     * Top clientes por score (limitado)
     */
    List<Customer> findTop10ByOrderByScoreDesc();

    /**
     * Clientes criados nos últimos X dias
     */
    @Query("SELECT c FROM Customer c WHERE c.createdAt >= :since")
    List<Customer> findRecentCustomers(@Param("since") LocalDateTime since);

    /**
     * Clientes com aniversário no mês atual
     */
    @Query("SELECT c FROM Customer c WHERE MONTH(c.birthday) = :month")
    List<Customer> findBirthdayThisMonth(@Param("month") int month);

    /**
     * Clientes com aniversário na semana atual
     */
    @Query("SELECT c FROM Customer c WHERE " +
            "MONTH(c.birthday) = :month AND DAY(c.birthday) BETWEEN :startDay AND :endDay")
    List<Customer> findBirthdayThisWeek(@Param("month") int month,
                                        @Param("startDay") int startDay,
                                        @Param("endDay") int endDay);

    /**
     * Clientes sem interação há mais de X dias
     */
    @Query("SELECT c FROM Customer c WHERE c.lastContactDate < :date")
    List<Customer> findCustomersWithoutInteractionSince(@Param("date") LocalDate date);

    /**
     * Clientes com pontuação alta (>= 70)
     */
    @Query("SELECT c FROM Customer c WHERE c.score >= 70 ORDER BY c.score DESC")
    List<Customer> findHotLeads();

    /**
     * Clientes com pontuação média (30-69)
     */
    @Query("SELECT c FROM Customer c WHERE c.score BETWEEN 30 AND 69 ORDER BY c.score DESC")
    List<Customer> findWarmLeads();

    /**
     * Clientes com pontuação baixa (<= 29)
     */
    @Query("SELECT c FROM Customer c WHERE c.score <= 29 ORDER BY c.score DESC")
    List<Customer> findColdLeads();

    /**
     * Atualiza o status de clientes inativos
     */
    @Modifying
    @Transactional
    @Query("UPDATE Customer c SET c.status = 'INACTIVE' " +
            "WHERE c.lastContactDate < :date AND c.status NOT IN ('INACTIVE', 'LOST')")
    int markInactiveCustomers(@Param("date") LocalDate date);

    /**
     * Recalcula scores de todos os clientes (para batch job)
     */
    @Modifying
    @Transactional
    @Query("UPDATE Customer c SET c.score = " +
            "CASE " +
            "  WHEN c.status = 'CUSTOMER' THEN 50 " +
            "  WHEN c.status = 'PROSPECT' THEN 30 " +
            "  WHEN c.status = 'LEAD' THEN 10 " +
            "  ELSE 0 " +
            "END + " +
            "CASE WHEN c.lastContactDate > :recentDate THEN 20 ELSE 0 END")
    int recalculateAllScores(@Param("recentDate") LocalDate recentDate);

    /**
     * Estatísticas por status
     */
    @Query("SELECT c.status, COUNT(c), AVG(c.score) FROM Customer c GROUP BY c.status")
    List<Object[]> getStatsByStatus();

    /**
     * Estatísticas por lead source
     */
    @Query("SELECT c.source, COUNT(c) FROM Customer c GROUP BY c.source")
    List<Object[]> getStatsBySource();

    /**
     * Média de score por vendedor
     */
    @Query("SELECT c.assignedTo, COUNT(c), AVG(c.score) FROM Customer c " +
            "WHERE c.assignedTo IS NOT NULL GROUP BY c.assignedTo")
    List<Object[]> getAverageScoreBySeller();

    /**
     * Total de clientes por vendedor
     */
    @Query("SELECT c.assignedTo, COUNT(c) FROM Customer c " +
            "WHERE c.assignedTo IS NOT NULL GROUP BY c.assignedTo")
    List<Object[]> countCustomersBySeller();

    /**
     * Clientes com oportunidades abertas
     */
    @Query("SELECT DISTINCT c FROM Customer c JOIN c.opportunities o WHERE o.status = 'OPEN'")
    List<Customer> findCustomersWithOpenOpportunities();

    /**
     * Clientes sem nenhuma oportunidade
     */
    @Query("SELECT c FROM Customer c WHERE c.opportunities IS EMPTY")
    List<Customer> findCustomersWithoutOpportunities();

    /**
     * Busca clientes com filtros dinâmicos (usando Specification)
     * Alternativa ao searchCustomers para filtros mais complexos
     */
    Page<Customer> findAllByActiveTrue(Pageable pageable);

    /**
     * Clientes por múltiplos status
     */
    List<Customer> findByStatusIn(List<CustomerStatus> statuses);

    /**
     * Conta clientes ativos
     */
    @Query("SELECT COUNT(c) FROM Customer c WHERE c.active = true")
    long countActiveCustomers();

    /**
     * Conta clientes por status e vendedor
     */
    long countByStatusAndAssignedTo(CustomerStatus status, User assignedTo);

    /**
     * Média de dias desde o último contato
     */
    @Query("SELECT AVG(CURRENT_DATE - c.lastContactDate) FROM Customer c WHERE c.lastContactDate IS NOT NULL")
    Double getAverageDaysSinceLastContact();

    /**
     * Clientes com tags específicas
     */
    @Query("SELECT c FROM Customer c JOIN c.tags t WHERE t.id IN :tagIds GROUP BY c HAVING COUNT(t) = :tagCount")
    List<Customer> findByTags(@Param("tagIds") List<UUID> tagIds, @Param("tagCount") long tagCount);
}