package com.twohands.social_service.infrastructure.persistence.jpa.repository;

import com.twohands.social_service.infrastructure.persistence.jpa.entity.FollowEntity;
import com.twohands.social_service.infrastructure.persistence.jpa.entity.FollowEntityId;
import com.twohands.social_service.infrastructure.persistence.jpa.entity.FollowStatusDb;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface JpaFollowRepository extends JpaRepository<FollowEntity, FollowEntityId> {

    @Query("select f.followeeId from FollowEntity f where f.followerId = :followerId and f.status = :status")
    List<UUID> findFolloweeIdsByFollowerIdAndStatus(UUID followerId, FollowStatusDb status);
}
