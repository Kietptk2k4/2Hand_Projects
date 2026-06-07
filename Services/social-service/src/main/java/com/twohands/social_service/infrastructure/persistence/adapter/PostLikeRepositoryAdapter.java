package com.twohands.social_service.infrastructure.persistence.adapter;

import com.twohands.social_service.domain.post.PageResult;
import com.twohands.social_service.domain.post.PostLikeEntry;
import com.twohands.social_service.domain.post.PostLikeRepository;
import com.twohands.social_service.infrastructure.persistence.jpa.entity.PostLikeEntity;
import com.twohands.social_service.infrastructure.persistence.jpa.repository.JpaPostLikeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Set;
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
    public PageResult<PostLikeEntry> findLikersByPostId(String postId, int page, int size) {
        Page<PostLikeEntity> result = jpaPostLikeRepository.findByPostIdOrderByCreatedAtDesc(
                postId,
                PageRequest.of(page, size)
        );
        List<PostLikeEntry> items = result.getContent().stream()
                .map(entity -> new PostLikeEntry(entity.getUserId(), entity.getCreatedAt()))
                .toList();
        return new PageResult<>(
                items,
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.hasNext()
        );
    }

    @Override
    public Set<String> findLikedPostIdsByUserIdAndPostIds(UUID userId, Collection<String> postIds) {
        if (postIds == null || postIds.isEmpty()) {
            return Set.of();
        }
        return jpaPostLikeRepository.findPostIdsByUserIdAndPostIdIn(userId, postIds);
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
