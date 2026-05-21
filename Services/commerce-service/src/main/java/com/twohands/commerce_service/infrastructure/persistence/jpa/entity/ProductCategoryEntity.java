package com.twohands.commerce_service.infrastructure.persistence.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "product_categories")
public class ProductCategoryEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    public UUID getId() {
        return id;
    }

    public boolean isActive() {
        return active;
    }
}
