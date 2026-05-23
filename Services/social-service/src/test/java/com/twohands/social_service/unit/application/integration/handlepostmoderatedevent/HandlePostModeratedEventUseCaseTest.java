package com.twohands.social_service.unit.application.integration.handlepostmoderatedevent;

import com.twohands.social_service.application.integration.handlepostmoderatedevent.HandlePostModeratedEventCommand;
import com.twohands.social_service.application.integration.handlepostmoderatedevent.HandlePostModeratedEventResult;
import com.twohands.social_service.application.integration.handlepostmoderatedevent.HandlePostModeratedEventUseCase;
import com.twohands.social_service.domain.integration.ProcessedDomainEventRepository;
import com.twohands.social_service.domain.post.Post;
import com.twohands.social_service.domain.post.PostModerationAction;
import com.twohands.social_service.domain.post.PostModerationStatus;
import com.twohands.social_service.domain.post.PostRepository;
import com.twohands.social_service.domain.post.PostStatus;
import com.twohands.social_service.domain.post.PostVisibility;
import com.twohands.social_service.domain.post.ProductTag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class HandlePostModeratedEventUseCaseTest {

    private final ProcessedDomainEventRepository processedDomainEventRepository =
            org.mockito.Mockito.mock(ProcessedDomainEventRepository.class);
    private final PostRepository postRepository = org.mockito.Mockito.mock(PostRepository.class);
    private final HandlePostModeratedEventUseCase useCase = new HandlePostModeratedEventUseCase(
            processedDomainEventRepository,
            postRepository
    );

    @Test
    void shouldHidePostAndMarkProcessed() {
        UUID eventId = UUID.randomUUID();
        UUID moderationLogId = UUID.randomUUID();
        String postId = "507f1f77bcf86cd799439011";
        Post post = buildPost(postId, PostStatus.ACTIVE, PostModerationStatus.NONE, null);

        when(processedDomainEventRepository.existsByEventId(eventId)).thenReturn(false);
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> invocation.getArgument(0));

        HandlePostModeratedEventResult result = useCase.execute(new HandlePostModeratedEventCommand(
                eventId,
                postId,
                moderationLogId,
                PostModerationAction.HIDE,
                "Spam",
                UUID.randomUUID(),
                Instant.parse("2026-05-23T10:00:00Z")
        ));

        assertThat(result.duplicate()).isFalse();
        verify(postRepository).save(any(Post.class));
        verify(processedDomainEventRepository).markProcessed(
                eventId,
                HandlePostModeratedEventUseCase.CONSUMER_NAME,
                "POST_MODERATED"
        );
        verify(postRepository).save(org.mockito.ArgumentMatchers.argThat(saved ->
                saved.moderationStatusOrDefault() == PostModerationStatus.HIDDEN
                        && saved.status() == PostStatus.ACTIVE
                        && moderationLogId.toString().equals(saved.lastModerationLogId())
        ));
    }

    @Test
    void shouldRemovePostAsDeleted() {
        UUID eventId = UUID.randomUUID();
        UUID moderationLogId = UUID.randomUUID();
        String postId = "507f1f77bcf86cd799439012";
        Post post = buildPost(postId, PostStatus.ACTIVE, PostModerationStatus.NONE, null);

        when(processedDomainEventRepository.existsByEventId(eventId)).thenReturn(false);
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> invocation.getArgument(0));

        useCase.execute(new HandlePostModeratedEventCommand(
                eventId,
                postId,
                moderationLogId,
                PostModerationAction.REMOVE,
                "Policy",
                UUID.randomUUID(),
                Instant.parse("2026-05-23T10:00:00Z")
        ));

        verify(postRepository).save(org.mockito.ArgumentMatchers.argThat(saved ->
                saved.status() == PostStatus.DELETED
                        && saved.moderationStatusOrDefault() == PostModerationStatus.REMOVED
                        && saved.deletedAt() != null
        ));
    }

    @Test
    void shouldSkipDuplicateEventId() {
        UUID eventId = UUID.randomUUID();
        when(processedDomainEventRepository.existsByEventId(eventId)).thenReturn(true);

        HandlePostModeratedEventResult result = useCase.execute(new HandlePostModeratedEventCommand(
                eventId,
                "507f1f77bcf86cd799439011",
                UUID.randomUUID(),
                PostModerationAction.HIDE,
                "Spam",
                UUID.randomUUID(),
                Instant.now()
        ));

        assertThat(result.duplicate()).isTrue();
        verify(postRepository, never()).save(any());
    }

    @Test
    void shouldAckWhenPostMissing() {
        UUID eventId = UUID.randomUUID();
        String postId = "507f1f77bcf86cd799439013";

        when(processedDomainEventRepository.existsByEventId(eventId)).thenReturn(false);
        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        HandlePostModeratedEventResult result = useCase.execute(new HandlePostModeratedEventCommand(
                eventId,
                postId,
                UUID.randomUUID(),
                PostModerationAction.REMOVE,
                "Policy",
                UUID.randomUUID(),
                Instant.now()
        ));

        assertThat(result.postMissing()).isTrue();
        verify(postRepository, never()).save(any());
        verify(processedDomainEventRepository).markProcessed(
                eq(eventId),
                eq(HandlePostModeratedEventUseCase.CONSUMER_NAME),
                eq("POST_MODERATED")
        );
    }

    private Post buildPost(String postId, PostStatus status, PostModerationStatus moderationStatus, String lastLogId) {
        Instant now = Instant.parse("2026-05-21T09:00:00Z");
        return new Post(
                postId,
                UUID.randomUUID().toString(),
                "caption",
                List.of(),
                List.of(new ProductTag("p1", new BigDecimal("1000"))),
                status,
                PostVisibility.PUBLIC,
                1L,
                0L,
                List.of(),
                true,
                moderationStatus,
                null,
                lastLogId,
                now,
                now,
                status == PostStatus.DELETED ? now : null
        );
    }
}
