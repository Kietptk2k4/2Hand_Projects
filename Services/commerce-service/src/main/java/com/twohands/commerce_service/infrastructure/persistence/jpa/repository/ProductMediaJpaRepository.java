package com.twohands.commerce_service.infrastructure.persistence.jpa.repository;

import com.twohands.commerce_service.infrastructure.persistence.jpa.entity.ProductMediaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProductMediaJpaRepository extends JpaRepository<ProductMediaEntity, UUID> {

    Optional<ProductMediaEntity> findFirstByProductIdOrderBySortOrderAsc(UUID productId);
}
