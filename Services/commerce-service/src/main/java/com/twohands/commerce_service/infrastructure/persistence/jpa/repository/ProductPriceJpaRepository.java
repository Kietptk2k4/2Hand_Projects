package com.twohands.commerce_service.infrastructure.persistence.jpa.repository;

import com.twohands.commerce_service.infrastructure.persistence.jpa.entity.ProductPriceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface ProductPriceJpaRepository extends JpaRepository<ProductPriceEntity, UUID> {

    @Query("""
            SELECT p FROM ProductPriceEntity p
            WHERE p.productId = :productId
              AND p.startAt <= :now
              AND (p.endAt IS NULL OR p.endAt > :now)
            ORDER BY p.startAt DESC
            LIMIT 1
            """)
    Optional<ProductPriceEntity> findActivePrice(@Param("productId") UUID productId, @Param("now") Instant now);
}
