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
