package com.twohands.social_service.unit.application.post.viewsavedposts;

import com.twohands.social_service.application.post.common.PostViewAccessPolicy;
import com.twohands.social_service.application.post.viewsavedposts.ViewSavedPostsResult;
import com.twohands.social_service.application.post.viewsavedposts.ViewSavedPostsUseCase;
import com.twohands.social_service.domain.follow.FollowRepository;
import com.twohands.social_service.domain.post.MediaItem;
import com.twohands.social_service.domain.post.PageResult;
import com.twohands.social_service.domain.post.Post;
import com.twohands.social_service.domain.post.PostRepository;
import com.twohands.social_service.domain.post.PostSaveEntry;
import com.twohands.social_service.domain.post.PostSaveRepository;
import com.twohands.social_service.domain.post.PostStatus;
import com.twohands.social_service.domain.post.PostVisibility;
import com.twohands.social_service.exception.AppException;
import com.twohands.social_service.exception.ErrorCode;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ViewSavedPostsUseCaseTest {

    private final PostSaveRepository postSaveRepository = org.mockito.Mockito.mock(PostSaveRepository.class);
    private final PostRepository postRepository = org.mockito.Mockito.mock(PostRepository.class);
    private final FollowRepository followRepository = org.mockito.Mockito.mock(FollowRepository.class);
    private final PostViewAccessPolicy postViewAccessPolicy = new PostViewAccessPolicy();
    private final ViewSavedPostsUseCase useCase = new ViewSavedPostsUseCase(
            postSaveRepository,
            postRepository,
            followRepository,
            postViewAccessPolicy
    );

    @Test
    void shouldReturnEmptyListWhenUserHasNoSaves() {
        UUID userId = UUID.randomUUID();
        when(postSaveRepository.findByUserId(userId, 0, 20))
                .thenReturn(new PageResult<>(List.of(), 0, 20, 0, 0, false));

        ViewSavedPostsResult result = useCase.execute(userId, 0, 20);

        assertThat(result.items()).isEmpty();
        assertThat(result.meta().totalElements()).isZero();
    }

    @Test
    void shouldReturnViewableSavedPostsInSaveOrder() {
        UUID userId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        String publicPostId = "507f1f77bcf86cd799439011";
        String deletedPostId = "507f1f77bcf86cd799439012";
        String followersPostId = "507f1f77bcf86cd799439013";

        Instant savedAt = Instant.parse("2026-05-20T08:00:00Z");
        when(postSaveRepository.findByUserId(userId, 0, 20)).thenReturn(new PageResult<>(
                List.of(
                        new PostSaveEntry(publicPostId, savedAt),
                        new PostSaveEntry(deletedPostId, savedAt.plusSeconds(1)),
                        new PostSaveEntry(followersPostId, savedAt.plusSeconds(2))
                ),
                0,
                20,
                3,
                1,
                false
        ));
        when(followRepository.findAcceptedFolloweeIds(userId)).thenReturn(List.of());

        Post publicPost = buildPost(publicPostId, authorId.toString(), PostStatus.ACTIVE, PostVisibility.PUBLIC);
        Post deletedPost = buildPost(deletedPostId, authorId.toString(), PostStatus.DELETED, PostVisibility.PUBLIC);
        Post followersPost = buildPost(followersPostId, authorId.toString(), PostStatus.ACTIVE, PostVisibility.FOLLOWERS);

        when(postRepository.findByIds(List.of(publicPostId, deletedPostId, followersPostId)))
                .thenReturn(List.of(publicPost, deletedPost, followersPost));

        ViewSavedPostsResult result = useCase.execute(userId, 0, 20);

        assertThat(result.items()).hasSize(1);
        assertThat(result.items().getFirst().postId()).isEqualTo(publicPostId);
        assertThat(result.items().getFirst().savedAt()).isEqualTo(savedAt.toString());
        verify(postRepository).findByIds(List.of(publicPostId, deletedPostId, followersPostId));
    }

    @Test
    void shouldIncludeFollowersOnlyPostWhenViewerFollowsAuthor() {
        UUID userId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        String postId = "507f1f77bcf86cd799439011";

        when(postSaveRepository.findByUserId(userId, 0, 20)).thenReturn(new PageResult<>(
                List.of(new PostSaveEntry(postId, Instant.parse("2026-05-20T08:00:00Z"))),
                0,
                20,
                1,
                1,
                false
        ));
        when(followRepository.findAcceptedFolloweeIds(userId)).thenReturn(List.of(authorId));
        when(postRepository.findByIds(any())).thenReturn(List.of(
                buildPost(postId, authorId.toString(), PostStatus.ACTIVE, PostVisibility.FOLLOWERS)
        ));

        ViewSavedPostsResult result = useCase.execute(userId, 0, 20);

        assertThat(result.items()).hasSize(1);
        assertThat(result.items().getFirst().visibility()).isEqualTo("FOLLOWERS");
    }

    @Test
    void shouldThrowUnauthorizedWhenUserMissing() {
        assertThatThrownBy(() -> useCase.execute(null, 0, 20))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.UNAUTHORIZED));
    }

    @Test
    void shouldThrowInvalidPaginationWhenSizeExceedsLimit() {
        UUID userId = UUID.randomUUID();

        assertThatThrownBy(() -> useCase.execute(userId, 0, 51))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> {
                    AppException appException = (AppException) ex;
                    assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.INVALID_PAGINATION);
                    assertThat(appException.getField()).isEqualTo("size");
                });
    }

    private Post buildPost(String postId, String authorId, PostStatus status, PostVisibility visibility) {
        return new Post(
                postId,
                authorId,
                "caption",
                List.of(new MediaItem("https://cdn/1.jpg", "IMAGE")),
                List.of(),
                status,
                visibility,
                5,
                1,
                List.of("tag"),
                true,
                Instant.parse("2026-05-18T10:15:30Z"),
                Instant.parse("2026-05-18T10:20:30Z"),
                status == PostStatus.DELETED ? Instant.parse("2026-05-19T10:00:00Z") : null
        );
    }
}
