package com.wedcrm.service;

import com.wedcrm.entity.Customer;
import com.wedcrm.entity.MessageTemplate;
import com.wedcrm.entity.User;
import com.wedcrm.repository.MessageTemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TemplateProcessorService {

    @Autowired
    private MessageTemplateRepository templateRepository;

    /**
     * Processa um template substituindo as variáveis pelos dados do cliente
     */
    public String processTemplate(MessageTemplate template, Customer customer, User seller) {
        String processedBody = template.getBody();
        String processedSubject = template.getSubject();

        // Mapa de variáveis disponíveis
        Map<String, String> variables = new HashMap<>();

        // Variáveis do cliente
        variables.put("nome", customer.getFirstName());
        variables.put("nomeCompleto", customer.getFullName());
        variables.put("empresa", customer.getCompany() != null ? customer.getCompany() : "");
        variables.put("email", customer.getEmail());
        variables.put("telefone", customer.getPhone() != null ? customer.getPhone() : "");

        // Variáveis do vendedor
        if (seller != null) {
            variables.put("vendedor", seller.getName());
            variables.put("emailVendedor", seller.getEmail());
            variables.put("telefoneVendedor", seller.getPhone() != null ? seller.getPhone() : "");
        }

        // Variáveis de data
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        variables.put("dataAtual", LocalDate.now().format(dateFormatter));

        // Variáveis de negócio
        if (customer.getLastContactDate() != null) {
            long daysSinceContact = java.time.Period.between(
                    customer.getLastContactDate(), LocalDate.now()).getDays();
            variables.put("diasSemContato", String.valueOf(daysSinceContact));
        }

        // Processa o corpo
        processedBody = replaceVariables(processedBody, variables);

        // Processa o assunto (se existir)
        if (processedSubject != null) {
            processedSubject = replaceVariables(processedSubject, variables);
        }

        // Incrementa o contador de uso
        template.incrementUsage();
        templateRepository.save(template);

        return processedBody;
    }

    /**
     * Substitui variáveis no formato {{variavel}} pelo valor correspondente
     */
    private String replaceVariables(String text, Map<String, String> variables) {
        if (text == null) return null;

        Pattern pattern = Pattern.compile("\\{\\{(.*?)\\}\\}");
        Matcher matcher = pattern.matcher(text);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String variable = matcher.group(1).trim();
            String value = variables.getOrDefault(variable, "{{" + variable + "}}");
            matcher.appendReplacement(result, Matcher.quoteReplacement(value));
        }
        matcher.appendTail(result);

        return result.toString();
    }

    /**
     * Retorna todas as variáveis disponíveis para documentação
     */
    public Map<String, String> getAvailableVariables() {
        Map<String, String> variables = new HashMap<>();
        variables.put("{{nome}}", "Primeiro nome do cliente");
        variables.put("{{nomeCompleto}}", "Nome completo do cliente");
        variables.put("{{empresa}}", "Empresa do cliente");
        variables.put("{{vendedor}}", "Nome do vendedor responsável");
        variables.put("{{emailVendedor}}", "E-mail do vendedor");
        variables.put("{{telefoneVendedor}}", "Telefone do vendedor");
        variables.put("{{dataAtual}}", "Data atual formatada");
        variables.put("{{valorOportunidade}}", "Valor da oportunidade mais recente");
        variables.put("{{estagio}}", "Estágio atual da oportunidade");
        variables.put("{{diasSemContato}}", "Dias sem contato");
        variables.put("{{linkProposta}}", "Link da proposta");
        variables.put("{{linkReuniao}}", "Link da reunião");
        return variables;
    }
}