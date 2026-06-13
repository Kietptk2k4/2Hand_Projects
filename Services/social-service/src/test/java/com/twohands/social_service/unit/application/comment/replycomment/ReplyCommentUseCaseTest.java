package com.twohands.social_service.unit.application.comment.replycomment;

import com.twohands.social_service.application.comment.common.CommentAuthorResolver;
import com.twohands.social_service.application.comment.common.CommentCreatedOutboxService;
import com.twohands.social_service.application.comment.replycomment.ReplyCommentCommand;
import com.twohands.social_service.application.comment.replycomment.ReplyCommentResult;
import com.twohands.social_service.application.comment.replycomment.ReplyCommentUseCase;
import com.twohands.social_service.domain.comment.Comment;
import com.twohands.social_service.domain.comment.CommentMediaItem;
import com.twohands.social_service.domain.comment.CommentRepository;
import com.twohands.social_service.domain.comment.CommentStatus;
import com.twohands.social_service.domain.outbox.OutboxEvent;
import com.twohands.social_service.domain.outbox.OutboxEventRepository;
import com.twohands.social_service.domain.outbox.OutboxStatus;
import com.twohands.social_service.domain.post.MediaItem;
import com.twohands.social_service.domain.post.Post;
import com.twohands.social_service.domain.post.PostModerationStatus;
import com.twohands.social_service.domain.post.PostRepository;
import com.twohands.social_service.domain.post.PostStatus;
import com.twohands.social_service.domain.post.PostVisibility;
import com.twohands.social_service.domain.user.UserProjection;
import com.twohands.social_service.application.user.common.UserWriteGuard;
import com.twohands.social_service.domain.user.UserProjectionRepository;
import com.twohands.social_service.testsupport.UserProjectionTestFixtures;
import com.twohands.social_service.exception.AppException;
import com.twohands.social_service.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

class ReplyCommentUseCaseTest {

    private final CommentRepository commentRepository = mock(CommentRepository.class);
    private final PostRepository postRepository = mock(PostRepository.class);
    private final OutboxEventRepository outboxEventRepository = mock(OutboxEventRepository.class);
    private final CommentCreatedOutboxService commentCreatedOutboxService =
            new CommentCreatedOutboxService(new ObjectMapper());
    private final UserProjectionRepository userProjectionRepository = mock(UserProjectionRepository.class);
    private final UserWriteGuard userWriteGuard = new UserWriteGuard(userProjectionRepository);
    private final CommentAuthorResolver commentAuthorResolver = new CommentAuthorResolver(userProjectionRepository);
    private final ReplyCommentUseCase useCase = new ReplyCommentUseCase(
            commentRepository,
            postRepository,
            outboxEventRepository,
            commentCreatedOutboxService,
            userWriteGuard,
            commentAuthorResolver
    );

    private Comment buildParentComment(UUID authorId, String commentId, String postId) {
        return new Comment(
                commentId,
                postId,
                authorId.toString(),
                null,
                "Parent content",
                List.of(),
                CommentStatus.ACTIVE,
                null,
                null,
                null,
                0L,
                Instant.parse("2026-05-19T00:00:00Z"),
                Instant.parse("2026-05-19T00:00:00Z"),
                null
        );
    }

    private Post buildPost(String postId, PostStatus status, boolean allowComments) {
        return new Post(
                postId,
                UUID.randomUUID().toString(),
                "caption",
                List.of(new MediaItem("https://cdn/1.jpg", "IMAGE")),
                List.of(),
                status,
                PostVisibility.PUBLIC,
                0L,
                0L,
                List.of(),
                allowComments,
                PostModerationStatus.NONE,
                null,
                null,
                Instant.parse("2026-05-19T00:00:00Z"),
                Instant.parse("2026-05-19T00:00:00Z"),
                null
        );
    }

