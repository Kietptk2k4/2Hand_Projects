package com.twohands.social_service.unit.infrastructure.outbox;

import com.twohands.social_service.exception.AppException;
import com.twohands.social_service.infrastructure.outbox.SocialOutboxTopicResolver;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SocialOutboxTopicResolverTest {

    private final SocialOutboxTopicResolver resolver = new SocialOutboxTopicResolver();

    @Test
    void shouldResolveMvpEventTopics() {
        assertThat(resolver.resolve("POST_CREATED")).isEqualTo("social.post.created");
        assertThat(resolver.resolve("POST_LIKED")).isEqualTo("social.post.liked");
        assertThat(resolver.resolve("COMMENT_LIKED")).isEqualTo("social.comment.liked");
        assertThat(resolver.resolve("COMMENT_CREATED")).isEqualTo("social.comment.created");
        assertThat(resolver.resolve("USER_FOLLOWED")).isEqualTo("social.user.followed");
        assertThat(resolver.resolve("USER_AVATAR_UPDATED")).isEqualTo("social.user.avatar_updated");
    }

    @Test
    void shouldRejectUnknownEventType() {
        assertThatThrownBy(() -> resolver.resolve("POST_UPDATED"))
                .isInstanceOf(AppException.class);
    }
}
