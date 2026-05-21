package com.twohands.commerce_service.infrastructure.persistence.jpa.repository;

import com.twohands.commerce_service.infrastructure.persistence.jpa.entity.ProductInventoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProductInventoryJpaRepository extends JpaRepository<ProductInventoryEntity, UUID> {
}
