package com.twohands.social_service.unit.application.comment.likecomment;

import com.twohands.social_service.application.comment.common.CommentLikedOutboxService;
import com.twohands.social_service.application.comment.likecomment.LikeCommentCommand;
import com.twohands.social_service.application.comment.likecomment.LikeCommentResult;
import com.twohands.social_service.application.comment.likecomment.LikeCommentUseCase;
import com.twohands.social_service.domain.comment.Comment;
import com.twohands.social_service.domain.comment.CommentReactionRepository;
import com.twohands.social_service.domain.comment.CommentRepository;
import com.twohands.social_service.domain.comment.CommentStatus;
import com.twohands.social_service.domain.outbox.OutboxEvent;
import com.twohands.social_service.domain.outbox.OutboxEventRepository;
import com.twohands.social_service.domain.outbox.OutboxStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mockito.ArgumentCaptor;
import com.twohands.social_service.domain.user.UserProjection;
import com.twohands.social_service.application.user.common.UserWriteGuard;
import com.twohands.social_service.domain.user.UserProjectionRepository;
import com.twohands.social_service.testsupport.UserProjectionTestFixtures;
import com.twohands.social_service.exception.AppException;
import com.twohands.social_service.exception.ErrorCode;
import org.junit.jupiter.api.Test;

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

class LikeCommentUseCaseTest {

    private final CommentRepository commentRepository = mock(CommentRepository.class);
    private final CommentReactionRepository commentReactionRepository = mock(CommentReactionRepository.class);
    private final OutboxEventRepository outboxEventRepository = mock(OutboxEventRepository.class);
    private final CommentLikedOutboxService commentLikedOutboxService =
            new CommentLikedOutboxService(new ObjectMapper());
    private final UserProjectionRepository userProjectionRepository = mock(UserProjectionRepository.class);
    private final UserWriteGuard userWriteGuard = new UserWriteGuard(userProjectionRepository);
    private final LikeCommentUseCase useCase = new LikeCommentUseCase(
            commentRepository,
            commentReactionRepository,
            outboxEventRepository,
            commentLikedOutboxService,
            userWriteGuard
    );

    private Comment buildComment(String commentId, CommentStatus status, long likeCount) {
        return new Comment(
                commentId,
                "post-id",
                UUID.randomUUID().toString(),
                null,
                "Hello",
                List.of(),
                status,
                likeCount,
                Instant.now(),
                Instant.now(),
                null
        );
    }

    @Test
    void shouldLikeCommentWhenNotYetLiked() {
        UUID userId = UUID.randomUUID();
        String commentId = "comment-id";
        Comment active = buildComment(commentId, CommentStatus.ACTIVE, 0L);
        Comment afterLike = buildComment(commentId, CommentStatus.ACTIVE, 1L);

        when(userProjectionRepository.findByUserId(userId)).thenReturn(UserProjectionTestFixtures.activeOptional(userId));
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(active), Optional.of(afterLike));
        when(commentReactionRepository.existsByCommentIdAndUserId(commentId, userId)).thenReturn(false);
        when(outboxEventRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        LikeCommentResult result = useCase.execute(new LikeCommentCommand(userId, commentId));

        verify(commentReactionRepository).save(commentId, userId);
        verify(commentRepository).incrementLikeCount(commentId);
        verify(commentReactionRepository, never()).deleteByCommentIdAndUserId(commentId, userId);

        ArgumentCaptor<OutboxEvent> outboxCaptor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(outboxEventRepository).save(outboxCaptor.capture());
        assertThat(outboxCaptor.getValue().eventType()).isEqualTo("COMMENT_LIKED");
        assertThat(outboxCaptor.getValue().status()).isEqualTo(OutboxStatus.PENDING);

        assertThat(result.liked()).isTrue();
        assertThat(result.likeCount()).isEqualTo(1L);
    }

    @Test
    void shouldUnlikeCommentWhenAlreadyLiked() {
        UUID userId = UUID.randomUUID();
        String commentId = "comment-id";
        Comment active = buildComment(commentId, CommentStatus.ACTIVE, 1L);
        Comment afterUnlike = buildComment(commentId, CommentStatus.ACTIVE, 0L);

        when(userProjectionRepository.findByUserId(userId)).thenReturn(UserProjectionTestFixtures.activeOptional(userId));
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(active), Optional.of(afterUnlike));
        when(commentReactionRepository.existsByCommentIdAndUserId(commentId, userId)).thenReturn(true);

        LikeCommentResult result = useCase.execute(new LikeCommentCommand(userId, commentId));

        verify(commentReactionRepository).deleteByCommentIdAndUserId(commentId, userId);
        verify(commentRepository).decrementLikeCount(commentId);
        verify(commentReactionRepository, never()).save(commentId, userId);
        verify(outboxEventRepository, never()).save(any());

        assertThat(result.liked()).isFalse();
        assertThat(result.likeCount()).isZero();
    }

    @Test
    void shouldThrowNotFoundWhenCommentDoesNotExist() {
        UUID userId = UUID.randomUUID();
        when(userProjectionRepository.findByUserId(userId)).thenReturn(UserProjectionTestFixtures.activeOptional(userId));
        when(commentRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(new LikeCommentCommand(userId, "missing")))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.RESOURCE_NOT_FOUND));
    }

    @Test
    void shouldThrowNotFoundWhenCommentIsDeleted() {
        UUID userId = UUID.randomUUID();
        String commentId = "comment-id";
        when(userProjectionRepository.findByUserId(userId)).thenReturn(UserProjectionTestFixtures.activeOptional(userId));
        when(commentRepository.findById(commentId))
                .thenReturn(Optional.of(buildComment(commentId, CommentStatus.DELETED, 0L)));

        assertThatThrownBy(() -> useCase.execute(new LikeCommentCommand(userId, commentId)))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.RESOURCE_NOT_FOUND));
    }

    @Test
    void shouldThrowForbiddenWhenUserIsSuspended() {
        UUID userId = UUID.randomUUID();
        when(userProjectionRepository.findByUserId(userId))
                .thenReturn(Optional.of(new UserProjection(userId.toString(), "SUSPENDED", "User", null, false)));

        assertThatThrownBy(() -> useCase.execute(new LikeCommentCommand(userId, "comment-id")))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.ACCOUNT_SUSPENDED));
    }
}
