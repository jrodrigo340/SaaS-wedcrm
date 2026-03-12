package com.wedcrm.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "tags")
public class Tag extends AbstractEntity {

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(length = 7)
    private String color;

    @Column(length = 500)
    private String description;

    @ManyToMany(mappedBy = "tags")
    private List<Customer> customers;

}
