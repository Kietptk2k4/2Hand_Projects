package com.twohands.social_service.application.feed.seenposts;

import com.twohands.social_service.domain.integration.AdminSystemConfigClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SeenPostsRetentionResolver {

    public static final String CONFIG_KEY = "social.feed.seen_posts_retention_days";

    private static final Logger log = LoggerFactory.getLogger(SeenPostsRetentionResolver.class);

    private final AdminSystemConfigClient adminSystemConfigClient;
    private final int fallbackRetentionDays;

    public SeenPostsRetentionResolver(
            AdminSystemConfigClient adminSystemConfigClient,
            @Value("${social.recommendation.seen-posts-retention-days:7}") int fallbackRetentionDays
    ) {
        this.adminSystemConfigClient = adminSystemConfigClient;
        this.fallbackRetentionDays = Math.max(1, fallbackRetentionDays);
    }

    /**
     * Resolves retention days from Admin system-configs, falling back to local property.
     */
    public int resolveRetentionDays() {
        return adminSystemConfigClient.findActiveConfigValue(CONFIG_KEY)
                .map(this::parseDays)
                .orElseGet(() -> {
                    log.warn(
                            "Admin config {} unavailable; using fallback retentionDays={}",
                            CONFIG_KEY,
                            fallbackRetentionDays
                    );
                    return fallbackRetentionDays;
                });
    }

    private int parseDays(String raw) {
        try {
            int days = Integer.parseInt(raw.trim());
            if (days < 1) {
                log.warn("Invalid retention days {} for {}; using fallback {}", raw, CONFIG_KEY, fallbackRetentionDays);
                return fallbackRetentionDays;
            }
            return days;
        } catch (NumberFormatException ex) {
            log.warn("Non-integer retention days {} for {}; using fallback {}", raw, CONFIG_KEY, fallbackRetentionDays);
            return fallbackRetentionDays;
        }
    }
}
