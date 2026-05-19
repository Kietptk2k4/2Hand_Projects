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
                .map(doc -> new UserProjection(
                        doc.getUserId(),
                        doc.getStatus(),
                        doc.getDisplayName(),
                        doc.getAvatarUrl()
                ));
    }
}
