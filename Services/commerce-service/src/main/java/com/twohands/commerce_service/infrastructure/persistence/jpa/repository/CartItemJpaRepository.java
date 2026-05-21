package com.twohands.commerce_service.infrastructure.persistence.jpa.repository;

import com.twohands.commerce_service.infrastructure.persistence.jpa.entity.CartItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CartItemJpaRepository extends JpaRepository<CartItemEntity, UUID> {

    Optional<CartItemEntity> findByCartIdAndProductId(UUID cartId, UUID productId);
}
