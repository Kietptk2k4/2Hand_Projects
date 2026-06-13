package com.twohands.social_service.infrastructure.persistence.jpa.repository;

import com.twohands.social_service.infrastructure.persistence.jpa.entity.FollowEntity;
import com.twohands.social_service.infrastructure.persistence.jpa.entity.FollowEntityId;
import com.twohands.social_service.infrastructure.persistence.jpa.entity.FollowStatusDb;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaFollowRepository extends JpaRepository<FollowEntity, FollowEntityId> {

    @Query("select f.followeeId from FollowEntity f where f.followerId = :followerId and f.status = :status")
    List<UUID> findFolloweeIdsByFollowerIdAndStatus(UUID followerId, FollowStatusDb status);

    @Query("select f.followeeId from FollowEntity f where f.followerId = :followerId")
    List<UUID> findFolloweeIdsByFollowerId(UUID followerId);

    @Query("select f.followerId from FollowEntity f where f.followeeId = :followeeId and f.status = :status")
    List<UUID> findFollowerIdsByFolloweeIdAndStatus(UUID followeeId, FollowStatusDb status);

    Optional<FollowEntity> findByFollowerIdAndFolloweeId(UUID followerId, UUID followeeId);

    void deleteByFollowerIdAndFolloweeId(UUID followerId, UUID followeeId);

    long countByFolloweeIdAndStatus(UUID followeeId, FollowStatusDb status);

    long countByFollowerIdAndStatus(UUID followerId, FollowStatusDb status);

    Page<FollowEntity> findByFolloweeIdAndStatusOrderByCreatedAtDesc(
            UUID followeeId,
            FollowStatusDb status,
            Pageable pageable
    );

    Page<FollowEntity> findByFollowerIdAndStatusOrderByCreatedAtDesc(
            UUID followerId,
            FollowStatusDb status,
            Pageable pageable
    );
}
