package com.twohands.social_service.infrastructure.persistence.mongo.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "comments")
public class CommentDocument {

    @Id
    private String id;

    @Field("post_id")
    private String postId;

    @Field("author_id")
    private String authorId;

    @Field("parent_comment_id")
    private String parentCommentId;

    @Field("content_text")
    private String contentText;

    @Field("media")
    private List<MediaDocument> media = new ArrayList<>();

    @Field("status")
    private String status;

    @Field("moderation_status")
    private String moderationStatus;

    @Field("moderation_reason")
    private String moderationReason;

    @Field("last_moderation_log_id")
    private String lastModerationLogId;

    @Field("like_count")
    private long likeCount;

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

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public String getParentCommentId() {
        return parentCommentId;
    }

    public void setParentCommentId(String parentCommentId) {
        this.parentCommentId = parentCommentId;
    }

    public String getContentText() {
        return contentText;
    }

    public void setContentText(String contentText) {
        this.contentText = contentText;
    }

    public List<MediaDocument> getMedia() {
        return media;
    }

    public void setMedia(List<MediaDocument> media) {
        this.media = media;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public long getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(long likeCount) {
        this.likeCount = likeCount;
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
}
