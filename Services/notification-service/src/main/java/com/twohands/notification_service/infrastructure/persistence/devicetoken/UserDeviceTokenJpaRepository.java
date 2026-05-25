package com.twohands.notification_service.infrastructure.persistence.devicetoken;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserDeviceTokenJpaRepository extends JpaRepository<UserDeviceTokenEntity, UUID> {

    Optional<UserDeviceTokenEntity> findByDeviceToken(String deviceToken);

    List<UserDeviceTokenEntity> findByUserIdOrderByActiveDescUpdatedAtDesc(UUID userId);

    List<UserDeviceTokenEntity> findByUserIdAndActiveTrueOrderByUpdatedAtDesc(UUID userId);

    boolean existsByUserIdAndActiveTrue(UUID userId);

    List<UserDeviceTokenEntity> findByActiveTrueAndLastUsedAtIsNullAndUpdatedAtBeforeOrderByUpdatedAtAsc(
            Instant updatedBefore,
            Pageable pageable
    );
}
