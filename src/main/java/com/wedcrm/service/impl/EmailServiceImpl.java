package com.wedcrm.service.impl;

import com.wedcrm.entity.Activity;
import com.wedcrm.entity.Customer;
import com.wedcrm.entity.MessageTemplate;
import com.wedcrm.entity.User;
import com.wedcrm.service.EmailService;
import com.wedcrm.service.MessageGeneratorService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private MessageGeneratorService messageGeneratorService;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${wedcrm.frontend-url}")
    private String frontendUrl;

    @Value("${wedcrm.company-name:WedCRM}")
    private String companyName;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // ========== MÉTODO PRINCIPAL ==========

    @Override
    public void sendEmail(String to, String subject, String templateName, Map<String, Object> variables) {
        try {
            Context context = new Context();
            context.setVariables(variables);

            String htmlContent = templateEngine.process(templateName, context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);

        } catch (MessagingException e) {
            throw new RuntimeException("Erro ao enviar e-mail para " + to + ": " + e.getMessage(), e);
        }
    }

    // ========== E-MAIL DE BOAS-VINDAS ==========

    @Override
    public void sendWelcomeEmail(User user, String verificationToken) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("userName", user.getName());
        variables.put("userEmail", user.getEmail());
        variables.put("verificationLink", frontendUrl + "/verify-email?token=" + verificationToken);
        variables.put("loginLink", frontendUrl + "/login");
        variables.put("companyName", companyName);
        variables.put("currentYear", LocalDateTime.now().getYear());

        sendEmail(
                user.getEmail(),
                "Bem-vindo ao " + companyName + "!",
                "welcome",
                variables
        );
    }

    // ========== E-MAIL DE RECUPERAÇÃO DE SENHA ==========

    @Override
    public void sendPasswordResetEmail(User user, String resetToken) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("userName", user.getName());
        variables.put("resetLink", frontendUrl + "/reset-password?token=" + resetToken);
        variables.put("expirationHours", 1);
        variables.put("companyName", companyName);
        variables.put("currentYear", LocalDateTime.now().getYear());

        sendEmail(
                user.getEmail(),
                "Recuperação de Senha - " + companyName,
                "password-reset",
                variables
        );
    }

    // ========== E-MAIL DE LEMBRETE DE ATIVIDADE ==========

    @Override
    public void sendActivityReminderEmail(Activity activity) {
        User user = activity.getAssignedTo();
        Customer customer = activity.getCustomer();

        Map<String, Object> variables = new HashMap<>();
        variables.put("userName", user.getName());
        variables.put("activityTitle", activity.getTitle());
        variables.put("activityDescription", activity.getDescription() != null ? activity.getDescription() : "");
        variables.put("activityType", activity.getType().getDescription());
        variables.put("activityPriority", activity.getPriority().name());
        variables.put("activityPriorityClass", getPriorityClass(activity.getPriority()));
        variables.put("dueDate", activity.getDueDate().format(dateFormatter));

        if (customer != null) {
            variables.put("customerName", customer.getFullName());
            variables.put("customerEmail", customer.getEmail());
            variables.put("customerPhone", customer.getPhone() != null ? customer.getPhone() : "");
            variables.put("customerLink", frontendUrl + "/customers/" + customer.getId());
        } else {
            variables.put("customerName", "Não associado");
            variables.put("customerEmail", "");
            variables.put("customerPhone", "");
            variables.put("customerLink", "#");
        }

        variables.put("activityLink", frontendUrl + "/activities/" + activity.getId());
        variables.put("companyName", companyName);
        variables.put("currentYear", LocalDateTime.now().getYear());

        sendEmail(
                user.getEmail(),
                "🔔 Lembrete: " + activity.getTitle(),
                "activity-reminder",
                variables
        );
    }

    // ========== E-MAIL A PARTIR DE TEMPLATE DO CRM ==========

    @Override
    public void sendMessageFromTemplate(Customer customer, MessageTemplate template) {
        if (customer.getEmail() == null || customer.getEmail().isEmpty()) {
            throw new RuntimeException("Cliente não possui e-mail cadastrado");
        }

        // Gera o conteúdo personalizado usando o MessageGeneratorService
        String processedBody = messageGeneratorService.resolveVariables(template.getBody(), customer);
        String processedSubject = template.getSubject() != null ?
                messageGeneratorService.resolveVariables(template.getSubject(), customer) :
                template.getName();

        Map<String, Object> variables = new HashMap<>();
        variables.put("customerName", customer.getFullName());
        variables.put("content", processedBody);
        variables.put("companyName", companyName);
        variables.put("currentYear", LocalDateTime.now().getYear());
        variables.put("unsubscribeLink", frontendUrl + "/unsubscribe/" + customer.getId());

        sendEmail(
                customer.getEmail(),
                processedSubject,
                "crm-template",
                variables
        );
    }

    // ========== E-MAIL DE TESTE ==========

    @Override
    public void sendTestEmail(String to) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("recipientName", to);
        variables.put("companyName", companyName);
        variables.put("currentYear", LocalDateTime.now().getYear());
        variables.put("testTime", LocalDateTime.now().format(dateFormatter));

        sendEmail(
                to,
                "Teste de E-mail - " + companyName,
                "test-email",
                variables
        );
    }

    // ========== MÉTODOS PRIVADOS ==========

    private String getPriorityClass(com.wedcrm.enums.Priority priority) {
        return switch (priority) {
            case LOW -> "priority-low";
            case MEDIUM -> "priority-medium";
            case HIGH -> "priority-high";
            case URGENT -> "priority-urgent";
        };
    }
}