package com.wedcrm.entity;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "pipeline_stages")
public class PipelineStage extends AbstractEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private Integer order;

    @Column(length = 7)
    private String color;

    @Column
    private Integer probability;

    @Column(nullable = false)
    private Boolean isWon = false;

    @Column(nullable = false)
    private Boolean isLost = false;

    @OneToMany(mappedBy = "stage")
    private List<Opportunity> opportunities;

}