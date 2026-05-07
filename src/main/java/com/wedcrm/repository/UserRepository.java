package com.wedcrm.repository;

import com.wedcrm.entity.User;
import com.wedcrm.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
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
public interface UserRepository extends JpaRepository<User, UUID> {

    // ========== MÉTODOS BÁSICOS DA DOCUMENTAÇÃO ==========

    /**
     * Busca usuário por e-mail para autenticação
     */
    Optional<User> findByEmail(String email);

    /**
     * Busca usuário pelo refresh token JWT
     */
    Optional<User> findByRefreshToken(String refreshToken);

    /**
     * Lista todos os usuários de um determinado papel
     */
    List<User> findAllByRole(Role role);

    /**
     * Verifica se e-mail já está cadastrado
     */
    boolean existsByEmail(String email);

    /**
     * Lista todos os usuários ativos
     */
    List<User> findAllByActiveTrue();

    // ========== MÉTODOS ADICIONAIS ÚTEIS ==========

    /**
     * Busca usuário por email ignorando maiúsculas/minúsculas
     */
    Optional<User> findByEmailIgnoreCase(String email);

    /**
     * Lista todos os usuários de um determinado papel que estão ativos
     */
    List<User> findAllByRoleAndActiveTrue(Role role);

    /**
     * Lista usuários que não são de um determinado papel
     */
    @Query("SELECT u FROM User u WHERE u.role != :role")
    List<User> findAllExceptRole(@Param("role") Role role);

    /**
     * Busca usuários por nome (contém)
     */
    List<User> findByNameContainingIgnoreCase(String name);

    /**
     * Conta usuários por papel
     */
    long countByRole(Role role);

    /**
     * Busca usuários que fizeram login após uma determinada data
     */
    List<User> findByLastLoginAfter(LocalDateTime date);

    /**
     * Busca usuários que nunca fizeram login
     */
    @Query("SELECT u FROM User u WHERE u.lastLogin IS NULL")
    List<User> findUsersWithoutLogin();

    /**
     * Busca usuários que não logaram nos últimos X dias
     */
    @Query("SELECT u FROM User u WHERE u.lastLogin < :date")
    List<User> findUsersNotLoggedSince(@Param("date") LocalDateTime date);

    /**
     * Atualiza último login do usuário
     */
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.lastLogin = :now WHERE u.id = :userId")
    void updateLastLogin(@Param("userId") UUID userId, @Param("now") LocalDateTime now);

    /**
     * Atualiza refresh token do usuário
     */
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.refreshToken = :refreshToken WHERE u.id = :userId")
    void updateRefreshToken(@Param("userId") UUID userId, @Param("refreshToken") String refreshToken);

    /**
     * Remove refresh token do usuário (logout)
     */
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.refreshToken = null WHERE u.id = :userId")
    void clearRefreshToken(@Param("userId") UUID userId);

    /**
     * Verifica se e-mail já está cadastrado (ignorando um usuário específico - útil para updates)
     */
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u " +
            "WHERE u.email = :email AND u.id != :excludeId")
    boolean existsByEmailAndIdNot(@Param("email") String email, @Param("excludeId") UUID excludeId);

    /**
     * Busca usuários por papel com paginação
     */
    @Query("SELECT u FROM User u WHERE u.role = :role AND u.active = true")
    org.springframework.data.domain.Page<User> findActiveByRole(@Param("role") Role role,
                                                                org.springframework.data.domain.Pageable pageable);

    /**
     * Busca todos os usuários com seus relacionamentos carregados (evita N+1)
     */
    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.customers LEFT JOIN FETCH u.activities")
    List<User> findAllWithRelations();

    /**
     * Busca usuário por ID com seus relacionamentos
     */
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.customers WHERE u.id = :id")
    Optional<User> findByIdWithCustomers(@Param("id") UUID id);

    /**
     * Conta usuários ativos
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.active = true")
    long countActiveUsers();

    /**
     * Estatísticas de usuários por papel
     */
    @Query("SELECT u.role, COUNT(u), SUM(CASE WHEN u.active = true THEN 1 ELSE 0 END) " +
            "FROM User u GROUP BY u.role")
    List<Object[]> getStatsByRole();

    /**
     * Busca usuários que verificaram o email
     */
    List<User> findByEmailVerifiedTrue();

    /**
     * Busca usuários que NÃO verificaram o email
     */
    List<User> findByEmailVerifiedFalse();

    /**
     * Desativa usuários inativos (soft delete em lote)
     */
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.active = false WHERE u.id IN :userIds")
    void deactivateUsers(@Param("userIds") List<UUID> userIds);

    /**
     * Busca usuários por múltiplos papéis
     */
    @Query("SELECT u FROM User u WHERE u.role IN :roles")
    List<User> findByRoles(@Param("roles") List<Role> roles);
}