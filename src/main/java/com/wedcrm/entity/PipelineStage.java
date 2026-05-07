package com.wedcrm.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pipeline_stages")
public class PipelineStage extends AbstractEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(name = "stage_order", nullable = false)
    private Integer order;

    @Column(length = 7)
    private String color;

    @Column(nullable = false)
    private Integer probability = 0;

    @Column(name = "is_won", nullable = false)
    private Boolean isWon = false;

    @Column(name = "is_lost", nullable = false)
    private Boolean isLost = false;

    @OneToMany(mappedBy = "stage", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Opportunity> opportunities = new ArrayList<>();

    public PipelineStage() {
    }

    public PipelineStage(String name, Integer order, Integer probability) {
        this.name = name;
        this.order = order;
        this.probability = probability;
        this.isWon = false;
        this.isLost = false;
    }

    public PipelineStage(String name, Integer order, Integer probability, String color) {
        this.name = name;
        this.order = order;
        this.probability = probability;
        this.color = color;
        this.isWon = false;
        this.isLost = false;
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

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Integer getProbability() {
        return probability;
    }

    public void setProbability(Integer probability) {
        if (probability != null) {
            this.probability = Math.min(100, Math.max(0, probability));
        }
    }

    public Boolean getIsWon() {
        return isWon;
    }

    public void setIsWon(Boolean isWon) {
        this.isWon = isWon;
        // Se for estágio de ganho, não pode ser de perda
        if (Boolean.TRUE.equals(isWon)) {
            this.isLost = false;
        }
    }

    public Boolean getIsLost() {
        return isLost;
    }

    public void setIsLost(Boolean isLost) {
        this.isLost = isLost;
        // Se for estágio de perda, não pode ser de ganho
        if (Boolean.TRUE.equals(isLost)) {
            this.isWon = false;
        }
    }

    public List<Opportunity> getOpportunities() {
        return opportunities;
    }

    public void setOpportunities(List<Opportunity> opportunities) {
        this.opportunities = opportunities;
    }

    // ========== Métodos de Negocio ==========

    /**
     * Adiciona uma oportunidade a este estágio
     */
    public void addOpportunity(Opportunity opportunity) {
        opportunities.add(opportunity);
        opportunity.setStage(this);
    }

    /**
     * Remove uma oportunidade deste estágio
     */
    public void removeOpportunity(Opportunity opportunity) {
        opportunities.remove(opportunity);
        opportunity.setStage(null);
    }

    /**
     * Verifica se é um estágio final (ganho ou perda)
     */
    public boolean isFinalStage() {
        return Boolean.TRUE.equals(isWon) || Boolean.TRUE.equals(isLost);
    }

    /**
     * Verifica se é um estágio de progresso (não é final)
     */
    public boolean isProgressStage() {
        return !isFinalStage();
    }

    /**
     * Retorna a cor em formato hexadecimal válido
     */
    public String getValidColor() {
        if (color != null && color.matches("^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$")) {
            return color;
        }
        // Cor padrão se não for válida
        return "#808080"; // Cinza
    }

    /**
     * Retorna o ícone/baseado no tipo de estágio
     */
    public String getStageIcon() {
        if (Boolean.TRUE.equals(isWon)) {
            return "🏆";
        } else if (Boolean.TRUE.equals(isLost)) {
            return "❌";
        } else {
            return "🔄"; // Setas para estágios de progresso
        }
    }

    /**
     * Clona um estágio (útil para criar pipelines personalizados)
     */
    public PipelineStage clone() {
        PipelineStage clone = new PipelineStage();
        clone.setName(this.name);
        clone.setDescription(this.description);
        clone.setOrder(this.order);
        clone.setColor(this.color);
        clone.setProbability(this.probability);
        clone.setIsWon(this.isWon);
        clone.setIsLost(this.isLost);
        return clone;
    }

    @Override
    public String toString() {
        return "PipelineStage{" +
                "id=" + getId() +
                ", name='" + name + '\'' +
                ", order=" + order +
                ", probability=" + probability +
                ", isWon=" + isWon +
                ", isLost=" + isLost +
                '}';
    }
}