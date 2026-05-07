package com.wedcrm.entity;

import com.wedcrm.enums.CustomerStatus;
import com.wedcrm.enums.LeadSource;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "customers")
public class Customer extends AbstractEntity {

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(length = 20)
    private String whatsapp;

    @Column(length = 200)
    private String company;

    @Column(length = 100)
    private String position;

    @Column(name = "cpf_cnpj", unique = true, length = 20)
    private String cpfCnpj;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CustomerStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "lead_source", length = 30)
    private LeadSource source;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to_id")
    private User assignedTo;

    @ManyToMany
    @JoinTable(
            name = "customer_tags",
            joinColumns = @JoinColumn(name = "customer_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private List<Tag> tags = new ArrayList<>();

    @Embedded
    private Address address;

    @Column(length = 5000)
    private String notes;

    private LocalDate birthday;

    @Column(nullable = false)
    private Integer score = 0;

    @Column(name = "last_contact_date")
    private LocalDate lastContactDate;

    // Relacionamentos
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Opportunity> opportunities = new ArrayList<>();

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Interaction> interactions = new ArrayList<>();

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Activity> activities = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "customer_custom_fields",
            joinColumns = @JoinColumn(name = "customer_id"))
    @MapKeyColumn(name = "field_key", length = 100)
    @Column(name = "field_value", length = 500)
    private Map<String, String> customFields = new HashMap<>();

    // ========== CONSTRUTORES ==========

    public Customer() {}

    public Customer(String firstName, String lastName, String email, CustomerStatus status) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.status = status;
        this.score = 0;
    }

    // ========== Getters E Setters ==========

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getWhatsapp() { return whatsapp; }
    public void setWhatsapp(String whatsapp) { this.whatsapp = whatsapp; }

    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }

    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }

    public String getCpfCnpj() { return cpfCnpj; }
    public void setCpfCnpj(String cpfCnpj) { this.cpfCnpj = cpfCnpj; }

    public CustomerStatus getStatus() { return status; }
    public void setStatus(CustomerStatus status) { this.status = status; }

    public LeadSource getSource() { return source; }
    public void setSource(LeadSource source) { this.source = source; }

    public User getAssignedTo() { return assignedTo; }
    public void setAssignedTo(User assignedTo) { this.assignedTo = assignedTo; }

    public List<Tag> getTags() { return tags; }
    public void setTags(List<Tag> tags) { this.tags = tags; }

    public Address getAddress() { return address; }
    public void setAddress(Address address) { this.address = address; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDate getBirthday() { return birthday; }
    public void setBirthday(LocalDate birthday) { this.birthday = birthday; }

    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }

    public LocalDate getLastContactDate() { return lastContactDate; }
    public void setLastContactDate(LocalDate lastContactDate) { this.lastContactDate = lastContactDate; }

    public List<Opportunity> getOpportunities() { return opportunities; }
    public void setOpportunities(List<Opportunity> opportunities) { this.opportunities = opportunities; }

    public List<Interaction> getInteractions() { return interactions; }
    public void setInteractions(List<Interaction> interactions) { this.interactions = interactions; }

    public List<Activity> getActivities() { return activities; }
    public void setActivities(List<Activity> activities) { this.activities = activities; }

    public Map<String, String> getCustomFields() { return customFields; }
    public void setCustomFields(Map<String, String> customFields) { this.customFields = customFields; }

    // ========== Métodos De Negócio ==========

    public String getFullName() {
        return firstName + " " + lastName;
    }

    /**
     * Adiciona uma oportunidade ao cliente
     */
    public void addOpportunity(Opportunity opportunity) {
        opportunities.add(opportunity);
        opportunity.setCustomer(this);
    }

    /**
     * Remove uma oportunidade do cliente
     */
    public void removeOpportunity(Opportunity opportunity) {
        opportunities.remove(opportunity);
        opportunity.setCustomer(null);
    }

    /**
     * Adiciona uma interação ao cliente
     */
    public void addInteraction(Interaction interaction) {
        interactions.add(interaction);
        interaction.setCustomer(this);
        this.lastContactDate = LocalDate.now();
    }

    /**
     * Adiciona uma atividade ao cliente
     */
    public void addActivity(Activity activity) {
        activities.add(activity);
        activity.setCustomer(this);
    }

    /**
     * Adiciona uma tag ao cliente
     */
    public void addTag(Tag tag) {
        if (!tags.contains(tag)) {
            tags.add(tag);
        }
    }

    /**
     * Remove uma tag do cliente
     */
    public void removeTag(Tag tag) {
        tags.remove(tag);
    }

    /**
     * Adiciona um campo personalizado
     */
    public void addCustomField(String key, String value) {
        customFields.put(key, value);
    }

    /**
     * Remove um campo personalizado
     */
    public void removeCustomField(String key) {
        customFields.remove(key);
    }

    /**
     * Obtém o valor de um campo personalizado
     */
    public String getCustomField(String key) {
        return customFields.get(key);
    }

    /**
     * Calcula a idade do cliente baseado no aniversário
     */
    public Integer getAge() {
        if (birthday == null) return null;
        return Period.between(birthday, LocalDate.now()).getYears();
    }

    /**
     * Verifica se é aniversário hoje
     */
    public boolean isBirthdayToday() {
        if (birthday == null) return false;
        LocalDate today = LocalDate.now();
        return birthday.getMonth() == today.getMonth() &&
                birthday.getDayOfMonth() == today.getDayOfMonth();
    }

    /**
     * Atualiza o lead score
     */
    public void recalculateScore() {
        int newScore = 0;

        // Score base por status
        switch (status) {
            case CUSTOMER -> newScore += 50;
            case PROSPECT -> newScore += 30;
            case LEAD -> newScore += 10;
        }

        // Score por interações recentes
        if (lastContactDate != null) {
            long daysSinceLastContact = Period.between(lastContactDate, LocalDate.now()).getDays();
            if (daysSinceLastContact < 7) newScore += 20;
            else if (daysSinceLastContact < 30) newScore += 10;
        }

        // Score por oportunidades abertas
        long openOpportunities = opportunities.stream()
                .filter(opp -> "OPEN".equals(opp.getStatus()))
                .count();
        newScore += openOpportunities * 5;

        // Score por atividades pendentes
        long pendingActivities = activities.stream()
                .filter(act -> "PENDING".equals(act.getStatus()))
                .count();
        newScore += pendingActivities * 3;

        // Limita entre 0 e 100
        this.score = Math.min(100, Math.max(0, newScore));
    }

    /**
     * Verifica se o cliente está inativo
     */
    public boolean isInactive() {
        if (lastContactDate == null) return true;
        return Period.between(lastContactDate, LocalDate.now()).getDays() > 30;
    }

    /**
     * Promove o status do cliente (Lead -> Prospect -> Customer)
     */
    public void promote() {
        switch (status) {
            case LEAD -> this.status = CustomerStatus.PROSPECT;
            case PROSPECT -> this.status = CustomerStatus.CUSTOMER;
            default -> {}
        }
    }

    /**
     * Rebaixa o status do cliente
     */
    public void demote() {
        switch (status) {
            case CUSTOMER -> this.status = CustomerStatus.PROSPECT;
            case PROSPECT -> this.status = CustomerStatus.LEAD;
            default -> {}
        }
    }

    @Override
    public String toString() {
        return "Customer{" +
                "id=" + getId() +
                ", name='" + firstName + " " + lastName + '\'' +
                ", email='" + email + '\'' +
                ", status=" + status +
                ", score=" + score +
                '}';
    }
}