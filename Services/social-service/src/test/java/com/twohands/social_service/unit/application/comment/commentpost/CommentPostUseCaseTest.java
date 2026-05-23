package com.twohands.social_service.unit.application.comment.commentpost;

import com.twohands.social_service.application.comment.commentpost.CommentPostCommand;
import com.twohands.social_service.application.comment.commentpost.CommentPostResult;
import com.twohands.social_service.application.comment.commentpost.CommentPostUseCase;
import com.twohands.social_service.application.comment.common.CommentCreatedOutboxService;
import com.twohands.social_service.domain.comment.Comment;
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

class CommentPostUseCaseTest {

    private final CommentRepository commentRepository = mock(CommentRepository.class);
    private final PostRepository postRepository = mock(PostRepository.class);
    private final OutboxEventRepository outboxEventRepository = mock(OutboxEventRepository.class);
    private final CommentCreatedOutboxService commentCreatedOutboxService =
            new CommentCreatedOutboxService(new ObjectMapper());
    private final UserProjectionRepository userProjectionRepository = mock(UserProjectionRepository.class);
    private final UserWriteGuard userWriteGuard = new UserWriteGuard(userProjectionRepository);
    private final CommentPostUseCase useCase = new CommentPostUseCase(
            commentRepository,
            postRepository,
            outboxEventRepository,
            commentCreatedOutboxService,
            userWriteGuard
    );

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
    void shouldCreateTopLevelCommentAndPublishOutboxEvent() {
        UUID authorId = UUID.randomUUID();
        String postId = "507f1f77bcf86cd799439011";

        when(userProjectionRepository.findByUserId(authorId)).thenReturn(UserProjectionTestFixtures.activeOptional(authorId));
        when(postRepository.findById(postId))
                .thenReturn(Optional.of(buildPost(postId, PostStatus.ACTIVE, true)));
        when(commentRepository.save(any())).thenAnswer(inv -> {
            Comment c = inv.getArgument(0);
            return new Comment(
                    "comment-id", c.postId(), c.authorId(), c.parentCommentId(), c.contentText(),
                    c.media(), c.status(), c.likeCount(), c.createdAt(), c.updatedAt(), c.deletedAt()
            );
        });
        when(outboxEventRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CommentPostResult result = useCase.execute(new CommentPostCommand(
                authorId, postId, "Great post!", List.of()
        ));

        assertThat(result.commentId()).isEqualTo("comment-id");
        assertThat(result.parentCommentId()).isNull();

        verify(postRepository).incrementReplyCount(postId);

        ArgumentCaptor<Comment> commentCaptor = ArgumentCaptor.forClass(Comment.class);
        verify(commentRepository).save(commentCaptor.capture());
        assertThat(commentCaptor.getValue().parentCommentId()).isNull();

        ArgumentCaptor<OutboxEvent> outboxCaptor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(outboxEventRepository).save(outboxCaptor.capture());
        assertThat(outboxCaptor.getValue().eventType()).isEqualTo("COMMENT_CREATED");
        assertThat(outboxCaptor.getValue().status()).isEqualTo(OutboxStatus.PENDING);
    }

    @Test
    void shouldRejectWhenCommentsDisabled() {
        UUID authorId = UUID.randomUUID();
        String postId = "507f1f77bcf86cd799439011";

        when(userProjectionRepository.findByUserId(authorId)).thenReturn(UserProjectionTestFixtures.activeOptional(authorId));
        when(postRepository.findById(postId))
                .thenReturn(Optional.of(buildPost(postId, PostStatus.ACTIVE, false)));

        assertThatThrownBy(() -> useCase.execute(new CommentPostCommand(
                authorId, postId, "Nice!", List.of()
        )))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN));

        verify(commentRepository, never()).save(any());
        verify(outboxEventRepository, never()).save(any());
    }

    @Test
    void shouldReturnNotFoundWhenPostMissing() {
        UUID authorId = UUID.randomUUID();
        String postId = "507f1f77bcf86cd799439011";

        when(userProjectionRepository.findByUserId(authorId)).thenReturn(UserProjectionTestFixtures.activeOptional(authorId));
        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(new CommentPostCommand(
                authorId, postId, "Nice!", List.of()
        )))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.RESOURCE_NOT_FOUND));
    }
}
