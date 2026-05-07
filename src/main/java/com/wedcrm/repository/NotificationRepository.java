package com.wedcrm.repository;

import com.wedcrm.entity.Notification;
import com.wedcrm.entity.User;
import com.wedcrm.enums.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    // Busca por usuário
    List<Notification> findByUserOrderByCreatedAtDesc(User user);

    Page<Notification> findByUser(User user, Pageable pageable);

    // Notificações não lidas do usuário
    List<Notification> findByUserAndIsReadFalseOrderByCreatedAtDesc(User user);

    long countByUserAndIsReadFalse(User user);

    // Notificações por tipo
    List<Notification> findByUserAndType(User user, NotificationType type);

    // Notificações por referência
    List<Notification> findByReferenceIdAndReferenceType(UUID referenceId, String referenceType);

    // Marca como lida
    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = :now WHERE n.user = :user AND n.id = :id")
    void markAsRead(@Param("user") User user, @Param("id") UUID id, @Param("now") LocalDateTime now);

    // Marca todas como lidas
    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = :now WHERE n.user = :user AND n.isRead = false")
    void markAllAsRead(@Param("user") User user, @Param("now") LocalDateTime now);

    // Remove notificações antigas
    @Modifying
    @Transactional
    @Query("DELETE FROM Notification n WHERE n.createdAt < :date")
    void deleteOldNotifications(@Param("date") LocalDateTime date);

    // Remove notificações lidas de um usuário
    @Modifying
    @Transactional
    @Query("DELETE FROM Notification n WHERE n.user = :user AND n.isRead = true")
    void deleteReadNotifications(@Param("user") User user);

    // Contagem por tipo
    @Query("SELECT n.type, COUNT(n) FROM Notification n WHERE n.user = :user GROUP BY n.type")
    List<Object[]> countByTypeForUser(@Param("user") User user);

    // Notificações recentes (últimas 24h)
    @Query("SELECT n FROM Notification n WHERE n.user = :user AND n.createdAt > :since ORDER BY n.createdAt DESC")
    List<Notification> findRecentNotifications(@Param("user") User user, @Param("since") LocalDateTime since);

    // Busca notificações por intervalo de datas
    @Query("SELECT n FROM Notification n WHERE n.user = :user AND n.createdAt BETWEEN :start AND :end")
    List<Notification> findByDateRange(@Param("user") User user,
                                       @Param("start") LocalDateTime start,
                                       @Param("end") LocalDateTime end);
}