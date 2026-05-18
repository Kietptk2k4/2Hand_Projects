package com.twohands.social_service.infrastructure.persistence.adapter;

import com.twohands.social_service.domain.post.FeedQuery;
import com.twohands.social_service.domain.post.MediaItem;
import com.twohands.social_service.domain.post.PageResult;
import com.twohands.social_service.domain.post.Post;
import com.twohands.social_service.domain.post.PostRepository;
import com.twohands.social_service.domain.post.PostStatus;
import com.twohands.social_service.domain.post.PostVisibility;
import com.twohands.social_service.infrastructure.persistence.mongo.document.PostDocument;
import com.twohands.social_service.infrastructure.persistence.mongo.repository.MongoPostRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
public class PostRepositoryAdapter implements PostRepository {

    private final MongoPostRepository mongoPostRepository;

    public PostRepositoryAdapter(MongoPostRepository mongoPostRepository) {
        this.mongoPostRepository = mongoPostRepository;
    }

    @Override
    public PageResult<Post> findGlobalFeed(FeedQuery query) {
        PageRequest pageRequest = PageRequest.of(query.page(), query.size());
        Page<PostDocument> page = mongoPostRepository.findByStatusAndVisibilityOrderByCreatedAtDesc(
                PostStatus.ACTIVE.name(),
                PostVisibility.PUBLIC.name(),
                pageRequest
        );

        List<Post> items = page.getContent().stream()
                .map(this::toDomain)
                .toList();

        return new PageResult<>(
                items,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext()
        );
    }

    @Override
    public PageResult<Post> findFollowingFeed(FeedQuery query, List<String> followeeIds) {
        if (followeeIds == null || followeeIds.isEmpty()) {
            return new PageResult<>(List.of(), query.page(), query.size(), 0, 0, false);
        }
        PageRequest pageRequest = PageRequest.of(query.page(), query.size());
        Page<PostDocument> page = mongoPostRepository.findByStatusAndAuthorIdInAndVisibilityInOrderByCreatedAtDesc(
                PostStatus.ACTIVE.name(),
                followeeIds,
                List.of(PostVisibility.PUBLIC.name(), PostVisibility.FOLLOWERS.name()),
                pageRequest
        );

        List<Post> items = page.getContent().stream()
                .map(this::toDomain)
                .filter(Objects::nonNull)
                .toList();

        return new PageResult<>(
                items,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext()
        );
    }

    private Post toDomain(PostDocument postDocument) {
        List<MediaItem> media = postDocument.getMedia().stream()
                .map(this::toMedia)
                .toList();
        return new Post(
                postDocument.getId(),
                postDocument.getAuthorId(),
                postDocument.getCaption(),
                media,
                PostStatus.valueOf(postDocument.getStatus()),
                PostVisibility.valueOf(postDocument.getVisibility()),
                postDocument.getLikeCount(),
                postDocument.getReplyCount(),
                postDocument.getHashtags(),
                postDocument.isAllowComments(),
                postDocument.getCreatedAt(),
                postDocument.getUpdatedAt()
        );
    }

    private MediaItem toMedia(PostDocument.MediaDocument mediaDocument) {
        return new MediaItem(mediaDocument.getUrl(), mediaDocument.getType());
    }
}
