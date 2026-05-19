package com.twohands.social_service.infrastructure.persistence.adapter;

import com.twohands.social_service.domain.post.FeedQuery;
import com.twohands.social_service.domain.post.MediaItem;
import com.twohands.social_service.domain.post.PageResult;
import com.twohands.social_service.domain.post.Post;
import com.twohands.social_service.domain.post.PostRepository;
import com.twohands.social_service.domain.post.PostStatus;
import com.twohands.social_service.domain.post.PostVisibility;
import com.twohands.social_service.domain.post.ProductTag;
import com.twohands.social_service.infrastructure.persistence.mongo.document.PostDocument;
import com.twohands.social_service.infrastructure.persistence.mongo.repository.MongoPostRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
public class PostRepositoryAdapter implements PostRepository {

    private final MongoPostRepository mongoPostRepository;

    public PostRepositoryAdapter(MongoPostRepository mongoPostRepository) {
        this.mongoPostRepository = mongoPostRepository;
    }

    @Override
    public Post save(Post post) {
        PostDocument document = toDocument(post);
        PostDocument saved = mongoPostRepository.save(document);
        return toDomain(saved);
    }

    @Override
    public Optional<Post> findById(String postId) {
        return mongoPostRepository.findById(postId).map(this::toDomain);
    }

    @Override
    public void incrementReplyCount(String postId) {
        mongoPostRepository.findById(postId).ifPresent(document -> {
            document.setReplyCount(document.getReplyCount() + 1);
            mongoPostRepository.save(document);
        });
    }

    @Override
    public void decrementReplyCount(String postId) {
        mongoPostRepository.findById(postId).ifPresent(document -> {
            document.setReplyCount(Math.max(0, document.getReplyCount() - 1));
            mongoPostRepository.save(document);
        });
    }

    @Override
    public void incrementLikeCount(String postId) {
        mongoPostRepository.findById(postId).ifPresent(document -> {
            document.setLikeCount(document.getLikeCount() + 1);
            mongoPostRepository.save(document);
        });
    }

    @Override
    public void decrementLikeCount(String postId) {
        mongoPostRepository.findById(postId).ifPresent(document -> {
            document.setLikeCount(Math.max(0, document.getLikeCount() - 1));
            mongoPostRepository.save(document);
        });
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

    private PostDocument toDocument(Post post) {
        PostDocument doc = new PostDocument();
        if (post.id() != null) {
            doc.setId(post.id());
        }
        doc.setAuthorId(post.authorId());
        doc.setCaption(post.caption());
        doc.setMedia(post.media().stream().map(m -> new PostDocument.MediaDocument(m.url(), m.type())).toList());
        doc.setProductTags(post.productTags().stream()
                .map(pt -> new PostDocument.ProductTagDocument(pt.productId(), pt.price()))
                .toList());
        doc.setStatus(post.status().name());
        doc.setVisibility(post.visibility().name());
        doc.setLikeCount(post.likeCount());
        doc.setReplyCount(post.replyCount());
        doc.setHashtags(post.hashtags());
        doc.setAllowComments(post.allowComments());
        doc.setCreatedAt(post.createdAt());
        doc.setUpdatedAt(post.updatedAt());
        doc.setDeletedAt(post.deletedAt());
        return doc;
    }

    private Post toDomain(PostDocument postDocument) {
        List<MediaItem> media = postDocument.getMedia().stream()
                .map(this::toMedia)
                .toList();
        List<ProductTag> productTags = postDocument.getProductTags() != null
                ? postDocument.getProductTags().stream().map(this::toProductTag).toList()
                : List.of();
        return new Post(
                postDocument.getId(),
                postDocument.getAuthorId(),
                postDocument.getCaption(),
                media,
                productTags,
                PostStatus.valueOf(postDocument.getStatus()),
                PostVisibility.valueOf(postDocument.getVisibility()),
                postDocument.getLikeCount(),
                postDocument.getReplyCount(),
                postDocument.getHashtags(),
                postDocument.isAllowComments(),
                postDocument.getCreatedAt(),
                postDocument.getUpdatedAt(),
                postDocument.getDeletedAt()
        );
    }

    private MediaItem toMedia(PostDocument.MediaDocument mediaDocument) {
        return new MediaItem(mediaDocument.getUrl(), mediaDocument.getType());
    }

    private ProductTag toProductTag(PostDocument.ProductTagDocument pt) {
        return new ProductTag(pt.getProductId(), pt.getPrice());
    }
}
