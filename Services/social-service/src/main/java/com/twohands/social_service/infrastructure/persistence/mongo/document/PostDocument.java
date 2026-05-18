package com.twohands.social_service.infrastructure.persistence.mongo.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

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

    @Field("created_at")
    private Instant createdAt;

    @Field("updated_at")
    private Instant updatedAt;

    public String getId() {
        return id;
    }

    public String getAuthorId() {
        return authorId;
    }

    public String getCaption() {
        return caption;
    }

    public List<MediaDocument> getMedia() {
        return media;
    }

    public String getStatus() {
        return status;
    }

    public String getVisibility() {
        return visibility;
    }

    public long getLikeCount() {
        return likeCount;
    }

    public long getReplyCount() {
        return replyCount;
    }

    public List<String> getHashtags() {
        return hashtags;
    }

    public boolean isAllowComments() {
        return allowComments;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public static class MediaDocument {
        @Field("url")
        private String url;
        @Field("type")
        private String type;

        public String getUrl() {
            return url;
        }

        public String getType() {
            return type;
        }
    }
}
