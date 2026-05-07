package com.wedcrm.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tags")
public class Tag extends AbstractEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(length = 7)
    private String color;

    @Column(length = 500)
    private String description;

    @ManyToMany(mappedBy = "tags")
    private List<Customer> customers = new ArrayList<>();

    public Tag() {

    }

    public Tag(String name) {
        this.name = name;
        this.color = "#6c757d"; // Cor padrão cinza
    }

    public Tag(String name, String color) {
        this.name = name;
        this.color = color;
    }

    public Tag(String name, String color, String description) {
        this.name = name;
        this.color = color;
        this.description = description;
    }

    // ========== Getters E Setters ==========

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Customer> getCustomers() {
        return customers;
    }

    public void setCustomers(List<Customer> customers) {
        this.customers = customers;
    }

    // ========== Métodos De Negócio ==========

    /**
     * Adiciona um cliente à tag
     */
    public void addCustomer(Customer customer) {
        if (!customers.contains(customer)) {
            customers.add(customer);
            customer.getTags().add(this);
        }
    }

    /**
     * Remove um cliente da tag
     */
    public void removeCustomer(Customer customer) {
        customers.remove(customer);
        customer.getTags().remove(this);
    }

    /**
     * Verifica se a tag tem uma cor válida
     */
    public boolean isValidColor() {
        if (color == null) return false;
        return color.matches("^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$");
    }

    /**
     * Retorna a cor da tag (com fallback)
     */
    public String getValidColor() {
        if (isValidColor()) {
            return color;
        }
        return "#6c757d"; // Cinza padrão
    }

    /**
     * Retorna a cor da tag em formato RGB
     */
    public String getRgbColor() {
        String hex = getValidColor();
        int r = Integer.parseInt(hex.substring(1, 3), 16);
        int g = Integer.parseInt(hex.substring(3, 5), 16);
        int b = Integer.parseInt(hex.substring(5, 7), 16);
        return String.format("rgb(%d, %d, %d)", r, g, b);
    }

    /**
     * Retorna a cor da tag para uso em CSS (com opacidade)
     */
    public String getColorWithOpacity(double opacity) {
        String hex = getValidColor();
        int r = Integer.parseInt(hex.substring(1, 3), 16);
        int g = Integer.parseInt(hex.substring(3, 5), 16);
        int b = Integer.parseInt(hex.substring(5, 7), 16);
        return String.format("rgba(%d, %d, %d, %.2f)", r, g, b, opacity);
    }

    /**
     * Conta quantos clientes têm esta tag
     */
    public int getCustomerCount() {
        return customers.size();
    }

    /**
     * Verifica se a tag está sendo usada por algum cliente
     */
    public boolean isUsed() {
        return !customers.isEmpty();
    }

    @Override
    public String toString() {
        return "Tag{" +
                "id=" + getId() +
                ", name='" + name + '\'' +
                ", color='" + color + '\'' +
                ", customerCount=" + customers.size() +
                '}';
    }
}