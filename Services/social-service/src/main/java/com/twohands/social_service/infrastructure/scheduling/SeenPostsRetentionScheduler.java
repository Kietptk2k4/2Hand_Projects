package com.twohands.social_service.infrastructure.scheduling;

import com.twohands.social_service.application.feed.seenposts.SeenPostsRetentionResolver;
import com.twohands.social_service.domain.post.UserSeenPostsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
public class SeenPostsRetentionScheduler {

    private static final Logger log = LoggerFactory.getLogger(SeenPostsRetentionScheduler.class);

    private final UserSeenPostsRepository userSeenPostsRepository;
    private final SeenPostsRetentionResolver retentionResolver;

    public SeenPostsRetentionScheduler(
            UserSeenPostsRepository userSeenPostsRepository,
            SeenPostsRetentionResolver retentionResolver
    ) {
        this.userSeenPostsRepository = userSeenPostsRepository;
        this.retentionResolver = retentionResolver;
    }

    @Scheduled(cron = "${social.recommendation.seen-posts-cleanup-cron:0 15 3 * * *}")
    public void purgeExpiredSeenPosts() {
        int retentionDays = retentionResolver.resolveRetentionDays();
        Instant cutoff = Instant.now().minus(retentionDays, ChronoUnit.DAYS);
        int deleted = userSeenPostsRepository.deleteSeenBefore(cutoff);
        log.info(
                "user_seen_posts TTL cleanup finished. deleted={}, retentionDays={}, cutoff={}",
                deleted,
                retentionDays,
                cutoff
        );
    }
}
