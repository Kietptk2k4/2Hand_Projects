package com.twohands.social_service.infrastructure.persistence.adapter;

import com.twohands.social_service.domain.user.UserProjection;
import com.twohands.social_service.domain.user.UserProjectionRepository;
import com.twohands.social_service.infrastructure.persistence.mongo.document.UserProjectionDocument;
import com.twohands.social_service.infrastructure.persistence.mongo.repository.MongoUserProjectionRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class UserProjectionRepositoryAdapter implements UserProjectionRepository {

    private final MongoUserProjectionRepository mongoUserProjectionRepository;

    public UserProjectionRepositoryAdapter(MongoUserProjectionRepository mongoUserProjectionRepository) {
        this.mongoUserProjectionRepository = mongoUserProjectionRepository;
    }

    @Override
    public Optional<UserProjection> findByUserId(UUID userId) {
        return mongoUserProjectionRepository.findByUserId(userId.toString())
                .map(this::toDomain);
    }

    @Override
    public UserProjection upsert(UserProjection projection) {
        UserProjectionDocument document = mongoUserProjectionRepository.findByUserId(projection.userId())
                .orElseGet(UserProjectionDocument::new);

        if (document.getId() == null) {
            document.setUserId(projection.userId());
        }
        if (projection.status() != null) {
            document.setStatus(projection.status());
        }
        if (projection.displayName() != null) {
            document.setDisplayName(projection.displayName());
        }
        if (projection.avatarUrl() != null) {
            document.setAvatarUrl(projection.avatarUrl());
        }
        if (projection.isPrivate() != null) {
            document.setIsPrivate(projection.isPrivate());
        }

        UserProjectionDocument saved = mongoUserProjectionRepository.save(document);
        return toDomain(saved);
    }

    private UserProjection toDomain(UserProjectionDocument doc) {
        return new UserProjection(
                doc.getUserId(),
                doc.getStatus(),
                doc.getDisplayName(),
                doc.getAvatarUrl(),
                doc.getIsPrivate()
        );
    }
}
