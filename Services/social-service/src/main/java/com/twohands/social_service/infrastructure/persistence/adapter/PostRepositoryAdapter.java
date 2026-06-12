package com.twohands.social_service.infrastructure.persistence.adapter;

import com.twohands.social_service.domain.post.AuthorPostsQuery;
import com.twohands.social_service.domain.post.AuthorPostsScope;
import com.twohands.social_service.domain.post.FeedQuery;
import com.twohands.social_service.domain.post.MediaItem;
import com.twohands.social_service.domain.post.PageResult;
import com.twohands.social_service.domain.post.Post;
import com.twohands.social_service.domain.post.PostRepository;
import com.twohands.social_service.domain.post.PostHashtagSearchQuery;
import com.twohands.social_service.domain.post.PostSearchQuery;
import com.twohands.social_service.domain.post.PostModerationStatus;
import com.twohands.social_service.domain.post.PostStatus;
import com.twohands.social_service.domain.post.PostVisibility;
import com.twohands.social_service.domain.post.ProductTag;
import com.twohands.social_service.infrastructure.persistence.mongo.document.PostDocument;
import com.twohands.social_service.infrastructure.persistence.mongo.repository.MongoPostRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Component
public class PostRepositoryAdapter implements PostRepository {

    private final MongoPostRepository mongoPostRepository;
    private final MongoTemplate mongoTemplate;

