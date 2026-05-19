package com.twohands.social_service.unit.application.comment.deleteowncomment;

import com.twohands.social_service.application.comment.deleteowncomment.DeleteOwnCommentCommand;
import com.twohands.social_service.application.comment.deleteowncomment.DeleteOwnCommentResult;
import com.twohands.social_service.application.comment.deleteowncomment.DeleteOwnCommentUseCase;
import com.twohands.social_service.domain.comment.Comment;
import com.twohands.social_service.domain.comment.CommentRepository;
import com.twohands.social_service.domain.comment.CommentStatus;
import com.twohands.social_service.domain.post.PostRepository;
import com.twohands.social_service.domain.user.UserProjection;
import com.twohands.social_service.domain.user.UserProjectionRepository;
import com.twohands.social_service.exception.AppException;
import com.twohands.social_service.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DeleteOwnCommentUseCaseTest {

    private final CommentRepository commentRepository = mock(CommentRepository.class);
    private final PostRepository postRepository = mock(PostRepository.class);
    private final UserProjectionRepository userProjectionRepository = mock(UserProjectionRepository.class);
    private final DeleteOwnCommentUseCase useCase = new DeleteOwnCommentUseCase(
            commentRepository, postRepository, userProjectionRepository
    );

    private Comment buildComment(UUID authorId, String commentId, CommentStatus status, Instant deletedAt) {
        return new Comment(
                commentId,
                "507f1f77bcf86cd799439011",
                authorId.toString(),
                null,
                "Hello",
                List.of(),
                status,
                0L,
                Instant.parse("2026-05-19T00:00:00Z"),
                Instant.parse("2026-05-19T00:00:00Z"),
                deletedAt
        );
    }

    @Test
    void shouldSoftDeleteCommentAndDecrementReplyCount() {
        UUID authorId = UUID.randomUUID();
        String commentId = "comment-id";
        Comment existing = buildComment(authorId, commentId, CommentStatus.ACTIVE, null);

        when(userProjectionRepository.findByUserId(authorId)).thenReturn(Optional.empty());
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(existing));
        when(commentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        DeleteOwnCommentResult result = useCase.execute(
                new DeleteOwnCommentCommand(authorId, List.of("USER"), commentId)
        );

        ArgumentCaptor<Comment> commentCaptor = ArgumentCaptor.forClass(Comment.class);
        verify(commentRepository).save(commentCaptor.capture());
        assertThat(commentCaptor.getValue().status()).isEqualTo(CommentStatus.DELETED);
        assertThat(commentCaptor.getValue().deletedAt()).isNotNull();

        verify(postRepository).decrementReplyCount("507f1f77bcf86cd799439011");
        assertThat(result.status()).isEqualTo("DELETED");
        assertThat(result.commentId()).isEqualTo(commentId);
    }

    @Test
    void shouldReturnSuccessIdempotentlyWhenCommentAlreadyDeleted() {
        UUID authorId = UUID.randomUUID();
        String commentId = "comment-id";
        Instant deletedAt = Instant.parse("2026-05-19T08:00:00Z");
        Comment existing = buildComment(authorId, commentId, CommentStatus.DELETED, deletedAt);

        when(userProjectionRepository.findByUserId(authorId)).thenReturn(Optional.empty());
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(existing));

        DeleteOwnCommentResult result = useCase.execute(
                new DeleteOwnCommentCommand(authorId, List.of(), commentId)
        );

        verify(commentRepository, never()).save(any());
        verify(postRepository, never()).decrementReplyCount(any());
        assertThat(result.status()).isEqualTo("DELETED");
        assertThat(result.deletedAt()).isEqualTo(deletedAt.toString());
    }

    @Test
    void shouldAllowModeratorToDeleteComment() {
        UUID authorId = UUID.randomUUID();
        UUID moderatorId = UUID.randomUUID();
        String commentId = "comment-id";
        Comment existing = buildComment(authorId, commentId, CommentStatus.ACTIVE, null);

        when(userProjectionRepository.findByUserId(moderatorId)).thenReturn(Optional.empty());
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(existing));
        when(commentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        useCase.execute(new DeleteOwnCommentCommand(moderatorId, List.of("MODERATOR"), commentId));

        verify(commentRepository).save(any());
        verify(postRepository).decrementReplyCount(existing.postId());
    }

    @Test
    void shouldThrowNotFoundWhenCommentDoesNotExist() {
        UUID authorId = UUID.randomUUID();
        when(userProjectionRepository.findByUserId(authorId)).thenReturn(Optional.empty());
        when(commentRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(
                new DeleteOwnCommentCommand(authorId, List.of(), "missing")))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.RESOURCE_NOT_FOUND));
    }

    @Test
    void shouldThrowForbiddenWhenActorIsNotAuthorNorModerator() {
        UUID authorId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();
        String commentId = "comment-id";

        when(userProjectionRepository.findByUserId(otherUserId)).thenReturn(Optional.empty());
        when(commentRepository.findById(commentId))
                .thenReturn(Optional.of(buildComment(authorId, commentId, CommentStatus.ACTIVE, null)));

        assertThatThrownBy(() -> useCase.execute(
                new DeleteOwnCommentCommand(otherUserId, List.of("USER"), commentId)))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN));

        verify(commentRepository, never()).save(any());
    }

    @Test
    void shouldThrowForbiddenWhenUserIsSuspended() {
        UUID authorId = UUID.randomUUID();
        when(userProjectionRepository.findByUserId(authorId))
                .thenReturn(Optional.of(new UserProjection(authorId.toString(), "SUSPENDED", "User", null)));

        assertThatThrownBy(() -> useCase.execute(
                new DeleteOwnCommentCommand(authorId, List.of(), "comment-id")))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.ACCOUNT_SUSPENDED));
    }
}
