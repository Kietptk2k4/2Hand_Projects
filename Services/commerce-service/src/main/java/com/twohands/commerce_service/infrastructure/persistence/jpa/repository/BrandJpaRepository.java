package com.twohands.commerce_service.infrastructure.persistence.jpa.repository;

import com.twohands.commerce_service.infrastructure.persistence.jpa.entity.BrandEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BrandJpaRepository extends JpaRepository<BrandEntity, UUID> {

    boolean existsByIdAndActiveTrue(UUID id);
}