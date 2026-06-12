package com.twohands.social_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "social.kafka.consumer")
public class SocialKafkaConsumerProperties {

    private boolean enabled = false;
    private String bootstrapServers = "localhost:9092";
    private String groupId = "social-user-projection";
    private List<String> topics = List.of(
            "auth.user.created",
            "auth.user.updated",
            "auth.user.deleted",
            "admin.user.suspended",
            "admin.user.banned",
            "admin.user.restricted",
            "admin.user.enforcement_revoked",
            "admin.user.enforcement_expired"
    );
    private String postModeratedGroupId = "social-post-moderated";
    private List<String> postModeratedTopics = List.of(
            "admin.post.moderated",
            "admin.post.restored"
    );
    private String commentModeratedGroupId = "social-comment-moderated";
    private List<String> commentModeratedTopics = List.of("admin.comment.moderated");
    private String commerceProductRemovedGroupId = "social-commerce-product-removed";
    private List<String> commerceProductRemovedTopics = List.of("commerce.product.removed");

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getBootstrapServers() {
        return bootstrapServers;
    }

    public void setBootstrapServers(String bootstrapServers) {
        this.bootstrapServers = bootstrapServers;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public List<String> getTopics() {
        return topics;
    }

    public void setTopics(List<String> topics) {
        this.topics = topics;
    }

    public String getPostModeratedGroupId() {
        return postModeratedGroupId;
    }

    public void setPostModeratedGroupId(String postModeratedGroupId) {
        this.postModeratedGroupId = postModeratedGroupId;
    }

    public List<String> getPostModeratedTopics() {
        return postModeratedTopics;
    }

    public void setPostModeratedTopics(List<String> postModeratedTopics) {
        this.postModeratedTopics = postModeratedTopics;
    }

    public String getCommentModeratedGroupId() {
        return commentModeratedGroupId;
    }

    public void setCommentModeratedGroupId(String commentModeratedGroupId) {
        this.commentModeratedGroupId = commentModeratedGroupId;
    }

    public List<String> getCommentModeratedTopics() {
        return commentModeratedTopics;
    }

    public void setCommentModeratedTopics(List<String> commentModeratedTopics) {
        this.commentModeratedTopics = commentModeratedTopics;
    }

    public String getCommerceProductRemovedGroupId() {
        return commerceProductRemovedGroupId;
    }

    public void setCommerceProductRemovedGroupId(String commerceProductRemovedGroupId) {
        this.commerceProductRemovedGroupId = commerceProductRemovedGroupId;
    }

    public List<String> getCommerceProductRemovedTopics() {
        return commerceProductRemovedTopics;
    }

    public void setCommerceProductRemovedTopics(List<String> commerceProductRemovedTopics) {
        this.commerceProductRemovedTopics = commerceProductRemovedTopics;
    }
}
