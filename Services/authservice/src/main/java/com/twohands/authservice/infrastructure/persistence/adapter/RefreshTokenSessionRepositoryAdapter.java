package com.twohands.authservice.infrastructure.persistence.adapter;

import com.twohands.authservice.domain.session.RefreshTokenSession;
import com.twohands.authservice.domain.session.RefreshTokenSessionRepository;
import com.twohands.authservice.domain.session.RefreshTokenStatus;
import com.twohands.authservice.infrastructure.persistence.entity.RefreshTokenSessionEntity;
import com.twohands.authservice.infrastructure.persistence.entity.UserEntity;
import com.twohands.authservice.infrastructure.persistence.mapper.RefreshTokenSessionMapper;
import com.twohands.authservice.infrastructure.persistence.repository.RefreshTokenSessionJpaRepository;
import com.twohands.authservice.infrastructure.persistence.repository.UserJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class RefreshTokenSessionRepositoryAdapter implements RefreshTokenSessionRepository {

    private final RefreshTokenSessionJpaRepository jpaRepository;
    private final UserJpaRepository userJpaRepository;
    private final RefreshTokenSessionMapper mapper;

    public RefreshTokenSessionRepositoryAdapter(RefreshTokenSessionJpaRepository jpaRepository,
                                                UserJpaRepository userJpaRepository,
                                                RefreshTokenSessionMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.userJpaRepository = userJpaRepository;
        this.mapper = mapper;
    }

    @Override
    public RefreshTokenSession save(RefreshTokenSession session) {
        RefreshTokenSessionEntity entity = mapper.toEntity(session);

        // Replace the mapper's stub UserEntity with a managed Hibernate proxy
        // to satisfy the @ManyToOne FK constraint on persist
        UserEntity userRef = userJpaRepository.getReferenceById(session.getUserId());
        entity.setUser(userRef);

        RefreshTokenSessionEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<RefreshTokenSession> findByTokenHash(String tokenHash) {
        return jpaRepository.findByTokenHash(tokenHash).map(mapper::toDomain);
    }

    @Override
    public Optional<RefreshTokenSession> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<RefreshTokenSession> findByUserId(UUID userId) {
        return jpaRepository.findByUser_IdOrderByCreatedAtDesc(userId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<RefreshTokenSession> findByIdAndUserId(UUID sessionId, UUID userId) {
        return jpaRepository.findByIdAndUser_Id(sessionId, userId).map(mapper::toDomain);
    }

    @Override
    @Transactional
    public void revokeAllActiveByUserId(UUID userId) {
        jpaRepository.revokeAllActiveByUserId(userId, RefreshTokenStatus.REVOKED);
    }
}
