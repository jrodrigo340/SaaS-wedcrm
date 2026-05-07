package com.wedcrm.service;

import com.wedcrm.entity.Address;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class GeocodingService {

    /**
     * Valida e formata o CEP
     */
    public String formatZipCode(String zipCode) {
        if (zipCode == null) return null;

        // Remove todos os caracteres não numéricos
        String cleaned = zipCode.replaceAll("[^0-9]", "");

        if (cleaned.length() == 8) {
            return cleaned.substring(0, 5) + "-" + cleaned.substring(5);
        }

        return zipCode;
    }

    /**
     * Valida se o CEP é válido (formato)
     */
    public boolean isValidZipCodeFormat(String zipCode) {
        if (zipCode == null) return false;
        return zipCode.matches("^\\d{5}-\\d{3}$");
    }

    /**
     * Retorna a lista de estados brasileiros
     */
    public Map<String, String> getBrazilianStates() {
        Map<String, String> states = new HashMap<>();
        states.put("AC", "Acre");
        states.put("AL", "Alagoas");
        states.put("AP", "Amapá");
        states.put("AM", "Amazonas");
        states.put("BA", "Bahia");
        states.put("CE", "Ceará");
        states.put("DF", "Distrito Federal");
        states.put("ES", "Espírito Santo");
        states.put("GO", "Goiás");
        states.put("MA", "Maranhão");
        states.put("MT", "Mato Grosso");
        states.put("MS", "Mato Grosso do Sul");
        states.put("MG", "Minas Gerais");
        states.put("PA", "Pará");
        states.put("PB", "Paraíba");
        states.put("PR", "Paraná");
        states.put("PE", "Pernambuco");
        states.put("PI", "Piauí");
        states.put("RJ", "Rio de Janeiro");
        states.put("RN", "Rio Grande do Norte");
        states.put("RS", "Rio Grande do Sul");
        states.put("RO", "Rondônia");
        states.put("RR", "Roraima");
        states.put("SC", "Santa Catarina");
        states.put("SP", "São Paulo");
        states.put("SE", "Sergipe");
        states.put("TO", "Tocantins");
        return states;
    }

    /**
     * Obtém o nome completo do estado a partir da sigla
     */
    public String getStateName(String stateCode) {
        if (stateCode == null) return null;
        return getBrazilianStates().get(stateCode.toUpperCase());
    }
}
