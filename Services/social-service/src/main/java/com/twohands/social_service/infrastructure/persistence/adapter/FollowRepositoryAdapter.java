package com.twohands.social_service.infrastructure.persistence.adapter;

import com.twohands.social_service.domain.follow.Follow;
import com.twohands.social_service.domain.follow.FollowRepository;
import com.twohands.social_service.domain.follow.FollowStatus;
import com.twohands.social_service.infrastructure.persistence.jpa.entity.FollowEntity;
import com.twohands.social_service.infrastructure.persistence.jpa.entity.FollowStatusDb;
import com.twohands.social_service.infrastructure.persistence.jpa.repository.JpaFollowRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class FollowRepositoryAdapter implements FollowRepository {

    private final JpaFollowRepository jpaFollowRepository;

    public FollowRepositoryAdapter(JpaFollowRepository jpaFollowRepository) {
        this.jpaFollowRepository = jpaFollowRepository;
    }

    @Override
    public List<UUID> findAcceptedFolloweeIds(UUID followerId) {
        return jpaFollowRepository.findFolloweeIdsByFollowerIdAndStatus(followerId, FollowStatusDb.ACCEPTED);
    }

    @Override
    public Optional<Follow> findByFollowerIdAndFolloweeId(UUID followerId, UUID followeeId) {
        return jpaFollowRepository.findByFollowerIdAndFolloweeId(followerId, followeeId)
                .map(this::toDomain);
    }

    @Override
    public void save(Follow follow) {
        FollowEntity entity = new FollowEntity();
        entity.setFollowerId(follow.followerId());
        entity.setFolloweeId(follow.followeeId());
        entity.setStatus(toDbStatus(follow.status()));
        entity.setCreatedAt(follow.createdAt());
        jpaFollowRepository.save(entity);
    }

    @Override
    public void deleteByFollowerIdAndFolloweeId(UUID followerId, UUID followeeId) {
        jpaFollowRepository.deleteByFollowerIdAndFolloweeId(followerId, followeeId);
    }

    private Follow toDomain(FollowEntity entity) {
        return new Follow(
                entity.getFollowerId(),
                entity.getFolloweeId(),
                toDomainStatus(entity.getStatus()),
                entity.getCreatedAt()
        );
    }

    private FollowStatus toDomainStatus(FollowStatusDb status) {
        return FollowStatus.valueOf(status.name());
    }

    private FollowStatusDb toDbStatus(FollowStatus status) {
        return FollowStatusDb.valueOf(status.name());
    }
}
