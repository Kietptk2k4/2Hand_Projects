package com.twohands.commerce_service.infrastructure.persistence.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "product_inventories")
public class ProductInventoryEntity {

    @Id
    @Column(name = "product_id", nullable = false, updatable = false)
    private UUID productId;

    @Column(name = "stock_quantity", nullable = false)
    private int stockQuantity;

    public UUID getProductId() {
        return productId;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }
}
