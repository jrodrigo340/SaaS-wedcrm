package com.wedcrm.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "opportunity_products")
public class OpportunityProduct extends AbstractEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "opportunity_id")
    private Opportunity opportunity;

    @ManyToOne(optional = false)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private BigDecimal unitPrice;

    private BigDecimal discount;

    @Column(nullable = false)
    private BigDecimal totalPrice;

    @Column(length = 1000)
    private String notes;

}
