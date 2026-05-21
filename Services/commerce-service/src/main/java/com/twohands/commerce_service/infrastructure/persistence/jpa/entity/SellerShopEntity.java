package com.twohands.commerce_service.infrastructure.persistence.jpa.entity;

import com.twohands.commerce_service.infrastructure.persistence.jpa.enums.ShopStatusType;
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
@Table(name = "seller_shops")
public class SellerShopEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "seller_id", nullable = false)
    private UUID sellerId;

    @Column(name = "shop_name", nullable = false)
    private String shopName;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status", nullable = false, columnDefinition = "shop_status")
    private ShopStatusType status;

    public UUID getId() {
        return id;
    }

    public UUID getSellerId() {
        return sellerId;
    }

    public String getShopName() {
        return shopName;
    }

    public ShopStatusType getStatus() {
        return status;
    }
}
