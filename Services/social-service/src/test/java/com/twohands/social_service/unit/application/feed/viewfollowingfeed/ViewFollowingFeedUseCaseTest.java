package com.twohands.social_service.unit.application.feed.viewfollowingfeed;

import com.twohands.social_service.application.feed.viewfollowingfeed.ViewFollowingFeedUseCase;
import com.twohands.social_service.application.feed.viewglobalfeed.ViewGlobalFeedResult;
import com.twohands.social_service.domain.follow.FollowRepository;
import com.twohands.social_service.domain.post.FeedQuery;
import com.twohands.social_service.domain.post.MediaItem;
import com.twohands.social_service.domain.post.PageResult;
import com.twohands.social_service.domain.post.Post;
import com.twohands.social_service.domain.post.PostLikeRepository;
import com.twohands.social_service.domain.post.PostModerationStatus;
import com.twohands.social_service.domain.post.PostRepository;
import com.twohands.social_service.domain.post.PostStatus;
import com.twohands.social_service.domain.post.PostVisibility;
import com.twohands.social_service.exception.AppException;
import com.twohands.social_service.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ViewFollowingFeedUseCaseTest {

    private final FollowRepository followRepository = mock(FollowRepository.class);
    private final PostRepository postRepository = mock(PostRepository.class);
    private final PostLikeRepository postLikeRepository = mock(PostLikeRepository.class);
    private final ViewFollowingFeedUseCase useCase = new ViewFollowingFeedUseCase(
            followRepository,
            postRepository,
            postLikeRepository
    );

    @Test
    void shouldReturnFollowingFeedWithMappedItems() {
        UUID userId = UUID.randomUUID();
        UUID followeeId = UUID.randomUUID();
        Post post = new Post(
                "507f1f77bcf86cd799439011",
                followeeId.toString(),
                "caption",
                List.of(new MediaItem("https://cdn/1.jpg", "IMAGE")),
                List.of(),
                PostStatus.ACTIVE,
                PostVisibility.FOLLOWERS,
                10,
                2,
                List.of("java"),
                true,
                PostModerationStatus.NONE,
                null,
                null,
                Instant.parse("2026-05-18T10:15:30Z"),
                Instant.parse("2026-05-18T10:20:30Z"),
                null
        );
        when(followRepository.findAcceptedFolloweeIds(userId)).thenReturn(List.of(followeeId));
        when(postRepository.findFollowingFeed(any(FeedQuery.class), any()))
                .thenReturn(new PageResult<>(List.of(post), 0, 20, 1, 1, false));
        when(postLikeRepository.findLikedPostIdsByUserIdAndPostIds(userId, List.of(post.id())))
                .thenReturn(Set.of(post.id()));

        ViewGlobalFeedResult result = useCase.execute(userId, 0, 20);

        ArgumentCaptor<List<String>> followeeCaptor = ArgumentCaptor.forClass(List.class);
        verify(postRepository).findFollowingFeed(any(FeedQuery.class), followeeCaptor.capture());
        assertThat(followeeCaptor.getValue()).containsExactly(followeeId.toString());
        assertThat(result.items()).hasSize(1);
        assertThat(result.items().getFirst().visibility()).isEqualTo("FOLLOWERS");
        assertThat(result.items().getFirst().likedByMe()).isTrue();
    }

    @Test
    void shouldReturnEmptyFeedWhenUserDoesNotFollowAnyone() {
        UUID userId = UUID.randomUUID();
        when(followRepository.findAcceptedFolloweeIds(userId)).thenReturn(List.of());
        when(postRepository.findFollowingFeed(any(FeedQuery.class), any()))
                .thenReturn(new PageResult<>(List.of(), 0, 20, 0, 0, false));
        when(postLikeRepository.findLikedPostIdsByUserIdAndPostIds(userId, List.of()))
                .thenReturn(Set.of());

        ViewGlobalFeedResult result = useCase.execute(userId, 0, 20);

        assertThat(result.items()).isEmpty();
        assertThat(result.meta().totalElements()).isEqualTo(0);
    }

    @Test
    void shouldThrowInvalidPaginationWhenPageIsNegative() {
        UUID userId = UUID.randomUUID();

        assertThatThrownBy(() -> useCase.execute(userId, -1, 20))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> {
                    AppException appException = (AppException) ex;
                    assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.INVALID_PAGINATION);
                    assertThat(appException.getField()).isEqualTo("page");
                });
    }
}
