package com.twohands.social_service.infrastructure.persistence.adapter;

import com.twohands.social_service.domain.follow.FollowRepository;
import com.twohands.social_service.infrastructure.persistence.jpa.entity.FollowStatusDb;
import com.twohands.social_service.infrastructure.persistence.jpa.repository.JpaFollowRepository;
import org.springframework.stereotype.Component;

import java.util.List;
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
}
