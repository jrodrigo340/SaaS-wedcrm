package com.wedcrm.service;

import com.wedcrm.entity.Activity;
import com.wedcrm.entity.Customer;
import com.wedcrm.entity.MessageTemplate;
import com.wedcrm.entity.User;
import org.thymeleaf.context.Context;

import java.util.Map;

public interface EmailService {

    void sendEmail(String to, String subject, String templateName, Map<String, Object> variables);

    void sendWelcomeEmail(User user, String verificationToken);

    void sendPasswordResetEmail(User user, String resetToken);

    void sendActivityReminderEmail(Activity activity);

    void sendMessageFromTemplate(Customer customer, MessageTemplate template);

    void sendTestEmail(String to);
}