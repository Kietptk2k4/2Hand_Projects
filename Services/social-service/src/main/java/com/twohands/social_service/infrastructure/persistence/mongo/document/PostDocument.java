package com.twohands.social_service.infrastructure.persistence.mongo.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "posts")
public class PostDocument {

    @Id
    private String id;

    @Field("author_id")
    private String authorId;

    @Field("caption")
    private String caption;

    @Field("media")
    private List<MediaDocument> media = new ArrayList<>();

    @Field("product_tags")
    private List<ProductTagDocument> productTags = new ArrayList<>();

    @Field("status")
    private String status;

    @Field("visibility")
    private String visibility;

    @Field("like_count")
    private long likeCount;

    @Field("reply_count")
    private long replyCount;

    @Field("hashtags")
    private List<String> hashtags = new ArrayList<>();

    @Field("allow_comments")
    private boolean allowComments;

    @Field("moderation_status")
    private String moderationStatus;

    @Field("moderation_reason")
    private String moderationReason;

    @Field("last_moderation_log_id")
    private String lastModerationLogId;

    @Field("created_at")
    private Instant createdAt;

    @Field("updated_at")
    private Instant updatedAt;

    @Field("deleted_at")
    private Instant deletedAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public List<MediaDocument> getMedia() {
        return media;
    }

    public void setMedia(List<MediaDocument> media) {
        this.media = media;
    }

    public List<ProductTagDocument> getProductTags() {
        return productTags;
    }

    public void setProductTags(List<ProductTagDocument> productTags) {
        this.productTags = productTags;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getVisibility() {
        return visibility;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    public long getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(long likeCount) {
        this.likeCount = likeCount;
    }

    public long getReplyCount() {
        return replyCount;
    }

    public void setReplyCount(long replyCount) {
        this.replyCount = replyCount;
    }

    public List<String> getHashtags() {
        return hashtags;
    }

    public void setHashtags(List<String> hashtags) {
        this.hashtags = hashtags;
    }

    public boolean isAllowComments() {
        return allowComments;
    }

    public void setAllowComments(boolean allowComments) {
        this.allowComments = allowComments;
    }

    public String getModerationStatus() {
        return moderationStatus;
    }

    public void setModerationStatus(String moderationStatus) {
        this.moderationStatus = moderationStatus;
    }

    public String getModerationReason() {
        return moderationReason;
    }

    public void setModerationReason(String moderationReason) {
        this.moderationReason = moderationReason;
    }

    public String getLastModerationLogId() {
        return lastModerationLogId;
    }

    public void setLastModerationLogId(String lastModerationLogId) {
        this.lastModerationLogId = lastModerationLogId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(Instant deletedAt) {
        this.deletedAt = deletedAt;
    }

    public static class MediaDocument {
        @Field("url")
        private String url;

        @Field("type")
        private String type;

        public MediaDocument() {
        }

        public MediaDocument(String url, String type) {
            this.url = url;
            this.type = type;
        }

        public String getUrl() {
            return url;
        }

        public String getType() {
            return type;
        }
    }

    public static class ProductTagDocument {
        @Field("product_id")
        private String productId;

        @Field("price")
        private BigDecimal price;

        public ProductTagDocument() {
        }

        public ProductTagDocument(String productId, BigDecimal price) {
            this.productId = productId;
            this.price = price;
        }

        public String getProductId() {
            return productId;
        }

        public BigDecimal getPrice() {
            return price;
        }
    }
}
