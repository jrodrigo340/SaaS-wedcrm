package com.wedcrm.security.validation;

import com.wedcrm.dto.AddressDTO;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class AddressValidator implements ConstraintValidator<ValidAddress, AddressDTO> {

    @Override
    public boolean isValid(AddressDTO address, ConstraintValidatorContext context) {
        if (address == null) {
            return true; // endereço opcional
        }

        boolean isValid = true;

        // Regra: se rua preenchida, número é obrigatório
        if (address.street() != null && !address.street().isEmpty()) {
            if (address.number() == null || address.number().isEmpty()) {
                context.buildConstraintViolationWithTemplate("Número é obrigatório quando a rua é informada")
                        .addPropertyNode("number")
                        .addConstraintViolation();
                isValid = false;
            }
        }

        // Validação do CEP (formato 00000-000)
        if (address.zipCode() != null && !address.zipCode().isEmpty()) {
            if (!address.zipCode().matches("^\\d{5}-\\d{3}$")) {
                context.buildConstraintViolationWithTemplate("CEP inválido. Use o formato 00000-000")
                        .addPropertyNode("zipCode")
                        .addConstraintViolation();
                isValid = false;
            }
        }

        // Validação da UF (estado)
        if (address.state() != null && !address.state().isEmpty()) {
            String[] validStates = {
                    "AC", "AL", "AP", "AM", "BA", "CE", "DF", "ES", "GO", "MA",
                    "MT", "MS", "MG", "PA", "PB", "PR", "PE", "PI", "RJ", "RN",
                    "RS", "RO", "RR", "SC", "SP", "SE", "TO"
            };
            boolean validState = false;
            for (String uf : validStates) {
                if (uf.equals(address.state())) {
                    validState = true;
                    break;
                }
            }
            if (!validState) {
                context.buildConstraintViolationWithTemplate("Estado inválido. Use a sigla de 2 letras")
                        .addPropertyNode("state")
                        .addConstraintViolation();
                isValid = false;
            }
        }

        return isValid;
    }
}
