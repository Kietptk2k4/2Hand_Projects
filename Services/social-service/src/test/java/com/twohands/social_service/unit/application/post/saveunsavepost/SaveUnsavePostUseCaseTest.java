package com.twohands.social_service.unit.application.post.saveunsavepost;

import com.twohands.social_service.application.post.saveunsavepost.SaveUnsavePostCommand;
import com.twohands.social_service.application.post.saveunsavepost.SaveUnsavePostResult;
import com.twohands.social_service.application.post.saveunsavepost.SaveUnsavePostUseCase;
import com.twohands.social_service.domain.post.MediaItem;
import com.twohands.social_service.domain.post.Post;
import com.twohands.social_service.domain.post.PostModerationStatus;
import com.twohands.social_service.domain.post.PostRepository;
import com.twohands.social_service.domain.post.PostSaveRepository;
import com.twohands.social_service.domain.post.PostStatus;
import com.twohands.social_service.domain.post.PostVisibility;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SaveUnsavePostUseCaseTest {

    private final PostRepository postRepository = mock(PostRepository.class);
    private final PostSaveRepository postSaveRepository = mock(PostSaveRepository.class);
    private final UserProjectionRepository userProjectionRepository = mock(UserProjectionRepository.class);
    private final UserWriteGuard userWriteGuard = new UserWriteGuard(userProjectionRepository);
    private final SaveUnsavePostUseCase useCase = new SaveUnsavePostUseCase(
            postRepository,
            postSaveRepository,
            userWriteGuard
    );

    private Post buildPost(String postId, PostStatus status) {
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
    void shouldSavePost() {
        UUID userId = UUID.randomUUID();
        String postId = "507f1f77bcf86cd799439011";

        when(userProjectionRepository.findByUserId(userId)).thenReturn(UserProjectionTestFixtures.activeOptional(userId));
        when(postRepository.findById(postId)).thenReturn(Optional.of(buildPost(postId, PostStatus.ACTIVE)));
        when(postSaveRepository.existsByPostIdAndUserId(postId, userId)).thenReturn(false);

        SaveUnsavePostResult result = useCase.execute(new SaveUnsavePostCommand(userId, postId));

        verify(postSaveRepository).save(postId, userId);
        verify(postSaveRepository, never()).deleteByPostIdAndUserId(postId, userId);
        assertThat(result.saved()).isTrue();
        assertThat(result.postId()).isEqualTo(postId);
    }

    @Test
    void shouldUnsavePost() {
        UUID userId = UUID.randomUUID();
        String postId = "507f1f77bcf86cd799439011";

        when(userProjectionRepository.findByUserId(userId)).thenReturn(UserProjectionTestFixtures.activeOptional(userId));
        when(postRepository.findById(postId)).thenReturn(Optional.of(buildPost(postId, PostStatus.ACTIVE)));
        when(postSaveRepository.existsByPostIdAndUserId(postId, userId)).thenReturn(true);

        SaveUnsavePostResult result = useCase.execute(new SaveUnsavePostCommand(userId, postId));

        verify(postSaveRepository).deleteByPostIdAndUserId(postId, userId);
        verify(postSaveRepository, never()).save(postId, userId);
        assertThat(result.saved()).isFalse();
    }

    @Test
    void shouldThrowNotFoundWhenPostDoesNotExist() {
        UUID userId = UUID.randomUUID();
        when(userProjectionRepository.findByUserId(userId)).thenReturn(UserProjectionTestFixtures.activeOptional(userId));
        when(postRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(new SaveUnsavePostCommand(userId, "missing")))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.RESOURCE_NOT_FOUND));
    }

    @Test
    void shouldThrowNotFoundWhenPostIsDeleted() {
        UUID userId = UUID.randomUUID();
        String postId = "507f1f77bcf86cd799439011";
        when(userProjectionRepository.findByUserId(userId)).thenReturn(UserProjectionTestFixtures.activeOptional(userId));
        when(postRepository.findById(postId)).thenReturn(Optional.of(buildPost(postId, PostStatus.DELETED)));

        assertThatThrownBy(() -> useCase.execute(new SaveUnsavePostCommand(userId, postId)))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.RESOURCE_NOT_FOUND));
    }

    @Test
    void shouldThrowForbiddenWhenUserIsSuspended() {
        UUID userId = UUID.randomUUID();
        when(userProjectionRepository.findByUserId(userId))
                .thenReturn(Optional.of(new UserProjection(userId.toString(), "SUSPENDED", "User", null, null, false)));

        assertThatThrownBy(() -> useCase.execute(new SaveUnsavePostCommand(userId, "post-id")))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.ACCOUNT_SUSPENDED));
    }
}
