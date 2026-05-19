package com.twohands.social_service.unit.application.post.deletepost;

import com.twohands.social_service.application.post.deletepost.DeletePostCommand;
import com.twohands.social_service.application.post.deletepost.DeletePostResult;
import com.twohands.social_service.application.post.deletepost.DeletePostUseCase;
import com.twohands.social_service.domain.post.MediaItem;
import com.twohands.social_service.domain.post.Post;
import com.twohands.social_service.domain.post.PostRepository;
import com.twohands.social_service.domain.post.PostStatus;
import com.twohands.social_service.domain.post.PostVisibility;
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

class DeletePostUseCaseTest {

    private final PostRepository postRepository = mock(PostRepository.class);
    private final UserProjectionRepository userProjectionRepository = mock(UserProjectionRepository.class);
    private final DeletePostUseCase useCase = new DeletePostUseCase(postRepository, userProjectionRepository);

    private Post buildPost(UUID authorId, String postId, PostStatus status, Instant deletedAt) {
        return new Post(
                postId,
                authorId.toString(),
                "Caption",
                List.of(new MediaItem("https://cdn/1.jpg", "IMAGE")),
                List.of(),
                status,
                PostVisibility.PUBLIC,
                3L,
                1L,
                List.of("tag"),
                true,
                Instant.parse("2026-05-19T00:00:00Z"),
                Instant.parse("2026-05-19T00:00:00Z"),
                deletedAt
        );
    }

    @Test
    void shouldSoftDeletePostWhenActorIsAuthor() {
        UUID authorId = UUID.randomUUID();
        String postId = "507f1f77bcf86cd799439011";
        Post existing = buildPost(authorId, postId, PostStatus.ACTIVE, null);

        when(userProjectionRepository.findByUserId(authorId)).thenReturn(Optional.empty());
        when(postRepository.findById(postId)).thenReturn(Optional.of(existing));
        when(postRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        DeletePostResult result = useCase.execute(new DeletePostCommand(authorId, List.of("USER"), postId));

        ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(postCaptor.capture());
        Post saved = postCaptor.getValue();

        assertThat(saved.status()).isEqualTo(PostStatus.DELETED);
        assertThat(saved.deletedAt()).isNotNull();
        assertThat(saved.updatedAt()).isNotNull();
        assertThat(saved.likeCount()).isEqualTo(3L);

        assertThat(result.status()).isEqualTo("DELETED");
        assertThat(result.postId()).isEqualTo(postId);
    }

    @Test
    void shouldSoftDeletePostWhenActorIsModerator() {
        UUID authorId = UUID.randomUUID();
        UUID moderatorId = UUID.randomUUID();
        String postId = "507f1f77bcf86cd799439011";
        Post existing = buildPost(authorId, postId, PostStatus.ACTIVE, null);

        when(userProjectionRepository.findByUserId(moderatorId)).thenReturn(Optional.empty());
        when(postRepository.findById(postId)).thenReturn(Optional.of(existing));
        when(postRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        useCase.execute(new DeletePostCommand(moderatorId, List.of("MODERATOR"), postId));

        verify(postRepository).save(any());
    }

    @Test
    void shouldReturnSuccessIdempotentlyWhenPostAlreadyDeleted() {
        UUID authorId = UUID.randomUUID();
        String postId = "507f1f77bcf86cd799439011";
        Instant deletedAt = Instant.parse("2026-05-19T08:00:00Z");
        Post existing = buildPost(authorId, postId, PostStatus.DELETED, deletedAt);

        when(userProjectionRepository.findByUserId(authorId)).thenReturn(Optional.empty());
        when(postRepository.findById(postId)).thenReturn(Optional.of(existing));

        DeletePostResult result = useCase.execute(new DeletePostCommand(authorId, List.of(), postId));

        verify(postRepository, never()).save(any());
        assertThat(result.status()).isEqualTo("DELETED");
        assertThat(result.deletedAt()).isEqualTo(deletedAt.toString());
    }

    @Test
    void shouldThrowNotFoundWhenPostDoesNotExist() {
        UUID authorId = UUID.randomUUID();
        when(userProjectionRepository.findByUserId(authorId)).thenReturn(Optional.empty());
        when(postRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(new DeletePostCommand(authorId, List.of(), "missing")))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.RESOURCE_NOT_FOUND));
    }

    @Test
    void shouldThrowForbiddenWhenActorIsNeitherAuthorNorModerator() {
        UUID authorId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();
        String postId = "507f1f77bcf86cd799439011";

        when(userProjectionRepository.findByUserId(otherUserId)).thenReturn(Optional.empty());
        when(postRepository.findById(postId)).thenReturn(Optional.of(buildPost(authorId, postId, PostStatus.ACTIVE, null)));

        assertThatThrownBy(() -> useCase.execute(new DeletePostCommand(otherUserId, List.of("USER"), postId)))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN));
    }

    @Test
    void shouldThrowForbiddenWhenUserIsSuspended() {
        UUID authorId = UUID.randomUUID();
        when(userProjectionRepository.findByUserId(authorId))
                .thenReturn(Optional.of(new UserProjection(authorId.toString(), "SUSPENDED", "User", null)));

        assertThatThrownBy(() -> useCase.execute(new DeletePostCommand(authorId, List.of(), "post-id")))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.ACCOUNT_SUSPENDED));
    }
}
