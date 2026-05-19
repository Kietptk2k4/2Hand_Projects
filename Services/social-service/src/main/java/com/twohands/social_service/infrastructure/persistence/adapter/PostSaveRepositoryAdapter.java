package com.twohands.social_service.infrastructure.persistence.adapter;

import com.twohands.social_service.domain.post.PostSaveRepository;
import com.twohands.social_service.infrastructure.persistence.jpa.entity.PostSaveEntity;
import com.twohands.social_service.infrastructure.persistence.jpa.repository.JpaPostSaveRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class PostSaveRepositoryAdapter implements PostSaveRepository {

    private final JpaPostSaveRepository jpaPostSaveRepository;

    public PostSaveRepositoryAdapter(JpaPostSaveRepository jpaPostSaveRepository) {
        this.jpaPostSaveRepository = jpaPostSaveRepository;
    }

    @Override
    public boolean existsByPostIdAndUserId(String postId, UUID userId) {
        return jpaPostSaveRepository.existsByPostIdAndUserId(postId, userId);
    }

    @Override
    public void save(String postId, UUID userId) {
        PostSaveEntity entity = new PostSaveEntity();
        entity.setPostId(postId);
        entity.setUserId(userId);
        entity.setCreatedAt(Instant.now());
        jpaPostSaveRepository.save(entity);
    }

    @Override
    public void deleteByPostIdAndUserId(String postId, UUID userId) {
        jpaPostSaveRepository.deleteByPostIdAndUserId(postId, userId);
    }
}
