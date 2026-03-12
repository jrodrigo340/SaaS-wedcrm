package com.wedcrm.entity;

import com.wedcrm.enums.CustomerStatus;
import com.wedcrm.enums.LeadSource;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "customers")
public class Customer extends AbstractEntity {

    @Column(nullable = false, length = 100)
    private String firstName;

    @Column(nullable = false, length = 100)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(length = 20)
    private String whatsapp;

    @Column(length = 200)
    private String company;

    @Column(length = 100)
    private String position;

    @Column(unique = true, length = 20)
    private String cpfCnpj;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CustomerStatus status;

    @Enumerated(EnumType.STRING)
    private LeadSource source;

    @ManyToOne
    @JoinColumn(name = "assigned_to")
    private User assignedTo;

    @ManyToMany
    @JoinTable(
            name = "customer_tags",
            joinColumns = @JoinColumn(name = "customer_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private List<Tag> tags;

    @Embedded
    private Address address;

    @Column(length = 5000)
    private String notes;

    private LocalDate birthday;

    @Column(nullable = false)
    private Integer score = 0;

    private LocalDate lastContactDate;

    @OneToMany(mappedBy = "customer")
    private List<Opportunity> opportunities;

    @OneToMany(mappedBy = "customer")
    private List<Interaction> interactions;

    @OneToMany(mappedBy = "customer")
    private List<Activity> activities;

    @ElementCollection
    @CollectionTable(
            name = "customer_custom_fields",
            joinColumns = @JoinColumn(name = "customer_id")
    )
    @MapKeyColumn(name = "field_key")
    @Column(name = "field_value")
    private Map<String, String> customFields;

}