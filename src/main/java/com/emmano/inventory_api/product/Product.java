package com.emmano.inventory_api.product;

import com.emmano.inventory_api.category.Category;
import com.emmano.inventory_api.supplier.Supplier;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false,length = 200)
    private String name;
    @Column(length = 64, nullable = false,unique = true)
    private String sku;
    @Column(length = 1000)
    private String description;
    @Column(nullable = false,precision = 19,scale = 2)
    private BigDecimal price;
    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;
    @Column(nullable = false)
    private boolean active= true;
    @Column(name = "created_at",nullable = false,updatable = false)
    private Instant createdAt;
    @Column(name = "updated_at",nullable = false)
    private Instant updatedAt;



    public Product(String name, String sku, String description, BigDecimal price, Category category, Supplier supplier) {
        this.name = name;
        this.sku = sku;
        this.description = description;
        this.price = price;
        this.category = category;
        this.supplier = supplier;
        this.active = true;
    }

    public void updateDetails(String name, String description, BigDecimal price, Category category, Supplier supplier) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.supplier = supplier;
    }

    public void deactivate() {
        this.active = false;
    }


    @PrePersist
    public void setInitialTimestamp() {
        Instant now = Instant.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
    }
    @PreUpdate
    public void setUpdateTimestamp() {
        updatedAt = Instant.now();
    }
}
