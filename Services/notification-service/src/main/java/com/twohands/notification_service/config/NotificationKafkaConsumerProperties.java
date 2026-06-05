package com.twohands.notification_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "notification.kafka.consumer")
public class NotificationKafkaConsumerProperties {

    private boolean enabled = false;
    private String bootstrapServers = "localhost:9092";
    private String groupId = "notification-domain-events";
    private List<String> topics = List.of(
            "auth.user.created",
            "auth.user.updated",
            "auth.user.deleted",
            "auth.email.verification_requested",
            "auth.password.reset_requested",
            "social.post.liked",
            "social.comment.created",
            "social.comment.replied",
            "social.comment.liked",
            "social.user.followed",
            "commerce.order.created",
            "commerce.payment.paid",
            "commerce.payment.failed",
            "commerce.shipment.created",
            "commerce.shipment.shipped",
            "commerce.shipment.delivered",
            "commerce.order.completed",
            "commerce.review.reminder",
            "admin.user.suspended",
            "admin.user.banned",
            "admin.user.restricted",
            "admin.product.removed",
            "admin.review.hidden",
            "admin.shop.suspended",
            "admin.shop.closed",
            "admin.announcement.published",
            "admin.announcement.cancelled",
            "admin.user.enforcement_revoked",
            "admin.user.enforcement_expired"
    );

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
}
