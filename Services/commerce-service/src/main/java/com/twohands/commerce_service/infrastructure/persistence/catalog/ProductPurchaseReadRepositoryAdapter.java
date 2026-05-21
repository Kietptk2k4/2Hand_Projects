package com.twohands.commerce_service.infrastructure.persistence.catalog;

import com.twohands.commerce_service.domain.catalog.ActiveProductPrice;
import com.twohands.commerce_service.domain.catalog.ProductPriceCalculator;
import com.twohands.commerce_service.domain.catalog.ProductPurchaseContext;
import com.twohands.commerce_service.domain.catalog.ProductPurchaseReadRepository;
import com.twohands.commerce_service.infrastructure.persistence.jpa.entity.ProductEntity;
import com.twohands.commerce_service.infrastructure.persistence.jpa.entity.ProductInventoryEntity;
import com.twohands.commerce_service.infrastructure.persistence.jpa.entity.ProductPriceEntity;
import com.twohands.commerce_service.infrastructure.persistence.jpa.mapper.PersistenceEnumMapper;
import com.twohands.commerce_service.infrastructure.persistence.jpa.repository.ProductCategoryJpaRepository;
import com.twohands.commerce_service.infrastructure.persistence.jpa.repository.ProductInventoryJpaRepository;
import com.twohands.commerce_service.infrastructure.persistence.jpa.repository.ProductJpaRepository;
import com.twohands.commerce_service.infrastructure.persistence.jpa.repository.ProductMediaJpaRepository;
import com.twohands.commerce_service.infrastructure.persistence.jpa.repository.ProductPriceJpaRepository;
import com.twohands.commerce_service.infrastructure.persistence.jpa.repository.SellerShopJpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ProductPurchaseReadRepositoryAdapter implements ProductPurchaseReadRepository {

    private final ProductJpaRepository productJpaRepository;
    private final SellerShopJpaRepository sellerShopJpaRepository;
    private final ProductCategoryJpaRepository productCategoryJpaRepository;
    private final ProductInventoryJpaRepository productInventoryJpaRepository;
    private final ProductPriceJpaRepository productPriceJpaRepository;
    private final ProductMediaJpaRepository productMediaJpaRepository;

    public ProductPurchaseReadRepositoryAdapter(
            ProductJpaRepository productJpaRepository,
            SellerShopJpaRepository sellerShopJpaRepository,
            ProductCategoryJpaRepository productCategoryJpaRepository,
            ProductInventoryJpaRepository productInventoryJpaRepository,
            ProductPriceJpaRepository productPriceJpaRepository,
            ProductMediaJpaRepository productMediaJpaRepository
    ) {
        this.productJpaRepository = productJpaRepository;
        this.sellerShopJpaRepository = sellerShopJpaRepository;
        this.productCategoryJpaRepository = productCategoryJpaRepository;
        this.productInventoryJpaRepository = productInventoryJpaRepository;
        this.productPriceJpaRepository = productPriceJpaRepository;
        this.productMediaJpaRepository = productMediaJpaRepository;
    }

    @Override
    public Optional<ProductPurchaseContext> findByProductId(UUID productId) {
        return productJpaRepository.findById(productId)
                .flatMap(this::toContext);
    }

    @Override
    public Map<UUID, ProductPurchaseContext> findByProductIds(Collection<UUID> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return Map.of();
        }
        Map<UUID, ProductPurchaseContext> result = new HashMap<>();
        for (ProductEntity product : productJpaRepository.findAllById(productIds)) {
            toContext(product).ifPresent(context -> result.put(product.getId(), context));
        }
        return result;
    }

    private Optional<ProductPurchaseContext> toContext(ProductEntity product) {
        return sellerShopJpaRepository.findById(product.getShopId())
                .flatMap(shop -> productCategoryJpaRepository.findById(product.getCategoryId())
                        .map(category -> {
                            int stockQuantity = productInventoryJpaRepository.findById(product.getId())
                                    .map(ProductInventoryEntity::getStockQuantity)
                                    .orElse(0);
                            ActiveProductPrice activePrice = productPriceJpaRepository
                                    .findActivePrice(product.getId(), Instant.now())
                                    .map(this::toActivePrice)
                                    .orElse(null);
                            String imageUrl = productMediaJpaRepository
                                    .findFirstByProductIdOrderBySortOrderAsc(product.getId())
                                    .map(media -> media.getMediaUrl())
                                    .orElse(null);

                            return new ProductPurchaseContext(
                                    product.getId(),
                                    product.getSellerId(),
                                    product.getShopId(),
                                    product.getTitle(),
                                    PersistenceEnumMapper.toDomain(product.getStatus()),
                                    PersistenceEnumMapper.toDomain(shop.getStatus()),
                                    category.isActive(),
                                    product.getWeightGram(),
                                    stockQuantity,
                                    activePrice,
                                    imageUrl
                            );
                        }));
    }

    private ActiveProductPrice toActivePrice(ProductPriceEntity priceEntity) {
        var effective = ProductPriceCalculator.effectivePrice(priceEntity.getPrice(), priceEntity.getSalePrice());
        return new ActiveProductPrice(priceEntity.getPrice(), priceEntity.getSalePrice(), effective);
    }
}
