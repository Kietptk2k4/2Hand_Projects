package com.twohands.social_service.unit.application.search.searchpost;

import com.twohands.social_service.application.user.common.UserWriteGuard;
import com.twohands.social_service.application.search.searchpost.SearchPostCommand;
import com.twohands.social_service.domain.user.UserProjectionRepository;
import com.twohands.social_service.testsupport.UserProjectionTestFixtures;
import com.twohands.social_service.application.search.searchpost.SearchPostResult;
import com.twohands.social_service.application.search.searchpost.SearchPostUseCase;
import com.twohands.social_service.domain.follow.FollowRepository;
import com.twohands.social_service.domain.post.MediaItem;
import com.twohands.social_service.domain.post.PageResult;
import com.twohands.social_service.domain.post.Post;
import com.twohands.social_service.domain.post.PostModerationStatus;
import com.twohands.social_service.domain.post.PostRepository;
import com.twohands.social_service.domain.post.PostSearchQuery;
import com.twohands.social_service.domain.post.PostStatus;
import com.twohands.social_service.domain.post.PostVisibility;
import com.twohands.social_service.domain.search.SearchHistoryRepository;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SearchPostUseCaseTest {

    private final PostRepository postRepository = mock(PostRepository.class);
    private final FollowRepository followRepository = mock(FollowRepository.class);
    private final SearchHistoryRepository searchHistoryRepository = mock(SearchHistoryRepository.class);
    private final UserProjectionRepository userProjectionRepository = mock(UserProjectionRepository.class);
    private final UserWriteGuard userWriteGuard = new UserWriteGuard(userProjectionRepository);
    private final SearchPostUseCase useCase = new SearchPostUseCase(
            postRepository,
            followRepository,
            searchHistoryRepository,
            userWriteGuard
    );

    private Post buildPost(String postId) {
        return new Post(
                postId,
                UUID.randomUUID().toString(),
                "caption travel",
                List.of(new MediaItem("https://cdn/1.jpg", "IMAGE")),
                List.of(),
                PostStatus.ACTIVE,
                PostVisibility.PUBLIC,
                1L,
                0L,
                List.of("travel"),
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
    void shouldSearchPostsAndSaveHistory() {
        UUID userId = UUID.randomUUID();
        UUID followeeId = UUID.randomUUID();
        Post post = buildPost("507f1f77bcf86cd799439011");

        when(userProjectionRepository.findByUserId(userId)).thenReturn(UserProjectionTestFixtures.activeOptional(userId));
        when(followRepository.findAcceptedFolloweeIds(userId)).thenReturn(List.of(followeeId));
        when(postRepository.searchPosts(any(PostSearchQuery.class), any()))
                .thenReturn(new PageResult<>(List.of(post), 0, 20, 1, 1, false));

        SearchPostResult result = useCase.execute(new SearchPostCommand(userId, "travel", 0, 20));

        ArgumentCaptor<PostSearchQuery> queryCaptor = ArgumentCaptor.forClass(PostSearchQuery.class);
        verify(postRepository).searchPosts(queryCaptor.capture(), eq(List.of(followeeId.toString())));
        assertThat(queryCaptor.getValue().keyword()).contains("travel");
        verify(searchHistoryRepository).saveOrRefresh(userId, "travel");
        assertThat(result.keyword()).isEqualTo("travel");
        assertThat(result.items()).hasSize(1);
    }

    @Test
    void shouldStillReturnResultsWhenHistorySaveFails() {
        UUID userId = UUID.randomUUID();
        when(followRepository.findAcceptedFolloweeIds(userId)).thenReturn(List.of());
        when(postRepository.searchPosts(any(), any()))
                .thenReturn(new PageResult<>(List.of(), 0, 20, 0, 0, false));
        doThrow(new RuntimeException("db down")).when(searchHistoryRepository).saveOrRefresh(userId, "food");

        SearchPostResult result = useCase.execute(new SearchPostCommand(userId, "food", 0, 20));

        assertThat(result.keyword()).isEqualTo("food");
    }

    @Test
    void shouldSkipSearchHistoryWhenUserCannotWrite() {
        UUID userId = UUID.randomUUID();
        when(userProjectionRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(followRepository.findAcceptedFolloweeIds(userId)).thenReturn(List.of());
        when(postRepository.searchPosts(any(), any()))
                .thenReturn(new PageResult<>(List.of(), 0, 20, 0, 0, false));

        SearchPostResult result = useCase.execute(new SearchPostCommand(userId, "food", 0, 20));

        assertThat(result.keyword()).isEqualTo("food");
        verify(searchHistoryRepository, never()).saveOrRefresh(any(), any());
    }

    @Test
    void shouldThrowBadRequestWhenKeywordIsBlank() {
        UUID userId = UUID.randomUUID();

        assertThatThrownBy(() -> useCase.execute(new SearchPostCommand(userId, "  ", 0, 20)))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.BAD_REQUEST));
    }
}
