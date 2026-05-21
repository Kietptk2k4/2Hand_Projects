package com.twohands.commerce_service.infrastructure.persistence.jpa.repository;

import com.twohands.commerce_service.infrastructure.persistence.jpa.entity.CartItemEntity;
import com.twohands.commerce_service.infrastructure.persistence.jpa.enums.CartItemStatusType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CartItemJpaRepository extends JpaRepository<CartItemEntity, UUID> {

    Optional<CartItemEntity> findByCartIdAndProductId(UUID cartId, UUID productId);

    List<CartItemEntity> findByCartIdAndIdIn(UUID cartId, Collection<UUID> ids);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE CartItemEntity ci
            SET ci.status = :invalidStatus, ci.updatedAt = :updatedAt
            WHERE ci.productId = :productId
              AND ci.status IN :statuses
            """)
    int markInvalidByProductId(
            @Param("productId") UUID productId,
            @Param("invalidStatus") CartItemStatusType invalidStatus,
            @Param("statuses") Collection<CartItemStatusType> statuses,
            @Param("updatedAt") Instant updatedAt
    );
}
