package com.wedcrm.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Entity
@Table(name = "opportunity_products")
public class OpportunityProduct extends AbstractEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "opportunity_id", nullable = false)
    private Opportunity opportunity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity = 1;

    @Column(name = "unit_price", nullable = false, precision = 19, scale = 2)
    private BigDecimal unitPrice;

    @Column(precision = 5, scale = 2)
    private BigDecimal discount = BigDecimal.ZERO;

    @Column(name = "total_price", precision = 19, scale = 2)
    private BigDecimal totalPrice;

    @Column(length = 500)
    private String notes;

    public OpportunityProduct() {

    }

    public OpportunityProduct(Opportunity opportunity, Product product, Integer quantity) {
        this.opportunity = opportunity;
        this.product = product;
        this.quantity = quantity;
        this.unitPrice = product.getPrice();
        this.discount = BigDecimal.ZERO;
        calculateTotalPrice();
    }

    public OpportunityProduct(Opportunity opportunity, Product product,
                              Integer quantity, BigDecimal unitPrice) {
        this.opportunity = opportunity;
        this.product = product;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.discount = BigDecimal.ZERO;
        calculateTotalPrice();
    }

    // ========== Getters E Setters ==========

    public Opportunity getOpportunity() {
        return opportunity;
    }

    public void setOpportunity(Opportunity opportunity) {
        this.opportunity = opportunity;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
        if (this.unitPrice == null) {
            this.unitPrice = product.getPrice();
        }
        calculateTotalPrice();
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        if (quantity == null || quantity < 1) {
            throw new IllegalArgumentException("Quantidade deve ser maior que zero");
        }
        this.quantity = quantity;
        calculateTotalPrice();
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Preço unitário deve ser maior ou igual a zero");
        }
        this.unitPrice = unitPrice;
        calculateTotalPrice();
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public void setDiscount(BigDecimal discount) {
        if (discount == null) {
            this.discount = BigDecimal.ZERO;
        } else if (discount.compareTo(BigDecimal.ZERO) < 0 || discount.compareTo(new BigDecimal("100")) > 0) {
            throw new IllegalArgumentException("Desconto deve estar entre 0% e 100%");
        } else {
            this.discount = discount;
        }
        calculateTotalPrice();
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    // ========== Método de Negócios ==========

    /**
     * Calcula o preço total baseado na quantidade, preço unitário e desconto
     */
    private void calculateTotalPrice() {
        if (quantity != null && unitPrice != null) {
            // Subtotal = quantidade * preço unitário
            BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));

            // Aplica desconto se houver
            if (discount != null && discount.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal discountValue = subtotal.multiply(
                        discount.divide(new BigDecimal("100"), 4, RoundingMode.HALF_EVEN)
                );
                this.totalPrice = subtotal.subtract(discountValue)
                        .setScale(2, RoundingMode.HALF_EVEN);
            } else {
                this.totalPrice = subtotal.setScale(2, RoundingMode.HALF_EVEN);
            }
        }
    }

    /**
     * Recalcula o preço total (útil após alterações)
     */
    public void recalculate() {
        calculateTotalPrice();
    }

    /**
     * Retorna o valor do desconto em reais
     */
    public BigDecimal getDiscountAmount() {
        if (quantity == null || unitPrice == null || discount == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
        return subtotal.multiply(discount.divide(new BigDecimal("100"), 4, RoundingMode.HALF_EVEN))
                .setScale(2, RoundingMode.HALF_EVEN);
    }

    /**
     * Retorna o subtotal sem desconto
     */
    public BigDecimal getSubtotal() {
        if (quantity == null || unitPrice == null) {
            return BigDecimal.ZERO;
        }
        return unitPrice.multiply(BigDecimal.valueOf(quantity))
                .setScale(2, RoundingMode.HALF_EVEN);
    }

    /**
     * Retorna o preço unitário com desconto aplicado
     */
    public BigDecimal getUnitPriceWithDiscount() {
        if (unitPrice == null || discount == null) {
            return unitPrice;
        }

        BigDecimal discountPerUnit = unitPrice.multiply(
                discount.divide(new BigDecimal("100"), 4, RoundingMode.HALF_EVEN)
        );

        return unitPrice.subtract(discountPerUnit)
                .setScale(2, RoundingMode.HALF_EVEN);
    }

    /**
     * Aplica um desconto percentual
     */
    public void applyDiscount(BigDecimal discountPercent) {
        setDiscount(discountPercent);
    }

    /**
     * Aplica um desconto em valor fixo por unidade
     */
    public void applyFixedDiscount(BigDecimal discountPerUnit) {
        if (unitPrice == null || discountPerUnit == null) {
            return;
        }

        BigDecimal newUnitPrice = unitPrice.subtract(discountPerUnit);
        if (newUnitPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Desconto não pode ser maior que o preço");
        }

        // Converte o desconto fixo em percentual
        BigDecimal percentual = discountPerUnit.multiply(new BigDecimal("100"))
                .divide(unitPrice, 2, RoundingMode.HALF_EVEN);
        setDiscount(percentual);
    }

    /**
     * Verifica se tem desconto
     */
    public boolean hasDiscount() {
        return discount != null && discount.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Retorna o nome do produto para exibição
     */
    public String getProductName() {
        return product != null ? product.getName() : "Produto não encontrado";
    }

    /**
     * Retorna o SKU do produto
     */
    public String getProductSku() {
        return product != null ? product.getSku() : "";
    }

    /**
     * Formata o preço total para exibição
     */
    public String getFormattedTotalPrice() {
        if (totalPrice == null) return "R$ 0,00";
        return String.format("R$ %,.2f", totalPrice);
    }

    /**
     * Formata o preço unitário para exibição
     */
    public String getFormattedUnitPrice() {
        if (unitPrice == null) return "R$ 0,00";
        return String.format("R$ %,.2f", unitPrice);
    }

    /**
     * Formata o desconto para exibição
     */
    public String getFormattedDiscount() {
        if (discount == null || discount.compareTo(BigDecimal.ZERO) == 0) {
            return "Sem desconto";
        }
        return String.format("%.2f%%", discount);
    }

    @Override
    public String toString() {
        return "OpportunityProduct{" +
                "id=" + getId() +
                ", product=" + (product != null ? product.getName() : null) +
                ", quantity=" + quantity +
                ", unitPrice=" + unitPrice +
                ", discount=" + discount +
                "%, totalPrice=" + totalPrice +
                '}';
    }
}