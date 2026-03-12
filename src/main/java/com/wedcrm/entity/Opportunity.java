package com.wedcrm.entity;

import com.wedcrm.enums.OpportunityStatus;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "opportunities")
public class Opportunity extends AbstractEntity {

    @Column(nullable = false, length = 200)
    private String title;

    @ManyToOne(optional = false)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @ManyToOne(optional = false)
    @JoinColumn(name = "stage_id")
    private PipelineStage stage;

    @ManyToOne
    @JoinColumn(name = "assigned_to")
    private User assignedTo;

    @Column(precision = 15, scale = 2)
    private BigDecimal value;

    @Column
    private Integer probability;

    private LocalDate expectedCloseDate;

    private LocalDateTime closedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OpportunityStatus status;

    @Column(length = 500)
    private String lostReason;

    @OneToMany(mappedBy = "opportunity")
    private List<OpportunityProduct> products;

    @OneToMany(mappedBy = "opportunity")
    private List<Activity> activities;

    @Column(length = 5000)
    private String notes;

}