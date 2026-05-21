package com.twohands.commerce_service.infrastructure.persistence.shop;

import com.twohands.commerce_service.domain.shop.SellerShop;
import com.twohands.commerce_service.domain.shop.SellerShopRepository;
import com.twohands.commerce_service.infrastructure.persistence.jpa.mapper.PersistenceEnumMapper;
import com.twohands.commerce_service.infrastructure.persistence.jpa.repository.SellerShopJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class SellerShopRepositoryAdapter implements SellerShopRepository {

    private final SellerShopJpaRepository sellerShopJpaRepository;

    public SellerShopRepositoryAdapter(SellerShopJpaRepository sellerShopJpaRepository) {
        this.sellerShopJpaRepository = sellerShopJpaRepository;
    }

    @Override
    public Optional<SellerShop> findBySellerId(UUID sellerId) {
        return sellerShopJpaRepository.findBySellerId(sellerId)
                .map(entity -> new SellerShop(
                        entity.getId(),
                        entity.getSellerId(),
                        PersistenceEnumMapper.toDomain(entity.getStatus())
                ));
    }
}
