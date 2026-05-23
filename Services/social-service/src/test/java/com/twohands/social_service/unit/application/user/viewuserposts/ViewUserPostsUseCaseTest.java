package com.twohands.social_service.unit.application.user.viewuserposts;

import com.twohands.social_service.application.user.viewuserposts.ViewUserPostsResult;
import com.twohands.social_service.application.user.viewuserposts.ViewUserPostsUseCase;
import com.twohands.social_service.domain.follow.Follow;
import com.twohands.social_service.domain.follow.FollowRepository;
import com.twohands.social_service.domain.follow.FollowStatus;
import com.twohands.social_service.domain.post.AuthorPostsQuery;
import com.twohands.social_service.domain.post.AuthorPostsScope;
import com.twohands.social_service.domain.post.PageResult;
import com.twohands.social_service.domain.post.Post;
import com.twohands.social_service.domain.post.PostRepository;
import com.twohands.social_service.domain.post.PostStatus;
import com.twohands.social_service.domain.post.PostVisibility;
import com.twohands.social_service.domain.post.ProductTag;
import com.twohands.social_service.domain.user.UserProjection;
import com.twohands.social_service.domain.user.UserProjectionRepository;
import com.twohands.social_service.exception.AppException;
import com.twohands.social_service.exception.ErrorCode;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ViewUserPostsUseCaseTest {

    private final PostRepository postRepository = org.mockito.Mockito.mock(PostRepository.class);
    private final UserProjectionRepository userProjectionRepository = org.mockito.Mockito.mock(UserProjectionRepository.class);
    private final FollowRepository followRepository = org.mockito.Mockito.mock(FollowRepository.class);
    private final ViewUserPostsUseCase useCase = new ViewUserPostsUseCase(
            postRepository,
            userProjectionRepository,
            followRepository
    );

    @Test
    void shouldReturnPublicPostsForPublicProfileViewer() {
        UUID viewerId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        Post publicPost = buildPost("507f1f77bcf86cd799439011", targetId.toString(), PostStatus.ACTIVE, PostVisibility.PUBLIC);

        when(userProjectionRepository.findByUserId(targetId))
                .thenReturn(Optional.of(activeProjection(targetId, false)));
        when(followRepository.findByFollowerIdAndFolloweeId(viewerId, targetId)).thenReturn(Optional.empty());
        when(postRepository.findAuthorPosts(eq(new AuthorPostsQuery(
                targetId.toString(),
                AuthorPostsScope.VIEWER_PUBLIC_ONLY,
                0,
                20
        )))).thenReturn(new PageResult<>(List.of(publicPost), 0, 20, 1, 1, false));

        ViewUserPostsResult result = useCase.execute(viewerId, targetId, 0, 20, "published");

        assertThat(result.items()).hasSize(1);
        assertThat(result.items().getFirst().postId()).isEqualTo(publicPost.id());
        assertThat(result.items().getFirst().visibility()).isEqualTo("PUBLIC");
    }

    @Test
    void shouldReturnFollowersPostsWhenViewerHasAcceptedFollow() {
        UUID viewerId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        Post followersPost = buildPost("507f1f77bcf86cd799439012", targetId.toString(), PostStatus.ACTIVE, PostVisibility.FOLLOWERS);

        when(userProjectionRepository.findByUserId(targetId))
                .thenReturn(Optional.of(activeProjection(targetId, true)));
        when(followRepository.findByFollowerIdAndFolloweeId(viewerId, targetId))
                .thenReturn(Optional.of(new Follow(viewerId, targetId, FollowStatus.ACCEPTED, Instant.now())));
        when(postRepository.findAuthorPosts(eq(new AuthorPostsQuery(
                targetId.toString(),
                AuthorPostsScope.VIEWER_AS_FOLLOWER,
                0,
                20
        )))).thenReturn(new PageResult<>(List.of(followersPost), 0, 20, 1, 1, false));

        ViewUserPostsResult result = useCase.execute(viewerId, targetId, 0, 20, "published");

        assertThat(result.items()).hasSize(1);
        assertThat(result.items().getFirst().visibility()).isEqualTo("FOLLOWERS");
    }

    @Test
    void shouldThrowForbiddenForPrivateProfileWithoutFollow() {
        UUID viewerId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();

        when(userProjectionRepository.findByUserId(targetId))
                .thenReturn(Optional.of(activeProjection(targetId, true)));
        when(followRepository.findByFollowerIdAndFolloweeId(viewerId, targetId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(viewerId, targetId, 0, 20, "published"))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN));
    }

    @Test
    void shouldReturnDraftPostsForOwnerWithAllFilter() {
        UUID ownerId = UUID.randomUUID();
        Post draftPost = buildPost("507f1f77bcf86cd799439013", ownerId.toString(), PostStatus.DRAFT, PostVisibility.PUBLIC);

        when(userProjectionRepository.findByUserId(ownerId))
                .thenReturn(Optional.of(activeProjection(ownerId, false)));
        when(postRepository.findAuthorPosts(eq(new AuthorPostsQuery(
                ownerId.toString(),
                AuthorPostsScope.OWNER_ALL,
                0,
                20
        )))).thenReturn(new PageResult<>(List.of(draftPost), 0, 20, 1, 1, false));

        ViewUserPostsResult result = useCase.execute(ownerId, ownerId, 0, 20, "all");

        assertThat(result.items()).hasSize(1);
        verify(postRepository).findAuthorPosts(new AuthorPostsQuery(
                ownerId.toString(),
                AuthorPostsScope.OWNER_ALL,
                0,
                20
        ));
    }

    @Test
    void shouldRejectAllFilterForNonOwner() {
        UUID viewerId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();

        when(userProjectionRepository.findByUserId(targetId))
                .thenReturn(Optional.of(activeProjection(targetId, false)));

        assertThatThrownBy(() -> useCase.execute(viewerId, targetId, 0, 20, "all"))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.INVALID_PAGINATION));
    }

    @Test
    void shouldThrowNotFoundWhenUserMissing() {
        UUID viewerId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();

        when(userProjectionRepository.findByUserId(targetId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(viewerId, targetId, 0, 20, "published"))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.RESOURCE_NOT_FOUND));
    }

    @Test
    void shouldThrowNotFoundWhenUserDeleted() {
        UUID viewerId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();

        when(userProjectionRepository.findByUserId(targetId))
                .thenReturn(Optional.of(new UserProjection(targetId.toString(), "DELETED", "User", null, false)));

        assertThatThrownBy(() -> useCase.execute(viewerId, targetId, 0, 20, "published"))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.RESOURCE_NOT_FOUND));
    }

    private UserProjection activeProjection(UUID userId, boolean isPrivate) {
        return new UserProjection(userId.toString(), "ACTIVE", "User", "https://avatar", isPrivate);
    }

    private Post buildPost(String postId, String authorId, PostStatus status, PostVisibility visibility) {
        Instant now = Instant.parse("2026-05-21T09:00:00Z");
        return new Post(
                postId,
                authorId,
                "caption",
                List.of(),
                List.of(new ProductTag("product-1", new BigDecimal("100000"))),
                status,
                visibility,
                5L,
                1L,
                List.of(),
                true,
                now,
                now,
                null
        );
    }
}
