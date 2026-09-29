package com.emmano.inventory_api.category;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "categories")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(length = 100, nullable = false, unique = true)
    private String name;
    @Column(length = 500)
    private String description;
    @Column(name = "created_at",nullable = false,updatable = false)
    private Instant createdAt;


//    Getters and Setters below
    protected Category() {

    }
    public Category(String name, String description) {
        this.name = name;
        this.description = description;
    }
    public Long getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public String getDescription() {
        return description;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    @PrePersist
    void setCreatedAt(){
        if(createdAt == null){
            createdAt = Instant.now();
        }
    }
}
