package com.twohands.social_service.infrastructure.persistence.adapter;

import com.twohands.social_service.domain.user.UserProjection;
import com.twohands.social_service.domain.user.UserProjectionRepository;
import com.twohands.social_service.infrastructure.persistence.mongo.document.UserProjectionDocument;
import com.twohands.social_service.infrastructure.persistence.mongo.repository.MongoUserProjectionRepository;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class UserProjectionRepositoryAdapter implements UserProjectionRepository {

    private static final List<String> EXCLUDED_STATUSES = List.of("DELETED", "SUSPENDED");

    private final MongoUserProjectionRepository mongoUserProjectionRepository;
    private final MongoTemplate mongoTemplate;

    public UserProjectionRepositoryAdapter(
            MongoUserProjectionRepository mongoUserProjectionRepository,
            MongoTemplate mongoTemplate
    ) {
        this.mongoUserProjectionRepository = mongoUserProjectionRepository;
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public Optional<UserProjection> findByUserId(UUID userId) {
        return mongoUserProjectionRepository.findByUserId(userId.toString())
                .map(this::toDomain);
    }

    @Override
    public List<UserProjection> findByUserIds(List<UUID> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return List.of();
        }
        List<String> ids = userIds.stream().map(UUID::toString).toList();
        return mongoUserProjectionRepository.findByUserIdIn(ids).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<UserProjection> findActiveSuggestionCandidatesExcluding(Collection<String> excludeUserIds, int maxResults) {
        Query query = suggestionCandidatesQuery(excludeUserIds)
                .with(Sort.by(Sort.Direction.ASC, "display_name"))
                .limit(Math.max(maxResults, 0));
        return mongoTemplate.find(query, UserProjectionDocument.class).stream()
                .map(this::toDomain)
                .filter(this::isSuggestableProjection)
                .toList();
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

    private Query suggestionCandidatesQuery(Collection<String> excludeUserIds) {
        Criteria criteria = Criteria.where("status").nin(EXCLUDED_STATUSES);
        if (excludeUserIds != null && !excludeUserIds.isEmpty()) {
            criteria = criteria.and("user_id").nin(excludeUserIds);
        }
        return new Query(criteria);
    }

    private boolean isSuggestableProjection(UserProjection projection) {
        if (projection == null || projection.isDeleted() || projection.isSuspended()) {
            return false;
        }
        String displayName = projection.displayName();
        return displayName != null && !displayName.isBlank();
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
