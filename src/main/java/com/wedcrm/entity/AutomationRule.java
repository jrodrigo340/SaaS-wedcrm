package com.wedcrm.entity;

import com.wedcrm.enums.AutomationTrigger;
import com.wedcrm.enums.MessageChannel;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "automation_rules")
public class AutomationRule extends AbstractEntity {

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AutomationTrigger trigger;

    @Column(columnDefinition = "TEXT")
    private String conditions;

    @ManyToOne(optional = false)
    @JoinColumn(name = "template_id")
    private MessageTemplate template;

    @Column(nullable = false)
    private Integer delayMinutes = 0;

    @Column(nullable = false)
    private Boolean isActive = true;

    private LocalDateTime lastExecutedAt;

    @Column(nullable = false)
    private Long executionCount = 0L;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageChannel channel;

}