    @Test
    void shouldCreateReplyAndPublishOutboxEvent() {
        UUID authorId = UUID.randomUUID();
        String parentId = "parent-comment-id";
        String postId = "507f1f77bcf86cd799439011";

        when(userProjectionRepository.findByUserId(authorId)).thenReturn(UserProjectionTestFixtures.activeOptional(authorId));
        when(commentRepository.findById(parentId))
                .thenReturn(Optional.of(buildParentComment(authorId, parentId, postId)));
        when(postRepository.findById(postId))
                .thenReturn(Optional.of(buildPost(postId, PostStatus.ACTIVE, true)));
        when(commentRepository.save(any())).thenAnswer(inv -> {
            Comment c = inv.getArgument(0);
            return new Comment(
                    "reply-id", c.postId(), c.authorId(), c.parentCommentId(), c.contentText(),
                    c.media(), c.status(), c.moderationStatus(), c.moderationReason(),
                    c.lastModerationLogId(), c.likeCount(), c.createdAt(), c.updatedAt(), c.deletedAt()
            );
        });
        when(outboxEventRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ReplyCommentCommand command = new ReplyCommentCommand(
                authorId, parentId, "Thanks!", List.of()
        );

        ReplyCommentResult result = useCase.execute(command);

        verify(postRepository).incrementReplyCount(postId);
        ArgumentCaptor<OutboxEvent> outboxCaptor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(outboxEventRepository).save(outboxCaptor.capture());

        assertThat(result.commentId()).isEqualTo("reply-id");
        assertThat(result.parentCommentId()).isEqualTo(parentId);
        assertThat(result.postId()).isEqualTo(postId);
        assertThat(result.author().userId()).isEqualTo(authorId.toString());
        assertThat(result.author().displayName()).isEqualTo("User");
        assertThat(outboxCaptor.getValue().eventType()).isEqualTo("COMMENT_CREATED");
        assertThat(outboxCaptor.getValue().status()).isEqualTo(OutboxStatus.PENDING);
    }

    @Test
    void shouldThrowNotFoundWhenParentCommentDoesNotExist() {
        UUID authorId = UUID.randomUUID();
        when(userProjectionRepository.findByUserId(authorId)).thenReturn(UserProjectionTestFixtures.activeOptional(authorId));
        when(commentRepository.findById("missing")).thenReturn(Optional.empty());

        ReplyCommentCommand command = new ReplyCommentCommand(authorId, "missing", "Reply", List.of());

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.RESOURCE_NOT_FOUND));
    }

    @Test
    void shouldThrowNotFoundWhenParentCommentIsDeleted() {
        UUID authorId = UUID.randomUUID();
        String parentId = "deleted-parent";
        Comment deletedParent = new Comment(
                parentId, "post-id", authorId.toString(), null, "text", List.of(),
                CommentStatus.DELETED, null, null, null, 0L, Instant.now(), Instant.now(), Instant.now()
        );

        when(userProjectionRepository.findByUserId(authorId)).thenReturn(UserProjectionTestFixtures.activeOptional(authorId));
        when(commentRepository.findById(parentId)).thenReturn(Optional.of(deletedParent));

        assertThatThrownBy(() -> useCase.execute(new ReplyCommentCommand(authorId, parentId, "Reply", List.of())))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.RESOURCE_NOT_FOUND));
    }

    @Test
    void shouldThrowValidationErrorWhenReplyingToNestedComment() {
        UUID authorId = UUID.randomUUID();
        String parentId = "nested-parent";
        Comment nestedParent = new Comment(
                parentId, "post-id", authorId.toString(), "grandparent-id", "text", List.of(),
                CommentStatus.ACTIVE, null, null, null, 0L, Instant.now(), Instant.now(), null
        );

        when(userProjectionRepository.findByUserId(authorId)).thenReturn(UserProjectionTestFixtures.activeOptional(authorId));
        when(commentRepository.findById(parentId)).thenReturn(Optional.of(nestedParent));

        assertThatThrownBy(() -> useCase.execute(new ReplyCommentCommand(authorId, parentId, "Reply", List.of())))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> {
                    AppException appEx = (AppException) ex;
                    assertThat(appEx.getErrorCode()).isEqualTo(ErrorCode.VALIDATION_ERROR);
                    assertThat(appEx.getField()).isEqualTo("parentCommentId");
                });
    }

    @Test
    void shouldThrowForbiddenWhenPostDisallowsComments() {
        UUID authorId = UUID.randomUUID();
        String parentId = "parent-id";
        String postId = "post-id";

        when(userProjectionRepository.findByUserId(authorId)).thenReturn(UserProjectionTestFixtures.activeOptional(authorId));
        when(commentRepository.findById(parentId))
                .thenReturn(Optional.of(buildParentComment(authorId, parentId, postId)));
        when(postRepository.findById(postId))
                .thenReturn(Optional.of(buildPost(postId, PostStatus.ACTIVE, false)));

        assertThatThrownBy(() -> useCase.execute(new ReplyCommentCommand(authorId, parentId, "Reply", List.of())))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN));

        verify(commentRepository, never()).save(any());
    }

    @Test
    void shouldThrowForbiddenWhenUserIsSuspended() {
        UUID authorId = UUID.randomUUID();
        when(userProjectionRepository.findByUserId(authorId))
                .thenReturn(Optional.of(new UserProjection(authorId.toString(), "SUSPENDED", "User", null, null, false)));

        assertThatThrownBy(() -> useCase.execute(new ReplyCommentCommand(authorId, "parent", "Reply", List.of())))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.ACCOUNT_SUSPENDED));
    }
}
