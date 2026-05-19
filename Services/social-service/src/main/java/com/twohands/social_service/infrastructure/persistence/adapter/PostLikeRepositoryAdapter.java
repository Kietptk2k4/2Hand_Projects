package com.twohands.social_service.infrastructure.persistence.adapter;

import com.twohands.social_service.domain.post.PostLikeRepository;
import com.twohands.social_service.infrastructure.persistence.jpa.entity.PostLikeEntity;
import com.twohands.social_service.infrastructure.persistence.jpa.repository.JpaPostLikeRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class PostLikeRepositoryAdapter implements PostLikeRepository {

    private final JpaPostLikeRepository jpaPostLikeRepository;

    public PostLikeRepositoryAdapter(JpaPostLikeRepository jpaPostLikeRepository) {
        this.jpaPostLikeRepository = jpaPostLikeRepository;
    }

    @Override
    public boolean existsByPostIdAndUserId(String postId, UUID userId) {
        return jpaPostLikeRepository.existsByPostIdAndUserId(postId, userId);
    }

    @Override
    public void save(String postId, UUID userId) {
        PostLikeEntity entity = new PostLikeEntity();
        entity.setPostId(postId);
        entity.setUserId(userId);
        entity.setCreatedAt(Instant.now());
        jpaPostLikeRepository.save(entity);
    }

    @Override
    public void deleteByPostIdAndUserId(String postId, UUID userId) {
        jpaPostLikeRepository.deleteByPostIdAndUserId(postId, userId);
    }
}
