package com.twohands.commerce_service.infrastructure.persistence.product;

import com.twohands.commerce_service.domain.product.Product;
import com.twohands.commerce_service.domain.product.ProductRepository;
import com.twohands.commerce_service.infrastructure.persistence.jpa.entity.ProductEntity;
import com.twohands.commerce_service.infrastructure.persistence.jpa.mapper.PersistenceEnumMapper;
import com.twohands.commerce_service.infrastructure.persistence.jpa.repository.ProductJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class ProductRepositoryAdapter implements ProductRepository {

    private final ProductJpaRepository productJpaRepository;

    public ProductRepositoryAdapter(ProductJpaRepository productJpaRepository) {
        this.productJpaRepository = productJpaRepository;
    }

    @Override
    public Optional<Product> findById(UUID productId) {
        return productJpaRepository.findById(productId).map(this::toDomain);
    }

    @Override
    public Product save(Product product) {
        ProductEntity entity = productJpaRepository.findById(product.id())
                .orElseThrow(() -> new IllegalStateException("Product not found: " + product.id()));
        entity.setStatus(PersistenceEnumMapper.toEntity(product.status()));
        entity.setUpdatedAt(product.updatedAt());
        return toDomain(productJpaRepository.save(entity));
    }

    private Product toDomain(ProductEntity entity) {
        return new Product(
                entity.getId(),
                entity.getSellerId(),
                entity.getShopId(),
                entity.getTitle(),
                PersistenceEnumMapper.toDomain(entity.getStatus()),
                entity.getUpdatedAt()
        );
    }
}
