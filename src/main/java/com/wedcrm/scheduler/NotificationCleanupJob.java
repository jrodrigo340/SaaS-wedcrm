package com.wedcrm.scheduler;

import com.wedcrm.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class NotificationCleanupJob {

    @Autowired
    private NotificationService notificationService;

    /**
     * Remove notificações com mais de 90 dias
     * Executa todos os domingos às 3:00 AM
     */
    @Scheduled(cron = "0 0 3 * * SUN")
    public void cleanOldNotifications() {
        System.out.println("Iniciando limpeza de notificações antigas...");
        notificationService.cleanOldNotifications();
        System.out.println("Limpeza de notificações concluída!");
    }
}