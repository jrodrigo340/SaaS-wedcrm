package com.wedcrm.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.regex.Pattern;

@Embeddable
public class Address {

    @Column(length = 200)
    private String street;

    @Column(length = 20)
    private String number;

    @Column(length = 100)
    private String complement;

    @Column(length = 100)
    private String neighborhood;

    @Column(length = 100)
    private String city;

    @Column(length = 2)
    private String state;

    @Column(name = "zip_code", length = 10)
    private String zipCode;

    @Column(length = 50)
    private String country = "Brasil";

    // ========== CONSTRUTORES ==========

    public Address() {
    }

    public Address(String street, String number, String city, String state) {
        this.street = street;
        this.number = number;
        this.city = city;
        this.state = state;
        this.country = "Brasil";
    }

    public Address(String street, String number, String complement,
                   String neighborhood, String city, String state, String zipCode) {
        this.street = street;
        this.number = number;
        this.complement = complement;
        this.neighborhood = neighborhood;
        this.city = city;
        this.state = state;
        this.zipCode = zipCode;
        this.country = "Brasil";
    }

    public Address(String street, String number, String complement,
                   String neighborhood, String city, String state,
                   String zipCode, String country) {
        this.street = street;
        this.number = number;
        this.complement = complement;
        this.neighborhood = neighborhood;
        this.city = city;
        this.state = state;
        this.zipCode = zipCode;
        this.country = country;
    }

    // ========== Getters E Setters ==========

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getComplement() {
        return complement;
    }

    public void setComplement(String complement) {
        this.complement = complement;
    }

    public String getNeighborhood() {
        return neighborhood;
    }

