package com.wedcrm.entity;

import com.wedcrm.enums.Direction;
import com.wedcrm.enums.InteractionStatus;
import com.wedcrm.enums.InteractionType;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "interactions")
public class Interaction extends AbstractEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InteractionType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Direction direction;

    @Column(length = 300)
    private String subject;

    @Column(nullable = false, length = 10000)
    private String content;

    @Column(length = 50)
    private String channel;

    private LocalDateTime sentAt;

    private LocalDateTime readAt;

    @Enumerated(EnumType.STRING)
    private InteractionStatus status;

    @Column(nullable = false)
    private Boolean isAutomatic = false;

    @ManyToOne
    @JoinColumn(name = "template_id")
    private MessageTemplate templateUsed;

}