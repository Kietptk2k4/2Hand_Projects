package com.twohands.commerce_service.infrastructure.persistence.jpa.repository;

import com.twohands.commerce_service.infrastructure.persistence.jpa.entity.UserAddressEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserAddressJpaRepository extends JpaRepository<UserAddressEntity, UUID> {

    Optional<UserAddressEntity> findByIdAndUserId(UUID id, UUID userId);
}
