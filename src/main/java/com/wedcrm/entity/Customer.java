package com.wedcrm.entity;

import com.wedcrm.enums.LeadSource;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "customers")

public class Customer extends AbstractEntity {

    @Column(nullable = false)
    private String name;

    @Column(unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    private LeadSource source;

    @Column(length = 1000)
    private String notes;

    @ManyToOne
    @JoinColumn(name = "assigned_to")
    private User assignedTo;
}
