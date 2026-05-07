package com.wedcrm.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Entity
@Table(name = "products")
public class Product extends AbstractEntity {

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 2000)
    private String description;

    @Column(unique = true, length = 50)
    private String sku;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal price;

    @Column(length = 30)
    private String unit;

    @Column(length = 100)
    private String category;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    public Product() {

    }

    public Product(String name, String sku, BigDecimal price) {
        this.name = name;
        this.sku = sku;
        this.price = price;
        this.isActive = true;
    }

    public Product(String name, String sku, BigDecimal price, String category) {
        this.name = name;
        this.sku = sku;
        this.price = price;
        this.category = category;
        this.isActive = true;
    }

    // ========== Getters E Setters ==========

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    // ========== Métodos de Negocio ==========

    /**
     * Ativa o produto
     */
    public void activate() {
        this.isActive = true;
    }

    /**
     * Desativa o produto
     */
    public void deactivate() {
        this.isActive = false;
    }

    /**
     * Verifica se o produto está ativo
     */
    public boolean isActive() {
        return Boolean.TRUE.equals(isActive);
    }

    /**
     * Formata o preço para exibição
     */
    public String getFormattedPrice() {
        if (price == null) return "R$ 0,00";
        return String.format("R$ %,.2f", price);
    }

    /**
     * Retorna o preço com a unidade
     */
    public String getPriceWithUnit() {
        String formattedPrice = getFormattedPrice();
        if (unit != null && !unit.isEmpty()) {
            return formattedPrice + " / " + unit;
        }
        return formattedPrice;
    }

    /**
     * Aplica um desconto percentual ao preço
     */
    public BigDecimal applyDiscount(double percentDiscount) {
        if (price == null) return BigDecimal.ZERO;

        BigDecimal discount = price.multiply(BigDecimal.valueOf(percentDiscount / 100));
        return price.subtract(discount).setScale(2, RoundingMode.HALF_EVEN);
    }

    /**
     * Calcula o preço para uma quantidade específica
     */
    public BigDecimal calculatePriceForQuantity(int quantity) {
        if (price == null) return BigDecimal.ZERO;
        return price.multiply(BigDecimal.valueOf(quantity))
                .setScale(2, RoundingMode.HALF_EVEN);
    }

    /**
     * Retorna o ícone baseado na categoria (para exibição)
     */
    public String getCategoryIcon() {
        if (category == null) return "📦";

        return switch (category.toLowerCase()) {
            case "software", "sistema", "aplicativo" -> "💻";
            case "consultoria", "serviço", "treinamento" -> "👨‍🏫";
            case "hardware", "equipamento" -> "🖥️";
            case "assinatura", "plano" -> "📅";
            case "suporte", "manutenção" -> "🔧";
            default -> "📦";
        };
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + getId() +
                ", name='" + name + '\'' +
                ", sku='" + sku + '\'' +
                ", price=" + price +
                ", category='" + category + '\'' +
                ", isActive=" + isActive +
                '}';
    }
}