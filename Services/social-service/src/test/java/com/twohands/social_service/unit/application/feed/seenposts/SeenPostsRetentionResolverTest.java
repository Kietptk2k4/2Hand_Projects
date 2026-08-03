package com.twohands.social_service.unit.application.feed.seenposts;

import com.twohands.social_service.application.feed.seenposts.SeenPostsRetentionResolver;
import com.twohands.social_service.domain.integration.AdminSystemConfigClient;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SeenPostsRetentionResolverTest {

    @Test
    void usesAdminValueWhenPresent() {
        AdminSystemConfigClient client = mock(AdminSystemConfigClient.class);
        when(client.findActiveConfigValue(SeenPostsRetentionResolver.CONFIG_KEY))
                .thenReturn(Optional.of("14"));
        SeenPostsRetentionResolver resolver = new SeenPostsRetentionResolver(client, 7);
        assertEquals(14, resolver.resolveRetentionDays());
    }

    @Test
    void fallsBackWhenAdminMissing() {
        AdminSystemConfigClient client = mock(AdminSystemConfigClient.class);
        when(client.findActiveConfigValue(SeenPostsRetentionResolver.CONFIG_KEY))
                .thenReturn(Optional.empty());
        SeenPostsRetentionResolver resolver = new SeenPostsRetentionResolver(client, 7);
        assertEquals(7, resolver.resolveRetentionDays());
    }

    @Test
    void fallsBackWhenAdminValueInvalid() {
        AdminSystemConfigClient client = mock(AdminSystemConfigClient.class);
        when(client.findActiveConfigValue(SeenPostsRetentionResolver.CONFIG_KEY))
                .thenReturn(Optional.of("0"));
        SeenPostsRetentionResolver resolver = new SeenPostsRetentionResolver(client, 7);
        assertEquals(7, resolver.resolveRetentionDays());
    }
}
