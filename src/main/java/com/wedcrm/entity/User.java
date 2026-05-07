package com.wedcrm.entity;

import com.wedcrm.enums.Role;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
public class User extends AbstractEntity {

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Column(length = 20)
    private String phone;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Column(name = "refresh_token", length = 500)
    private String refreshToken;

    @Column(name = "email_verified", nullable = false)
    private Boolean emailVerified = false;

    // Inicialize as listas para evitar NullPointerException
    @OneToMany(mappedBy = "assignedTo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Customer> customers = new ArrayList<>();

    @OneToMany(mappedBy = "assignedTo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Activity> activities = new ArrayList<>();

    public User() {

    }

    public User(String name, String email, String password, Role role) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.emailVerified = false;
    }

    // ========== Getters E Setters ==========

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public Boolean getEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(Boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public List<Customer> getCustomers() {
        return customers;
    }

    public void setCustomers(List<Customer> customers) {
        this.customers = customers;
    }

    public List<Activity> getActivities() {
        return activities;
    }

    public void setActivities(List<Activity> activities) {
        this.activities = activities;
    }

    /**
     * Adiciona um cliente à lista de clientes do usuário
     */
    public void addCustomer(Customer customer) {
        customers.add(customer);
        customer.setAssignedTo(this);
    }

    /**
     * Remove um cliente da lista de clientes do usuário
     */
    public void removeCustomer(Customer customer) {
        customers.remove(customer);
        customer.setAssignedTo(null);
    }

    /**
     * Adiciona uma atividade à lista de atividades do usuário
     */
    public void addActivity(Activity activity) {
        activities.add(activity);
        activity.setAssignedTo(this);
    }

    /**
     * Remove uma atividade da lista de atividades do usuário
     */
    public void removeActivity(Activity activity) {
        activities.remove(activity);
        activity.setAssignedTo(null);
    }

    /**
     * Registra o último login do usuário
     */
    public void registerLogin() {
        this.lastLogin = LocalDateTime.now();
    }

    /**
     * Verifica se o usuário tem permissão de administrador
     */
    public boolean isAdmin() {
        return Role.ADMIN.equals(this.role);
    }

    /**
     * Verifica se o usuário tem permissão de gerente
     */
    public boolean isManager() {
        return Role.MANAGER.equals(this.role) || isAdmin();
    }

    /**
     * Verifica se o usuário tem permissão de vendedor
     */
    public boolean isSales() {
        return Role.SALES.equals(this.role) || isManager();
    }

    public boolean canAccess(Customer customer) {
        if (isAdmin() || isManager()) {
            return true; // Admin e Manager podem acessar qualquer cliente
        }
        // Vendedor só pode acessar seus próprios clientes
        return this.equals(customer.getAssignedTo());
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + getId() +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", role=" + role +
                ", emailVerified=" + emailVerified +
                '}';
    }
}