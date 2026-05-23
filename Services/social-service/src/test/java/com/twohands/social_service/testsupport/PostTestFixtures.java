package com.twohands.social_service.testsupport;

import com.twohands.social_service.domain.post.MediaItem;
import com.twohands.social_service.domain.post.Post;
import com.twohands.social_service.domain.post.PostModerationStatus;
import com.twohands.social_service.domain.post.PostStatus;
import com.twohands.social_service.domain.post.PostVisibility;
import com.twohands.social_service.domain.post.ProductTag;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class PostTestFixtures {

    private static final Instant DEFAULT_CREATED = Instant.parse("2026-05-18T10:15:30Z");
    private static final Instant DEFAULT_UPDATED = Instant.parse("2026-05-18T10:20:30Z");

    private PostTestFixtures() {
    }

    public static PostFixtureBuilder builder() {
        return new PostFixtureBuilder();
    }

    public static Post activePublic(String postId, String authorId) {
        return builder().id(postId).authorId(authorId).build();
    }

    public static final class PostFixtureBuilder {
        private String id = "507f1f77bcf86cd799439011";
        private String authorId = "550e8400-e29b-41d4-a716-446655440001";
        private String caption = "caption";
        private List<MediaItem> media = List.of(new MediaItem("https://cdn/1.jpg", "IMAGE"));
        private List<ProductTag> productTags = List.of(new ProductTag("product-1", new BigDecimal("199000")));
        private PostStatus status = PostStatus.ACTIVE;
        private PostVisibility visibility = PostVisibility.PUBLIC;
        private long likeCount = 10L;
        private long replyCount = 2L;
        private List<String> hashtags = List.of("tag");
        private boolean allowComments = true;
        private PostModerationStatus moderationStatus = PostModerationStatus.NONE;
        private String moderationReason = null;
        private String lastModerationLogId = null;
        private Instant createdAt = DEFAULT_CREATED;
        private Instant updatedAt = DEFAULT_UPDATED;
        private Instant deletedAt = null;

        public PostFixtureBuilder id(String id) {
            this.id = id;
            return this;
        }

        public PostFixtureBuilder authorId(String authorId) {
            this.authorId = authorId;
            return this;
        }

        public PostFixtureBuilder caption(String caption) {
            this.caption = caption;
            return this;
        }

        public PostFixtureBuilder media(List<MediaItem> media) {
            this.media = media;
            return this;
        }

        public PostFixtureBuilder productTags(List<ProductTag> productTags) {
            this.productTags = productTags;
            return this;
        }

        public PostFixtureBuilder status(PostStatus status) {
            this.status = status;
            return this;
        }

        public PostFixtureBuilder visibility(PostVisibility visibility) {
            this.visibility = visibility;
            return this;
        }

        public PostFixtureBuilder likeCount(long likeCount) {
            this.likeCount = likeCount;
            return this;
        }

        public PostFixtureBuilder replyCount(long replyCount) {
            this.replyCount = replyCount;
            return this;
        }

        public PostFixtureBuilder hashtags(List<String> hashtags) {
            this.hashtags = hashtags;
            return this;
        }

        public PostFixtureBuilder allowComments(boolean allowComments) {
            this.allowComments = allowComments;
            return this;
        }

        public PostFixtureBuilder moderationStatus(PostModerationStatus moderationStatus) {
            this.moderationStatus = moderationStatus;
            return this;
        }

        public PostFixtureBuilder moderationReason(String moderationReason) {
            this.moderationReason = moderationReason;
            return this;
        }

        public PostFixtureBuilder lastModerationLogId(String lastModerationLogId) {
            this.lastModerationLogId = lastModerationLogId;
            return this;
        }

        public PostFixtureBuilder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public PostFixtureBuilder updatedAt(Instant updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public PostFixtureBuilder deletedAt(Instant deletedAt) {
            this.deletedAt = deletedAt;
            return this;
        }

        public Post build() {
            return new Post(
                    id,
                    authorId,
                    caption,
                    media,
                    productTags,
                    status,
                    visibility,
                    likeCount,
                    replyCount,
                    hashtags,
                    allowComments,
                    moderationStatus,
                    moderationReason,
                    lastModerationLogId,
                    createdAt,
                    updatedAt,
                    deletedAt
            );
        }
    }
}
