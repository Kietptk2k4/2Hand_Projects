package com.twohands.commerce_service.infrastructure.persistence.jpa.repository;

import com.twohands.commerce_service.infrastructure.persistence.jpa.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProductJpaRepository extends JpaRepository<ProductEntity, UUID> {
}
