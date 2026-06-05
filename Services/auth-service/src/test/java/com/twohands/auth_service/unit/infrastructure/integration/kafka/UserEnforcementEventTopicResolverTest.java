package com.twohands.auth_service.unit.infrastructure.integration.kafka;

import com.twohands.auth_service.infrastructure.integration.kafka.UserEnforcementEventTopicResolver;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserEnforcementEventTopicResolverTest {

    private final UserEnforcementEventTopicResolver resolver = new UserEnforcementEventTopicResolver();

    @Test
    void shouldResolveAllAdminEnforcementTopics() {
        assertThat(resolver.resolveEventType("admin.user.suspended")).isEqualTo("USER_SUSPENDED");
        assertThat(resolver.resolveEventType("admin.user.banned")).isEqualTo("USER_BANNED");
        assertThat(resolver.resolveEventType("admin.user.restricted")).isEqualTo("USER_RESTRICTED");
        assertThat(resolver.resolveEventType("admin.user.enforcement_revoked"))
                .isEqualTo("USER_ENFORCEMENT_REVOKED");
        assertThat(resolver.resolveEventType("admin.user.enforcement_expired"))
                .isEqualTo("USER_ENFORCEMENT_EXPIRED");
        assertThat(resolver.resolveEventType("admin.product.removed")).isNull();
    }
}
