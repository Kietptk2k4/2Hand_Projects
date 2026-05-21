package com.twohands.commerce_service.infrastructure.persistence.jpa.repository;

import com.twohands.commerce_service.infrastructure.persistence.jpa.entity.CartEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface CartJpaRepository extends JpaRepository<CartEntity, UUID> {

    Optional<CartEntity> findByUserId(UUID userId);

    @Modifying
    @Query("UPDATE CartEntity c SET c.updatedAt = :updatedAt WHERE c.id = :cartId")
    void updateTimestamp(@Param("cartId") UUID cartId, @Param("updatedAt") Instant updatedAt);
}
