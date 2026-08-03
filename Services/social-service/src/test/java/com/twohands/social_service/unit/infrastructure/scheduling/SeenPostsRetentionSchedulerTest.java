package com.twohands.social_service.unit.infrastructure.scheduling;

import com.twohands.social_service.application.feed.seenposts.SeenPostsRetentionResolver;
import com.twohands.social_service.domain.post.UserSeenPostsRepository;
import com.twohands.social_service.infrastructure.scheduling.SeenPostsRetentionScheduler;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SeenPostsRetentionSchedulerTest {

    @Test
    void purgeUsesResolvedRetentionDays() {
        UserSeenPostsRepository repo = mock(UserSeenPostsRepository.class);
        SeenPostsRetentionResolver resolver = mock(SeenPostsRetentionResolver.class);
        when(resolver.resolveRetentionDays()).thenReturn(7);
        when(repo.deleteSeenBefore(any())).thenReturn(3);

        SeenPostsRetentionScheduler scheduler = new SeenPostsRetentionScheduler(repo, resolver);
        Instant before = Instant.now();
        scheduler.purgeExpiredSeenPosts();

        ArgumentCaptor<Instant> cutoffCaptor = ArgumentCaptor.forClass(Instant.class);
        verify(repo).deleteSeenBefore(cutoffCaptor.capture());
        Instant cutoff = cutoffCaptor.getValue();
        Instant expectedMin = before.minus(7, ChronoUnit.DAYS).minusSeconds(5);
        Instant expectedMax = Instant.now().minus(7, ChronoUnit.DAYS).plusSeconds(5);
        assertTrue(!cutoff.isBefore(expectedMin) && !cutoff.isAfter(expectedMax));
    }
}
