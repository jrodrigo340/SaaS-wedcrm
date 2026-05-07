package com.wedcrm.entity;

import com.wedcrm.enums.OpportunityStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "opportunities")
public class Opportunity extends AbstractEntity {

    @Column(nullable = false, length = 200)
    private String title;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "stage_id", nullable = false)
    private PipelineStage stage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to_id")
    private User assignedTo;

    @Column(precision = 19, scale = 2)
    private BigDecimal value = BigDecimal.ZERO;

    private Integer probability = 0;

    @Column(name = "expected_close_date")
    private LocalDate expectedCloseDate;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OpportunityStatus status = OpportunityStatus.OPEN;

    @Column(name = "lost_reason", length = 500)
    private String lostReason;

    @OneToMany(mappedBy = "opportunity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OpportunityProduct> products = new ArrayList<>();

    @OneToMany(mappedBy = "opportunity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Activity> activities = new ArrayList<>();

    @Column(length = 5000)
    private String notes;



    public Opportunity() {}

    public Opportunity(String title, Customer customer, PipelineStage stage, User assignedTo) {
        this.title = title;
        this.customer = customer;
        this.stage = stage;
        this.assignedTo = assignedTo;
        this.probability = stage.getProbability();
        this.status = OpportunityStatus.OPEN;
    }

    // ========== Getters E Setters ==========

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }

    public PipelineStage getStage() { return stage; }
    public void setStage(PipelineStage stage) {
        this.stage = stage;
        if (stage != null) {
            this.probability = stage.getProbability();
        }
    }

    public User getAssignedTo() { return assignedTo; }
    public void setAssignedTo(User assignedTo) { this.assignedTo = assignedTo; }

    public BigDecimal getValue() { return value; }
    public void setValue(BigDecimal value) { this.value = value; }

    public Integer getProbability() { return probability; }
    public void setProbability(Integer probability) {
        if (probability != null) {
            this.probability = Math.min(100, Math.max(0, probability));
        }
    }

    public LocalDate getExpectedCloseDate() { return expectedCloseDate; }
    public void setExpectedCloseDate(LocalDate expectedCloseDate) { this.expectedCloseDate = expectedCloseDate; }

    public LocalDateTime getClosedAt() { return closedAt; }
    public void setClosedAt(LocalDateTime closedAt) { this.closedAt = closedAt; }

    public OpportunityStatus getStatus() { return status; }
    public void setStatus(OpportunityStatus status) { this.status = status; }

    public String getLostReason() { return lostReason; }
    public void setLostReason(String lostReason) { this.lostReason = lostReason; }

    public List<OpportunityProduct> getProducts() { return products; }
    public void setProducts(List<OpportunityProduct> products) { this.products = products; }

    public List<Activity> getActivities() { return activities; }
    public void setActivities(List<Activity> activities) { this.activities = activities; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    // ========== Métodos de Negocio ==========

    /**
     * Adiciona um produto à oportunidade
     */
    public void addProduct(Product product, Integer quantity) {
        OpportunityProduct oppProduct = new OpportunityProduct(this, product, quantity);
        products.add(oppProduct);
        recalculateTotalValue();
    }

    /**
     * Remove um produto da oportunidade
     */
    public void removeProduct(OpportunityProduct product) {
        products.remove(product);
        product.setOpportunity(null);
        recalculateTotalValue();
    }

    /**
     * Recalcula o valor total baseado nos produtos
     */
    public void recalculateTotalValue() {
        this.value = products.stream()
                .map(OpportunityProduct::getTotalPrice)
                .filter(price -> price != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_EVEN);
    }

    /**
     * Adiciona uma atividade à oportunidade
     */
    public void addActivity(Activity activity) {
        activities.add(activity);
        activity.setOpportunity(this);
    }

    /**
     * Marca a oportunidade como GANHA
     */
    public void closeAsWon() {
        this.status = OpportunityStatus.WON;
        this.closedAt = LocalDateTime.now();
        this.probability = 100;

        // Promove o cliente se necessário
        if (customer != null && customer.getStatus() != null) {
            customer.promote();
        }
    }

    /**
     * Marca a oportunidade como PERDIDA
     */
    public void closeAsLost(String reason) {
        this.status = OpportunityStatus.LOST;
        this.lostReason = reason;
        this.closedAt = LocalDateTime.now();
        this.probability = 0;
    }

    /**
     * Marca a oportunidade como ABANDONADA
     */
    public void abandon() {
        this.status = OpportunityStatus.ABANDONED;
        this.closedAt = LocalDateTime.now();
    }

    /**
     * Move a oportunidade para um novo estágio
     */
    public void moveToStage(PipelineStage newStage) {
        this.stage = newStage;
        this.probability = newStage.getProbability();

        // Se for estágio de ganho ou perda, atualiza status
        if (newStage.getIsWon()) {
            closeAsWon();
        } else if (newStage.getIsLost()) {
            closeAsLost("Movido para estágio de perda");
        }
    }

    /**
     * Verifica se a oportunidade está atrasada
     */
    public boolean isOverdue() {
        if (expectedCloseDate == null) return false;
        if (status != OpportunityStatus.OPEN) return false;
        return expectedCloseDate.isBefore(LocalDate.now());
    }

    /**
     * Calcula quantos dias até o fechamento esperado
     */
    public Long getDaysUntilExpectedClose() {
        if (expectedCloseDate == null) return null;
        return ChronoUnit.DAYS.between(LocalDate.now(), expectedCloseDate);
    }

    /**
     * Verifica se a oportunidade tem alta probabilidade de fechamento
     */
    public boolean isHighProbability() {
        return probability >= 70;
    }

    /**
     * Retorna o valor formatado para exibição
     */
    public String getFormattedValue() {
        return String.format("R$ %,.2f", value);
    }

    @Override
    public String toString() {
        return "Opportunity{" +
                "id=" + getId() +
                ", title='" + title + '\'' +
                ", value=" + value +
                ", status=" + status +
                ", probability=" + probability +
                '}';
    }
}