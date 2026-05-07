package com.wedcrm.service;

import com.wedcrm.entity.Activity;
import com.wedcrm.entity.User;
import com.wedcrm.enums.ActivityStatus;
import com.wedcrm.repository.ActivityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Service
public class ActivityService {

    @Autowired
    private ActivityRepository activityRepository;

    @Transactional
    public Activity createActivity(Activity activity) {
        // Validações
        if (activity.getDueDate().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Data de vencimento não pode ser no passado");
        }

        // Se tem lembrete, verifica se é antes do vencimento
        if (activity.getReminderAt() != null &&
                activity.getReminderAt().isAfter(activity.getDueDate())) {
            throw new IllegalArgumentException("Lembrete deve ser antes da data de vencimento");
        }

        return activityRepository.save(activity);
    }

    @Transactional
    public Activity completeActivity(UUID activityId) {
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new RuntimeException("Atividade não encontrada"));

        activity.complete();

        // Se tiver cliente associado, atualiza último contato
        if (activity.getCustomer() != null) {
            activity.getCustomer().setLastContactDate(LocalDateTime.now().toLocalDate());
        }

        return activityRepository.save(activity);
    }

    public List<Activity> getUserDashboard(User user) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfWeek = now.with(LocalTime.MIN).minusDays(now.getDayOfWeek().getValue() - 1);
        LocalDateTime endOfWeek = startOfWeek.plusDays(7).with(LocalTime.MAX);

        return activityRepository.findWeekActivities(user, startOfWeek, endOfWeek);
    }

    @Transactional
    public void processReminders() {
        LocalDateTime now = LocalDateTime.now();
        List<Activity> pendingReminders = activityRepository.findPendingReminders(now);

        for (Activity activity : pendingReminders) {
            // Envia notificação (email, push, etc)
            sendReminderNotification(activity);
            activity.markReminderAsSent();
            activityRepository.save(activity);
        }
    }

    private void sendReminderNotification(Activity activity) {
        // Implementar envio de notificação
        System.out.println("🔔 Lembrete: " + activity.getTitle() +
                " para " + activity.getAssignedTo().getName());
    }
}