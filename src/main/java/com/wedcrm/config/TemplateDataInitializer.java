package com.wedcrm.config;

import com.wedcrm.entity.MessageTemplate;
import com.wedcrm.enums.MessageChannel;
import com.wedcrm.enums.TemplateCategory;
import com.wedcrm.repository.MessageTemplateRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("dev")
public class TemplateDataInitializer {

    @Bean
    public CommandLineRunner initTemplates(MessageTemplateRepository templateRepository) {
        return args -> {
            if (templateRepository.count() == 0) {

                // Template de boas-vindas
                MessageTemplate welcome = new MessageTemplate(
                        "Boas-vindas Novo Cliente",
                        MessageChannel.EMAIL,
                        TemplateCategory.WELCOME,
                        """
                        Olá {{nome}}!
                        
                        Seja bem-vindo(a) à nossa empresa! É um prazer ter você conosco.
                        
                        Seu consultor responsável será {{vendedor}}, que está à disposição para ajudá-lo no que precisar.
                        
                        Em breve você receberá mais informações sobre nossos produtos e serviços.
                        
                        Atenciosamente,
                        Equipe WedCRM
                        """
                );
                welcome.setSubject("Bem-vindo ao WedCRM!");
                welcome.setDescription("Template de boas-vindas para novos clientes");

                // Template de aniversário
                MessageTemplate birthday = new MessageTemplate(
                        "Feliz Aniversário",
                        MessageChannel.WHATSAPP,
                        TemplateCategory.BIRTHDAY,
                        """
                        🎉 Feliz aniversário, {{nome}}!
                        
                        Toda a equipe WedCRM deseja um dia especial, cheio de alegrias e realizações!
                        
                        Conte conosco para mais um ano de parceria e sucesso!
                        
                        🎂🎈
                        """
                );
                birthday.setDescription("Mensagem de aniversário para clientes");

                // Template de follow-up
                MessageTemplate followup = new MessageTemplate(
                        "Follow-up Proposta",
                        MessageChannel.EMAIL,
                        TemplateCategory.FOLLOWUP,
                        """
                        Olá {{nome}}, tudo bem?
                        
                        Há alguns dias enviamos uma proposta para {{empresa}} e gostaríamos de saber se você teve oportunidade de analisá-la.
                        
                        Estamos à disposição para esclarecer qualquer dúvida ou ajustar o que for necessário.
                        
                        Aguardamos seu retorno!
                        
                        Atenciosamente,
                        {{vendedor}}
                        """
                );
                followup.setSubject("Sobre a proposta enviada");
                followup.setDescription("Follow-up após envio de proposta");

                // Template de lembrete
                MessageTemplate reminder = new MessageTemplate(
                        "Lembrete de Reunião",
                        MessageChannel.SMS,
                        TemplateCategory.REMINDER,
                        """
                        Olá {{nome}}, lembramos da nossa reunião amanhã às 14h.
                        
                        Link: {{linkReuniao}}
                        
                        Confirme sua presença.
                        """
                );
                reminder.setDescription("Lembrete de reunião por SMS");

                // Template de reengajamento
                MessageTemplate winback = new MessageTemplate(
                        "Sentimos sua falta",
                        MessageChannel.EMAIL,
                        TemplateCategory.WIN_BACK,
                        """
                        Olá {{nome}}!
                        
                        Fazem {{diasSemContato}} dias que não falamos com você e estamos com saudades!
                        
                        Temos novidades que podem interessar à {{empresa}}. Que tal marcarmos uma conversa?
                        
                        Clique aqui para agendar: {{linkReuniao}}
                        
                        Abraços,
                        {{vendedor}}
                        """
                );
                winback.setSubject("Sentimos sua falta! 🙋‍♂️");
                winback.setDescription("Reengajamento de clientes inativos");

                // Salva todos
                templateRepository.save(welcome);
                templateRepository.save(birthday);
                templateRepository.save(followup);
                templateRepository.save(reminder);
                templateRepository.save(winback);

                System.out.println("✅ Templates de mensagem criados com sucesso!");
            }
        };
    }
}