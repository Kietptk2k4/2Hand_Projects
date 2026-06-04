package com.twohands.social_service.infrastructure.persistence.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status", nullable = false, columnDefinition = "follow_status")
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setFollowerId(UUID followerId) {
        this.followerId = followerId;
    }

    public void setFolloweeId(UUID followeeId) {
        this.followeeId = followeeId;
    }

    public void setStatus(FollowStatusDb status) {
        this.status = status;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
