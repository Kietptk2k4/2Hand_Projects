package com.twohands.commerce_service.infrastructure.persistence.catalog;

import com.twohands.commerce_service.domain.catalog.ProductCategoryRepository;
import com.twohands.commerce_service.infrastructure.persistence.jpa.repository.ProductCategoryJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public class ProductCategoryRepositoryAdapter implements ProductCategoryRepository {

    private final ProductCategoryJpaRepository productCategoryJpaRepository;

    public ProductCategoryRepositoryAdapter(ProductCategoryJpaRepository productCategoryJpaRepository) {
        this.productCategoryJpaRepository = productCategoryJpaRepository;
    }

    @Override
    public boolean existsActiveById(UUID categoryId) {
        return productCategoryJpaRepository.existsByIdAndActiveTrue(categoryId);
    }
}
