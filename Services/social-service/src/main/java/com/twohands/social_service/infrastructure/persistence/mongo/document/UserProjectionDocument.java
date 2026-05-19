package com.twohands.social_service.infrastructure.persistence.mongo.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "user_projections")
public class UserProjectionDocument {

    @Id
    private String id;

    @Field("user_id")
    private String userId;

    @Field("status")
    private String status;

    @Field("display_name")
    private String displayName;

    @Field("avatar_url")
    private String avatarUrl;

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getStatus() {
        return status;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }
}