    public void setNeighborhood(String neighborhood) {
        this.neighborhood = neighborhood;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        if (state != null) {
            this.state = state.toUpperCase();
        }
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        if (zipCode != null) {
            // Remove formatação e mantém apenas números
            String cleaned = zipCode.replaceAll("[^0-9]", "");
            if (cleaned.length() == 8) {
                this.zipCode = cleaned.substring(0, 5) + "-" + cleaned.substring(5);
            } else {
                this.zipCode = zipCode;
            }
        }
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    // ========== MÉTODOS DE NEGÓCIO ==========

    /**
     * Valida se o endereço está completo
     */
    public boolean isComplete() {
        return street != null && !street.trim().isEmpty() &&
                number != null && !number.trim().isEmpty() &&
                city != null && !city.trim().isEmpty() &&
                state != null && !state.trim().isEmpty();
    }

    /**
     * Retorna o endereço formatado em uma linha
     */
    public String getFullAddress() {
        StringBuilder sb = new StringBuilder();

        if (street != null && !street.isEmpty()) {
            sb.append(street);
        }

        if (number != null && !number.isEmpty()) {
            sb.append(", ").append(number);
        }

        if (complement != null && !complement.isEmpty()) {
            sb.append(" - ").append(complement);
        }

        return sb.toString();
    }

    /**
     * Retorna o endereço completo formatado para exibição
     */
    public String getFormattedAddress() {
        StringBuilder sb = new StringBuilder();

        // Linha 1: Rua, Número
        if (street != null && !street.isEmpty()) {
            sb.append(street);
            if (number != null && !number.isEmpty()) {
                sb.append(", ").append(number);
            }
        } else if (number != null && !number.isEmpty()) {
            sb.append("Nº ").append(number);
        }

        // Complemento
        if (complement != null && !complement.isEmpty()) {
            sb.append("\n").append(complement);
        }

        // Linha 2: Bairro
        if (neighborhood != null && !neighborhood.isEmpty()) {
            if (sb.length() > 0) sb.append("\n");
            sb.append(neighborhood);
        }

        // Linha 3: Cidade/UF
        if (city != null && !city.isEmpty() || state != null && !state.isEmpty()) {
            if (sb.length() > 0) sb.append("\n");
            if (city != null && !city.isEmpty()) {
                sb.append(city);
            }
            if (state != null && !state.isEmpty()) {
                if (city != null && !city.isEmpty()) {
                    sb.append("/");
                }
                sb.append(state);
            }
        }

        // Linha 4: CEP
        if (zipCode != null && !zipCode.isEmpty()) {
            if (sb.length() > 0) sb.append("\n");
            sb.append("CEP: ").append(zipCode);
        }

        // País
        if (country != null && !country.isEmpty() && !"Brasil".equals(country)) {
            if (sb.length() > 0) sb.append("\n");
            sb.append(country);
        }

        return sb.toString();
    }

    /**
     * Retorna o endereço em formato de linha única para busca
     */
    public String getSearchableAddress() {
        return String.format("%s %s %s %s %s %s %s %s",
                street != null ? street : "",
                number != null ? number : "",
                complement != null ? complement : "",
                neighborhood != null ? neighborhood : "",
                city != null ? city : "",
                state != null ? state : "",
                zipCode != null ? zipCode : "",
                country != null ? country : ""
        ).trim().toLowerCase();
    }

    /**
     * Valida o CEP
     */
    public boolean isValidZipCode() {
        if (zipCode == null) return false;
        Pattern pattern = Pattern.compile("^\\d{5}-\\d{3}$");
        return pattern.matcher(zipCode).matches();
    }

    /**
     * Valida a UF (sigla do estado)
     */
    public boolean isValidState() {
        if (state == null) return false;
        String[] validStates = {
                "AC", "AL", "AP", "AM", "BA", "CE", "DF", "ES", "GO", "MA",
                "MT", "MS", "MG", "PA", "PB", "PR", "PE", "PI", "RJ", "RN",
                "RS", "RO", "RR", "SC", "SP", "SE", "TO"
        };

        for (String validState : validStates) {
            if (validState.equals(state)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Verifica se o endereço está vazio
     */
    public boolean isEmpty() {
        return (street == null || street.isEmpty()) &&
                (number == null || number.isEmpty()) &&
                (complement == null || complement.isEmpty()) &&
                (neighborhood == null || neighborhood.isEmpty()) &&
                (city == null || city.isEmpty()) &&
                (state == null || state.isEmpty()) &&
                (zipCode == null || zipCode.isEmpty());
    }

    /**
     * Retorna um resumo do endereço
     */
    public String getSummary() {
        if (isEmpty()) {
            return "Endereço não informado";
        }

        if (isComplete()) {
            return String.format("%s, %s - %s/%s",
                    street, number, city, state);
        }

        return getFullAddress();
    }

    /**
     * Cria uma cópia do endereço
     */
    public Address copy() {
        return new Address(
                this.street,
                this.number,
                this.complement,
                this.neighborhood,
                this.city,
                this.state,
                this.zipCode,
                this.country
        );
    }

    @Override
    public String toString() {
        return getFormattedAddress();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Address address)) return false;

        if (street != null ? !street.equals(address.street) : address.street != null) return false;
        if (number != null ? !number.equals(address.number) : address.number != null) return false;
        if (complement != null ? !complement.equals(address.complement) : address.complement != null)
            return false;
        if (neighborhood != null ? !neighborhood.equals(address.neighborhood) : address.neighborhood != null)
            return false;
        if (city != null ? !city.equals(address.city) : address.city != null) return false;
        if (state != null ? !state.equals(address.state) : address.state != null) return false;
        if (zipCode != null ? !zipCode.equals(address.zipCode) : address.zipCode != null) return false;
        return country != null ? country.equals(address.country) : address.country == null;
    }

    @Override
    public int hashCode() {
        int result = street != null ? street.hashCode() : 0;
        result = 31 * result + (number != null ? number.hashCode() : 0);
        result = 31 * result + (complement != null ? complement.hashCode() : 0);
        result = 31 * result + (neighborhood != null ? neighborhood.hashCode() : 0);
        result = 31 * result + (city != null ? city.hashCode() : 0);
        result = 31 * result + (state != null ? state.hashCode() : 0);
        result = 31 * result + (zipCode != null ? zipCode.hashCode() : 0);
        result = 31 * result + (country != null ? country.hashCode() : 0);
        return result;
    }
}