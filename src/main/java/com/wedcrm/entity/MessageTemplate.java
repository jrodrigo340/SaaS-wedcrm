package com.wedcrm.entity;

import com.wedcrm.enums.MessageChannel;
import com.wedcrm.enums.TemplateCategory;
import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "message_templates")
public class MessageTemplate extends AbstractEntity {

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageChannel channel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TemplateCategory category;

    @Column(length = 300)
    private String subject;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    @ElementCollection
    @CollectionTable(
            name = "template_variables",
            joinColumns = @JoinColumn(name = "template_id")
    )
    @Column(name = "variable")
    private List<String> variables;

    @Column(nullable = false)
    private Boolean isActive = true;

    @Column(length = 5)
    private String language = "pt-BR";

    @OneToMany(mappedBy = "template")
    private List<AutomationRule> automationRules;

    @Column(nullable = false)
    private Integer usageCount = 0;

}
