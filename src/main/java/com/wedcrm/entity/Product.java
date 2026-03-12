package com.wedcrm.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "products")
public class Product extends AbstractEntity {

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 2000)
    private String description;

    @Column(unique = true, length = 50)
    private String sku;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(length = 30)
    private String unit;

    @Column(length = 100)
    private String category;

    @Column(nullable = false)
    private Boolean isActive = true;

}