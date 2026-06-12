package com.twohands.social_service.unit.application.integration.handlecommentmoderatedevent;

import com.twohands.social_service.application.integration.handlecommentmoderatedevent.HandleCommentModeratedEventCommand;
import com.twohands.social_service.application.integration.handlecommentmoderatedevent.HandleCommentModeratedEventResult;
import com.twohands.social_service.application.integration.handlecommentmoderatedevent.HandleCommentModeratedEventUseCase;
import com.twohands.social_service.domain.comment.Comment;
import com.twohands.social_service.domain.comment.CommentModerationAction;
import com.twohands.social_service.domain.comment.CommentModerationStatus;
import com.twohands.social_service.domain.comment.CommentRepository;
import com.twohands.social_service.domain.comment.CommentStatus;
import com.twohands.social_service.domain.integration.ProcessedDomainEventRepository;
import com.twohands.social_service.domain.post.PostRepository;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class HandleCommentModeratedEventUseCaseTest {

    private final ProcessedDomainEventRepository processedDomainEventRepository =
            org.mockito.Mockito.mock(ProcessedDomainEventRepository.class);
    private final CommentRepository commentRepository = org.mockito.Mockito.mock(CommentRepository.class);
    private final PostRepository postRepository = org.mockito.Mockito.mock(PostRepository.class);
    private final HandleCommentModeratedEventUseCase useCase = new HandleCommentModeratedEventUseCase(
            processedDomainEventRepository,
            commentRepository,
            postRepository
    );

    @Test
    void shouldHideCommentAndMarkProcessed() {
        UUID eventId = UUID.randomUUID();
        UUID moderationLogId = UUID.randomUUID();
        String commentId = "507f1f77bcf86cd799439011";
        Comment comment = buildComment(commentId, CommentStatus.ACTIVE, CommentModerationStatus.NONE, null);

        when(processedDomainEventRepository.existsByEventId(eventId)).thenReturn(false);
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        HandleCommentModeratedEventResult result = useCase.execute(new HandleCommentModeratedEventCommand(
                eventId,
                commentId,
                moderationLogId,
                CommentModerationAction.HIDE,
                "Spam",
                UUID.randomUUID(),
                Instant.parse("2026-05-23T10:00:00Z")
        ));

        assertThat(result.duplicate()).isFalse();
        verify(commentRepository).save(org.mockito.ArgumentMatchers.argThat(saved ->
                saved.moderationStatusOrDefault() == CommentModerationStatus.HIDDEN
                        && saved.status() == CommentStatus.ACTIVE
                        && moderationLogId.toString().equals(saved.lastModerationLogId())
        ));
        verify(processedDomainEventRepository).markProcessed(
                eventId,
                HandleCommentModeratedEventUseCase.CONSUMER_NAME,
                "COMMENT_MODERATED"
        );
    }

    @Test
    void shouldRemoveCommentAndDecrementReplyCount() {
        UUID eventId = UUID.randomUUID();
        UUID moderationLogId = UUID.randomUUID();
        String commentId = "507f1f77bcf86cd799439012";
        Comment comment = buildComment(commentId, CommentStatus.ACTIVE, CommentModerationStatus.NONE, null);

        when(processedDomainEventRepository.existsByEventId(eventId)).thenReturn(false);
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        useCase.execute(new HandleCommentModeratedEventCommand(
                eventId,
                commentId,
                moderationLogId,
                CommentModerationAction.REMOVE,
                "Policy",
                UUID.randomUUID(),
                Instant.parse("2026-05-23T10:00:00Z")
        ));

        verify(commentRepository).save(org.mockito.ArgumentMatchers.argThat(saved ->
                saved.status() == CommentStatus.DELETED
                        && saved.moderationStatusOrDefault() == CommentModerationStatus.REMOVED
                        && saved.deletedAt() != null
        ));
        verify(postRepository).decrementReplyCount("507f1f77bcf86cd799439011");
    }


    @Test
    void shouldRestoreRemovedCommentAndIncrementReplyCount() {
        UUID eventId = UUID.randomUUID();
        UUID moderationLogId = UUID.randomUUID();
        String commentId = "507f1f77bcf86cd799439013";
        Comment comment = buildComment(
                commentId,
                CommentStatus.DELETED,
                CommentModerationStatus.REMOVED,
                null
        );

        when(processedDomainEventRepository.existsByEventId(eventId)).thenReturn(false);
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        useCase.execute(new HandleCommentModeratedEventCommand(
                eventId,
                commentId,
                moderationLogId,
                CommentModerationAction.RESTORE,
                "Appeal approved",
                UUID.randomUUID(),
                Instant.parse("2026-05-23T11:00:00Z")
        ));

        verify(commentRepository).save(org.mockito.ArgumentMatchers.argThat(saved ->
                saved.status() == CommentStatus.ACTIVE
                        && saved.moderationStatusOrDefault() == CommentModerationStatus.NONE
                        && saved.deletedAt() == null
        ));
        verify(postRepository).incrementReplyCount("507f1f77bcf86cd799439011");
        verify(processedDomainEventRepository).markProcessed(
                eventId,
                HandleCommentModeratedEventUseCase.CONSUMER_NAME,
                "COMMENT_RESTORED"
        );
    }

    private Comment buildComment(
            String commentId,
            CommentStatus status,
            CommentModerationStatus moderationStatus,
            String lastModerationLogId
    ) {
        return new Comment(
                commentId,
                "507f1f77bcf86cd799439011",
                UUID.randomUUID().toString(),
                null,
                "Hello",
                List.of(),
                status,
                moderationStatus,
                null,
                lastModerationLogId,
                0L,
                Instant.parse("2026-05-19T00:00:00Z"),
                Instant.parse("2026-05-19T00:00:00Z"),
                null
        );
    }
}
