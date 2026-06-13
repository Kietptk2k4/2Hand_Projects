package com.twohands.social_service.unit.application.post.likeunlikepost;

import com.twohands.social_service.application.post.common.PostLikedOutboxService;
import com.twohands.social_service.application.post.likeunlikepost.LikeUnlikePostCommand;
import com.twohands.social_service.application.post.likeunlikepost.LikeUnlikePostResult;
import com.twohands.social_service.application.post.likeunlikepost.LikeUnlikePostUseCase;
import com.twohands.social_service.domain.outbox.OutboxEvent;
import com.twohands.social_service.domain.outbox.OutboxEventRepository;
import com.twohands.social_service.domain.outbox.OutboxStatus;
import com.twohands.social_service.domain.post.MediaItem;
import com.twohands.social_service.domain.post.Post;
import com.twohands.social_service.domain.post.PostModerationStatus;
import com.twohands.social_service.domain.post.PostLikeRepository;
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

class LikeUnlikePostUseCaseTest {

    private final PostRepository postRepository = mock(PostRepository.class);
    private final PostLikeRepository postLikeRepository = mock(PostLikeRepository.class);
    private final OutboxEventRepository outboxEventRepository = mock(OutboxEventRepository.class);
    private final PostLikedOutboxService postLikedOutboxService = new PostLikedOutboxService(new ObjectMapper());
    private final UserProjectionRepository userProjectionRepository = mock(UserProjectionRepository.class);
    private final UserWriteGuard userWriteGuard = new UserWriteGuard(userProjectionRepository);
    private final LikeUnlikePostUseCase useCase = new LikeUnlikePostUseCase(
            postRepository,
            postLikeRepository,
            outboxEventRepository,
            postLikedOutboxService,
            userWriteGuard
    );

    private Post buildPost(String postId, PostStatus status, long likeCount) {
        return new Post(
                postId,
                UUID.randomUUID().toString(),
                "caption",
                List.of(new MediaItem("https://cdn/1.jpg", "IMAGE")),
                List.of(),
                status,
                PostVisibility.PUBLIC,
                likeCount,
                0L,
                List.of(),
                true,
                PostModerationStatus.NONE,
                null,
                null,
                Instant.now(),
                Instant.now(),
                null
        );
    }

    @Test
    void shouldLikePostAndPublishOutboxEvent() {
        UUID userId = UUID.randomUUID();
        String postId = "507f1f77bcf86cd799439011";
        Post active = buildPost(postId, PostStatus.ACTIVE, 0L);
        Post afterLike = buildPost(postId, PostStatus.ACTIVE, 1L);

        when(userProjectionRepository.findByUserId(userId)).thenReturn(UserProjectionTestFixtures.activeOptional(userId));
        when(postRepository.findById(postId)).thenReturn(Optional.of(active), Optional.of(afterLike));
        when(postLikeRepository.existsByPostIdAndUserId(postId, userId)).thenReturn(false);
        when(outboxEventRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        LikeUnlikePostResult result = useCase.execute(new LikeUnlikePostCommand(userId, postId));

        verify(postLikeRepository).save(postId, userId);
        verify(postRepository).incrementLikeCount(postId);
        ArgumentCaptor<OutboxEvent> outboxCaptor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(outboxEventRepository).save(outboxCaptor.capture());
        assertThat(outboxCaptor.getValue().eventType()).isEqualTo("POST_LIKED");
        assertThat(outboxCaptor.getValue().status()).isEqualTo(OutboxStatus.PENDING);

        assertThat(result.liked()).isTrue();
        assertThat(result.likeCount()).isEqualTo(1L);
    }

    @Test
    void shouldUnlikePostWithoutOutboxEvent() {
        UUID userId = UUID.randomUUID();
        String postId = "507f1f77bcf86cd799439011";
        Post active = buildPost(postId, PostStatus.ACTIVE, 1L);
        Post afterUnlike = buildPost(postId, PostStatus.ACTIVE, 0L);

        when(userProjectionRepository.findByUserId(userId)).thenReturn(UserProjectionTestFixtures.activeOptional(userId));
        when(postRepository.findById(postId)).thenReturn(Optional.of(active), Optional.of(afterUnlike));
        when(postLikeRepository.existsByPostIdAndUserId(postId, userId)).thenReturn(true);

        LikeUnlikePostResult result = useCase.execute(new LikeUnlikePostCommand(userId, postId));

        verify(postLikeRepository).deleteByPostIdAndUserId(postId, userId);
        verify(postRepository).decrementLikeCount(postId);
        verify(outboxEventRepository, never()).save(any());
        verify(postLikeRepository, never()).save(postId, userId);

        assertThat(result.liked()).isFalse();
        assertThat(result.likeCount()).isZero();
    }

    @Test
    void shouldThrowNotFoundWhenPostDoesNotExist() {
        UUID userId = UUID.randomUUID();
        when(userProjectionRepository.findByUserId(userId)).thenReturn(UserProjectionTestFixtures.activeOptional(userId));
        when(postRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(new LikeUnlikePostCommand(userId, "missing")))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.RESOURCE_NOT_FOUND));
    }

    @Test
    void shouldThrowNotFoundWhenPostIsNotActive() {
        UUID userId = UUID.randomUUID();
        String postId = "507f1f77bcf86cd799439011";
        when(userProjectionRepository.findByUserId(userId)).thenReturn(UserProjectionTestFixtures.activeOptional(userId));
        when(postRepository.findById(postId)).thenReturn(Optional.of(buildPost(postId, PostStatus.DELETED, 0L)));

        assertThatThrownBy(() -> useCase.execute(new LikeUnlikePostCommand(userId, postId)))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.RESOURCE_NOT_FOUND));
    }

    @Test
    void shouldThrowForbiddenWhenUserIsSuspended() {
        UUID userId = UUID.randomUUID();
        when(userProjectionRepository.findByUserId(userId))
                .thenReturn(Optional.of(new UserProjection(userId.toString(), "SUSPENDED", "User", null, null, false)));

        assertThatThrownBy(() -> useCase.execute(new LikeUnlikePostCommand(userId, "post-id")))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.ACCOUNT_SUSPENDED));
    }
}
