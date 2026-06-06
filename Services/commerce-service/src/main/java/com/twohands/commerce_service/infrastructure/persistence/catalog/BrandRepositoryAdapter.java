package com.twohands.commerce_service.infrastructure.persistence.catalog;

import com.twohands.commerce_service.domain.catalog.BrandRepository;
import com.twohands.commerce_service.infrastructure.persistence.jpa.repository.BrandJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public class BrandRepositoryAdapter implements BrandRepository {

    private final BrandJpaRepository brandJpaRepository;

    public BrandRepositoryAdapter(BrandJpaRepository brandJpaRepository) {
        this.brandJpaRepository = brandJpaRepository;
    }

    @Override
    public boolean existsActiveById(UUID brandId) {
        return brandJpaRepository.existsByIdAndActiveTrue(brandId);
    }
}