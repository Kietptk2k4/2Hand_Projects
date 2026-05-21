package com.twohands.commerce_service.infrastructure.persistence.jpa.repository;

import com.twohands.commerce_service.infrastructure.persistence.jpa.entity.SellerShippingProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface SellerShippingProfileJpaRepository extends JpaRepository<SellerShippingProfileEntity, UUID> {

    List<SellerShippingProfileEntity> findByShopIdIn(Collection<UUID> shopIds);
}
