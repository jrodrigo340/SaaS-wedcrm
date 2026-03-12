package com.wedcrm.entity;

import com.wedcrm.enums.Role;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users")
public class User extends AbstractEntity {

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(length = 20)
    private String phone;

    private String avatarUrl;

    private LocalDateTime lastLogin;

    @Column(length = 500)
    private String refreshToken;

    @Column(nullable = false)
    private Boolean emailVerified = false;

    @OneToMany(mappedBy = "assignedTo")
    private List<Customer> customers;

    @OneToMany(mappedBy = "assignedTo")
    private List<Activity> activities;

}