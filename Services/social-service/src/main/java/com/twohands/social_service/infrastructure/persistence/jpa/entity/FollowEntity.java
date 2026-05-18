package com.twohands.social_service.infrastructure.persistence.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "follows")
@IdClass(FollowEntityId.class)
public class FollowEntity {

    @Id
    @Column(name = "follower_id", nullable = false)
    private UUID followerId;

    @Id
    @Column(name = "followee_id", nullable = false)
    private UUID followeeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private FollowStatusDb status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public UUID getFollowerId() {
        return followerId;
    }

    public UUID getFolloweeId() {
        return followeeId;
    }

    public FollowStatusDb getStatus() {
        return status;
    }
}
