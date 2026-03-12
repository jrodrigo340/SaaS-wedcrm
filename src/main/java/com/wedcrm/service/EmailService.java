package com.wedcrm.service;

import java.util.Map;

public interface EmailService {

    void sendEmail(String to, String subject, String templateName, Map<String, Object> vars);

    void sendWelcomeEmail(User user);

    void sendPasswordResetEmail(String email, String token);

    void sendActivityReminderEmail(Activity activity);

    void sendMessageFromTemplate(Customer customer, MessageTemplate template);

}