    public PostRepositoryAdapter(MongoPostRepository mongoPostRepository, MongoTemplate mongoTemplate) {
        this.mongoPostRepository = mongoPostRepository;
        this.mongoTemplate = mongoTemplate;
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
    public List<Post> findByIds(Collection<String> postIds) {
        if (postIds == null || postIds.isEmpty()) {
            return List.of();
        }
        Map<String, Post> byId = new HashMap<>();
        for (PostDocument document : mongoPostRepository.findByIdIn(postIds)) {
            byId.put(document.getId(), toDomain(document));
        }
        List<Post> ordered = new ArrayList<>();
        for (String postId : postIds) {
            Post post = byId.get(postId);
            if (post != null) {
                ordered.add(post);
            }
        }
        return ordered;
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

    @Override
    public PageResult<Post> searchPosts(PostSearchQuery query, List<String> acceptedFolloweeAuthorIds) {
        PageRequest pageRequest = PageRequest.of(query.page(), query.size());
        List<String> followeeIds = acceptedFolloweeAuthorIds != null ? acceptedFolloweeAuthorIds : List.of();
        Page<PostDocument> page = mongoPostRepository.searchActivePostsByKeyword(
                query.keyword(),
                followeeIds,
                PostStatus.ACTIVE.name(),
                PostVisibility.PUBLIC.name(),
                PostVisibility.FOLLOWERS.name(),
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
    public PageResult<Post> searchPostsByHashtag(PostHashtagSearchQuery query, List<String> acceptedFolloweeAuthorIds) {
        PageRequest pageRequest = PageRequest.of(query.page(), query.size());
        List<String> followeeIds = acceptedFolloweeAuthorIds != null ? acceptedFolloweeAuthorIds : List.of();
        Page<PostDocument> page = mongoPostRepository.searchActivePostsByHashtag(
                query.hashtagVariants(),
                followeeIds,
                PostStatus.ACTIVE.name(),
                PostVisibility.PUBLIC.name(),
                PostVisibility.FOLLOWERS.name(),
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
    public PageResult<Post> findAuthorPosts(AuthorPostsQuery query) {
        PageRequest pageRequest = PageRequest.of(query.page(), query.size());
        Page<PostDocument> page = switch (query.scope()) {
            case OWNER_PUBLISHED -> mongoPostRepository.findByAuthorIdAndStatusOrderByCreatedAtDesc(
                    query.authorId(),
                    PostStatus.ACTIVE.name(),
                    pageRequest
            );
            case OWNER_ALL -> mongoPostRepository.findByAuthorIdAndStatusInOrderByCreatedAtDesc(
                    query.authorId(),
                    List.of(PostStatus.ACTIVE.name(), PostStatus.DRAFT.name()),
                    pageRequest
            );
            case VIEWER_PUBLIC_ONLY -> mongoPostRepository.findByAuthorIdAndStatusAndVisibilityOrderByCreatedAtDesc(
                    query.authorId(),
                    PostStatus.ACTIVE.name(),
                    PostVisibility.PUBLIC.name(),
                    pageRequest
            );
            case VIEWER_AS_FOLLOWER -> mongoPostRepository.findByAuthorIdAndStatusAndVisibilityInOrderByCreatedAtDesc(
                    query.authorId(),
                    PostStatus.ACTIVE.name(),
                    PostVisibility.PUBLIC.name(),
                    PostVisibility.FOLLOWERS.name(),
                    pageRequest
            );
        };

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

    private PostDocument toDocument(Post post) {
        PostDocument doc = new PostDocument();
        if (post.id() != null) {
            doc.setId(post.id());
        }
        doc.setAuthorId(post.authorId());
        doc.setCaption(post.caption());
        doc.setMedia(post.media().stream()
                .map(m -> new PostDocument.MediaDocument(m.url(), m.type(), m.width(), m.height()))
                .toList());
        doc.setProductTags(post.productTags().stream()
                .map(this::toProductTagDocument)
                .toList());
        doc.setStatus(post.status().name());
        doc.setVisibility(post.visibility().name());
        doc.setLikeCount(post.likeCount());
        doc.setReplyCount(post.replyCount());
        doc.setHashtags(post.hashtags());
        doc.setAllowComments(post.allowComments());
        doc.setModerationStatus(post.moderationStatusOrDefault().name());
        doc.setModerationReason(post.moderationReason());
        doc.setLastModerationLogId(post.lastModerationLogId());
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
                resolveModerationStatus(postDocument),
                postDocument.getModerationReason(),
                postDocument.getLastModerationLogId(),
                postDocument.getCreatedAt(),
                postDocument.getUpdatedAt(),
                postDocument.getDeletedAt()
        );
    }

    private PostModerationStatus resolveModerationStatus(PostDocument postDocument) {
        if (postDocument.getModerationStatus() == null || postDocument.getModerationStatus().isBlank()) {
            return PostModerationStatus.NONE;
        }
        return PostModerationStatus.valueOf(postDocument.getModerationStatus());
    }

    private MediaItem toMedia(PostDocument.MediaDocument mediaDocument) {
        return new MediaItem(
                mediaDocument.getUrl(),
                mediaDocument.getType(),
                mediaDocument.getWidth(),
                mediaDocument.getHeight()
        );
    }

    @Override
    public long markProductTagsUnavailable(String productId) {
        if (productId == null || productId.isBlank()) {
            return 0;
        }

        Query query = Query.query(Criteria.where("product_tags.product_id").is(productId));
        Update update = new Update().set("product_tags.$[tag].available", false);
        update.filterArray(Criteria.where("tag.product_id").is(productId));

        return mongoTemplate.updateMulti(query, update, PostDocument.class).getModifiedCount();
    }

    @Override
    public long markProductTagsAvailable(String productId) {
        if (productId == null || productId.isBlank()) {
            return 0;
        }

        Query query = Query.query(Criteria.where("product_tags.product_id").is(productId));
        Update update = new Update().set("product_tags.$[tag].available", true);
        update.filterArray(Criteria.where("tag.product_id").is(productId));

        return mongoTemplate.updateMulti(query, update, PostDocument.class).getModifiedCount();
    }

    private PostDocument.ProductTagDocument toProductTagDocument(ProductTag tag) {
        PostDocument.ProductTagDocument document = new PostDocument.ProductTagDocument(tag.productId(), tag.price());
        document.setName(tag.name());
        document.setImageUrl(tag.imageUrl());
        document.setCategory(tag.category());
        document.setAvailable(tag.available());
        return document;
    }

    private ProductTag toProductTag(PostDocument.ProductTagDocument pt) {
        return new ProductTag(
                pt.getProductId(),
                pt.getPrice(),
                pt.getName(),
                pt.getImageUrl(),
                pt.getCategory(),
                pt.getAvailable()
        );
    }
}
