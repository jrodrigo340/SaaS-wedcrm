package com.wedcrm.controller;

import com.wedcrm.dto.request.NotificationRequestDTO;
import com.wedcrm.dto.response.NotificationResponseDTO;
import com.wedcrm.dto.dashboard.NotificationSummaryDTO;
import com.wedcrm.entity.Notification;
import com.wedcrm.mapper.NotificationMapper;
import com.wedcrm.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationMapper notificationMapper;

    /**
     * Lista todas as notificações do usuário autenticado
     */
    @GetMapping
    public ResponseEntity<List<NotificationResponseDTO>> getMyNotifications(
            @AuthenticationPrincipal UserDetails userDetails) {

        // Obtém o ID do usuário do contexto de segurança
        UUID userId = getUserIdFromUserDetails(userDetails);

        List<Notification> notifications = notificationService.getUserNotifications(userId);
        List<NotificationResponseDTO> dtos = notifications.stream()
                .map(notificationMapper::toResponseDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    /**
     * Obtém o resumo das notificações (quantidade não lida)
     */
    @GetMapping("/summary")
    public ResponseEntity<NotificationSummaryDTO> getNotificationSummary(
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID userId = getUserIdFromUserDetails(userDetails);

        long unreadCount = notificationService.countUnreadNotifications(userId);
        List<Notification> allNotifications = notificationService.getUserNotifications(userId);

        NotificationSummaryDTO summary = new NotificationSummaryDTO(
                unreadCount,
                (long) allNotifications.size(),
                unreadCount > 0
        );

        return ResponseEntity.ok(summary);
    }

    /**
     * Lista apenas notificações não lidas
     */
    @GetMapping("/unread")
    public ResponseEntity<List<NotificationResponseDTO>> getUnreadNotifications(
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID userId = getUserIdFromUserDetails(userDetails);

        List<Notification> notifications = notificationService.getUnreadNotifications(userId);
        List<NotificationResponseDTO> dtos = notifications.stream()
                .map(notificationMapper::toResponseDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    /**
     * Conta notificações não lidas
     */
    @GetMapping("/unread/count")
    public ResponseEntity<Long> countUnreadNotifications(
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID userId = getUserIdFromUserDetails(userDetails);
        long count = notificationService.countUnreadNotifications(userId);

        return ResponseEntity.ok(count);
    }

    /**
     * Marca uma notificação como lida
     */
    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID userId = getUserIdFromUserDetails(userDetails);
        notificationService.markAsRead(userId, id);

        return ResponseEntity.ok().build();
    }

    /**
     * Marca todas as notificações como lidas
     */
    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID userId = getUserIdFromUserDetails(userDetails);
        notificationService.markAllAsRead(userId);

        return ResponseEntity.ok().build();
    }

    /**
     * Remove notificações lidas
     */
    @DeleteMapping("/read")
    public ResponseEntity<Void> deleteReadNotifications(
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID userId = getUserIdFromUserDetails(userDetails);
        notificationService.deleteReadNotifications(userId);

        return ResponseEntity.noContent().build();
    }

    /**
     * Cria uma nova notificação (apenas ADMIN)
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<NotificationResponseDTO> createNotification(
            @Valid @RequestBody NotificationRequestDTO request) {

        Notification notification = notificationMapper.toEntity(request);
        Notification created = notificationService.createNotification(notification);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(notificationMapper.toResponseDTO(created));
    }

    /**
     * Método auxiliar para extrair o ID do usuário do UserDetails
     * Nota: Isso assume que o username é o ID do usuário ou que você tem um UserDetails customizado
     */
    private UUID getUserIdFromUserDetails(UserDetails userDetails) {
        // Aqui você deve implementar a lógica para extrair o ID do usuário
        // Pode ser do JWT, do banco de dados, ou do contexto de segurança
        // Por enquanto, vamos assumir que o username é o ID em formato string
        return UUID.fromString(userDetails.getUsername());
    }
}