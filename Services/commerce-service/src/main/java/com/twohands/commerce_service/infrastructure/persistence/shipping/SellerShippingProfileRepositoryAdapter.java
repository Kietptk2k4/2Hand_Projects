package com.twohands.commerce_service.infrastructure.persistence.shipping;

import com.twohands.commerce_service.domain.shipping.SellerShippingProfile;
import com.twohands.commerce_service.domain.shipping.SellerShippingProfileRepository;
import com.twohands.commerce_service.infrastructure.persistence.jpa.entity.SellerShippingProfileEntity;
import com.twohands.commerce_service.infrastructure.persistence.jpa.entity.SellerShopEntity;
import com.twohands.commerce_service.infrastructure.persistence.jpa.repository.SellerShippingProfileJpaRepository;
import com.twohands.commerce_service.infrastructure.persistence.jpa.repository.SellerShopJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Repository
public class SellerShippingProfileRepositoryAdapter implements SellerShippingProfileRepository {

    private final SellerShippingProfileJpaRepository sellerShippingProfileJpaRepository;
    private final SellerShopJpaRepository sellerShopJpaRepository;

    public SellerShippingProfileRepositoryAdapter(
            SellerShippingProfileJpaRepository sellerShippingProfileJpaRepository,
            SellerShopJpaRepository sellerShopJpaRepository
    ) {
        this.sellerShippingProfileJpaRepository = sellerShippingProfileJpaRepository;
        this.sellerShopJpaRepository = sellerShopJpaRepository;
    }

    @Override
    public Map<UUID, SellerShippingProfile> findByShopIds(Collection<UUID> shopIds) {
        if (shopIds == null || shopIds.isEmpty()) {
            return Map.of();
        }
        Map<UUID, UUID> shopSellerIds = new HashMap<>();
        for (SellerShopEntity shop : sellerShopJpaRepository.findAllById(shopIds)) {
            shopSellerIds.put(shop.getId(), shop.getSellerId());
        }

        Map<UUID, SellerShippingProfile> profiles = new HashMap<>();
        for (SellerShippingProfileEntity entity : sellerShippingProfileJpaRepository.findByShopIdIn(shopIds)) {
            UUID sellerId = shopSellerIds.get(entity.getShopId());
            if (sellerId == null) {
                continue;
            }
            profiles.put(entity.getShopId(), new SellerShippingProfile(
                    entity.getShopId(),
                    sellerId,
                    entity.getProvinceCode(),
                    entity.getDistrictCode(),
                    entity.getWardCode()
            ));
        }
        return profiles;
    }
}
