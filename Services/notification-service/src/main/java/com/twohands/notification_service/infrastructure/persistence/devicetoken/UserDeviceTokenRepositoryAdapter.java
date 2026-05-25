package com.twohands.notification_service.infrastructure.persistence.devicetoken;

import com.twohands.notification_service.domain.devicetoken.UserDeviceToken;
import com.twohands.notification_service.domain.devicetoken.UserDeviceTokenRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class UserDeviceTokenRepositoryAdapter implements UserDeviceTokenRepository {

    private final UserDeviceTokenJpaRepository jpaRepository;

    public UserDeviceTokenRepositoryAdapter(UserDeviceTokenJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public UserDeviceToken save(UserDeviceToken token) {
        UserDeviceTokenEntity entity = jpaRepository.findByDeviceToken(token.deviceToken())
                .orElseGet(UserDeviceTokenEntity::new);

        if (entity.getId() == null) {
            entity.setId(token.id());
        }
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(token.createdAt());
        }

        entity.setUserId(token.userId());
        entity.setDeviceType(token.deviceType());
        entity.setDeviceToken(token.deviceToken());
        entity.setActive(token.active());
        entity.setUpdatedAt(token.updatedAt());
        entity.setLastUsedAt(token.lastUsedAt());
        return toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<UserDeviceToken> findByDeviceToken(String deviceToken) {
        return jpaRepository.findByDeviceToken(deviceToken).map(this::toDomain);
    }

    @Override
    public boolean existsActiveByUserId(UUID userId) {
        return jpaRepository.existsByUserIdAndActiveTrue(userId);
    }

    private UserDeviceToken toDomain(UserDeviceTokenEntity entity) {
        return new UserDeviceToken(
                entity.getId(),
                entity.getUserId(),
                entity.getDeviceType(),
                entity.getDeviceToken(),
                entity.isActive(),
                entity.getUpdatedAt(),
                entity.getLastUsedAt(),
                entity.getCreatedAt()
        );
    }
}
