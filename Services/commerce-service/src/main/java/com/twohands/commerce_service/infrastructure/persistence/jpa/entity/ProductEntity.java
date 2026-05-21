package com.twohands.commerce_service.infrastructure.persistence.jpa.entity;

import com.twohands.commerce_service.infrastructure.persistence.jpa.enums.ProductStatusType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Table(name = "products")
public class ProductEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "seller_id", nullable = false)
    private UUID sellerId;

    @Column(name = "shop_id", nullable = false)
    private UUID shopId;

    @Column(name = "category_id", nullable = false)
    private UUID categoryId;

    @Column(name = "title", nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status", nullable = false, columnDefinition = "product_status")
    private ProductStatusType status;

    public UUID getId() {
        return id;
    }

    public UUID getSellerId() {
        return sellerId;
    }

    public UUID getShopId() {
        return shopId;
    }

    public UUID getCategoryId() {
        return categoryId;
    }

    public String getTitle() {
        return title;
    }

    public ProductStatusType getStatus() {
        return status;
    }
}
