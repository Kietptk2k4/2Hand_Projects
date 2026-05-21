package com.twohands.commerce_service.infrastructure.persistence.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "product_media")
public class ProductMediaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "media_url", nullable = false)
    private String mediaUrl;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    public UUID getProductId() {
        return productId;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public int getSortOrder() {
        return sortOrder;
    }
}
