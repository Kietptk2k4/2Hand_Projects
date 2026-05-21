package com.twohands.commerce_service.infrastructure.persistence.jpa.repository;

import com.twohands.commerce_service.infrastructure.persistence.jpa.entity.SellerShopEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SellerShopJpaRepository extends JpaRepository<SellerShopEntity, UUID> {

    Optional<SellerShopEntity> findBySellerId(UUID sellerId);
}
