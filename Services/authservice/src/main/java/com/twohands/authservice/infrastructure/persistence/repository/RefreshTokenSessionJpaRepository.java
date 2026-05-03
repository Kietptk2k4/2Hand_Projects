package com.twohands.authservice.infrastructure.persistence.repository;

import com.twohands.authservice.domain.session.RefreshTokenStatus;
import com.twohands.authservice.infrastructure.persistence.entity.RefreshTokenSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenSessionJpaRepository extends JpaRepository<RefreshTokenSessionEntity, UUID> {

    Optional<RefreshTokenSessionEntity> findByTokenHash(String tokenHash);

    List<RefreshTokenSessionEntity> findByUser_IdOrderByCreatedAtDesc(UUID userId);

    Optional<RefreshTokenSessionEntity> findByIdAndUser_Id(UUID sessionId, UUID userId);

    @Modifying
    @Query("UPDATE RefreshTokenSessionEntity s SET s.revoked = true, s.status = :status WHERE s.user.id = :userId AND s.revoked = false")
    void revokeAllActiveByUserId(@Param("userId") UUID userId, @Param("status") RefreshTokenStatus status);
}
