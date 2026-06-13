package com.twohands.social_service.infrastructure.persistence.adapter;

import com.twohands.social_service.domain.follow.Follow;
import com.twohands.social_service.domain.follow.FollowRelationEntry;
import com.twohands.social_service.domain.follow.FollowRepository;
import com.twohands.social_service.domain.follow.FollowStatus;
import com.twohands.social_service.domain.post.PageResult;
import com.twohands.social_service.infrastructure.persistence.jpa.entity.FollowEntity;
import com.twohands.social_service.infrastructure.persistence.jpa.entity.FollowStatusDb;
import com.twohands.social_service.infrastructure.persistence.jpa.repository.JpaFollowRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
    public List<UUID> findFolloweeIdsByFollowerId(UUID followerId) {
        return jpaFollowRepository.findFolloweeIdsByFollowerId(followerId);
    }

    @Override
    public List<UUID> findAcceptedFolloweeIds(UUID followerId) {
        return jpaFollowRepository.findFolloweeIdsByFollowerIdAndStatus(followerId, FollowStatusDb.ACCEPTED);
    }

    @Override
    public List<UUID> findAcceptedFollowerIds(UUID followeeId) {
        return jpaFollowRepository.findFollowerIdsByFolloweeIdAndStatus(followeeId, FollowStatusDb.ACCEPTED);
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

    @Override
    public long countAcceptedFollowers(UUID followeeId) {
        return jpaFollowRepository.countByFolloweeIdAndStatus(followeeId, FollowStatusDb.ACCEPTED);
    }

    @Override
    public long countAcceptedFollowing(UUID followerId) {
        return jpaFollowRepository.countByFollowerIdAndStatus(followerId, FollowStatusDb.ACCEPTED);
    }

    @Override
    public PageResult<FollowRelationEntry> findAcceptedFollowersPage(UUID followeeId, int page, int size) {
        Page<FollowEntity> result = jpaFollowRepository.findByFolloweeIdAndStatusOrderByCreatedAtDesc(
                followeeId,
                FollowStatusDb.ACCEPTED,
                PageRequest.of(page, size)
        );
        return toFollowersPage(result);
    }

    @Override
    public PageResult<FollowRelationEntry> findAcceptedFollowingPage(UUID followerId, int page, int size) {
        Page<FollowEntity> result = jpaFollowRepository.findByFollowerIdAndStatusOrderByCreatedAtDesc(
                followerId,
                FollowStatusDb.ACCEPTED,
                PageRequest.of(page, size)
        );
        return toFollowingPage(result);
    }

    private PageResult<FollowRelationEntry> toFollowersPage(Page<FollowEntity> page) {
        List<FollowRelationEntry> items = page.getContent().stream()
                .map(entity -> new FollowRelationEntry(entity.getFollowerId(), entity.getCreatedAt()))
                .toList();
        return toPageResult(page, items);
    }

    private PageResult<FollowRelationEntry> toFollowingPage(Page<FollowEntity> page) {
        List<FollowRelationEntry> items = page.getContent().stream()
                .map(entity -> new FollowRelationEntry(entity.getFolloweeId(), entity.getCreatedAt()))
                .toList();
        return toPageResult(page, items);
    }

    private PageResult<FollowRelationEntry> toPageResult(Page<FollowEntity> page, List<FollowRelationEntry> items) {
        return new PageResult<>(
                items,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext()
        );
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
