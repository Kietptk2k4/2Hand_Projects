package com.twohands.social_service.unit.application.feed.recommendposts;

import com.twohands.social_service.application.feed.recommendposts.PostImpressionLogger;
import com.twohands.social_service.application.feed.recommendposts.PostFeatureBuilder;
import com.twohands.social_service.application.feed.recommendposts.RecommendPostsUseCase;
import com.twohands.social_service.application.feed.viewglobalfeed.ViewGlobalFeedResult;
import com.twohands.social_service.domain.post.*;
import com.twohands.social_service.exception.AppException;
import com.twohands.social_service.exception.ErrorCode;
import com.twohands.social_service.infrastructure.model.LightGBMRankingModel;
import com.twohands.social_service.infrastructure.model.ModelLoader;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

class RecommendPostsUseCaseTest {

    private final CandidatePoolService candidatePoolService = mock(CandidatePoolService.class);
    private final PostFeatureBuilder postFeatureBuilder = mock(PostFeatureBuilder.class);
    private final RuleBasedRankingModel ruleBasedRankingModel = mock(RuleBasedRankingModel.class);
    private final LightGBMRankingModel lightGbmRankingModel = mock(LightGBMRankingModel.class);
    private final ModelLoader modelLoader = mock(ModelLoader.class);
    private final PostRepository postRepository = mock(PostRepository.class);
    private final PostLikeRepository postLikeRepository = mock(PostLikeRepository.class);
    private final PostImpressionLogger postImpressionLogger = mock(PostImpressionLogger.class);

    private final RecommendPostsUseCase useCase = new RecommendPostsUseCase(
            candidatePoolService, postFeatureBuilder, ruleBasedRankingModel, lightGbmRankingModel, modelLoader, postRepository, postLikeRepository, postImpressionLogger
    );

    @Test
    void shouldThrowUnauthorizedWhenUserIdIsNull() {
        assertThatThrownBy(() -> useCase.execute(null, 0, 10))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.UNAUTHORIZED));
    }

    @Test
    void shouldThrowInvalidPaginationWhenPageIsNegative() {
        UUID userId = UUID.randomUUID();
        assertThatThrownBy(() -> useCase.execute(userId, -1, 10))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> {
                    AppException appEx = (AppException) ex;
                    assertThat(appEx.getErrorCode()).isEqualTo(ErrorCode.INVALID_PAGINATION);
                    assertThat(appEx.getField()).isEqualTo("page");
                });
    }

    @Test
    void shouldReturnEmptyResultWhenNoCandidatesFound() {
        UUID userId = UUID.randomUUID();
        when(candidatePoolService.getCandidates(userId, 500)).thenReturn(List.of());

        ViewGlobalFeedResult result = useCase.execute(userId, 0, 10);

        assertThat(result.items()).isEmpty();
        assertThat(result.meta().totalElements()).isZero();
    }

    @Test
    void shouldRecommendSortAndPaginateCandidatesCorrectly() {
        UUID userId = UUID.randomUUID();

        // 1. Mock Candidates
        PostCandidate cand1 = new PostCandidate("post1", "auth1", Instant.now(), List.of(), List.of(), 10, 2);
        PostCandidate cand2 = new PostCandidate("post2", "auth2", Instant.now(), List.of(), List.of(), 20, 5);
        PostCandidate cand3 = new PostCandidate("post3", "auth3", Instant.now(), List.of(), List.of(), 5, 0);
        when(candidatePoolService.getCandidates(userId, 500)).thenReturn(List.of(cand1, cand2, cand3));

        // 2. Mock Features
        PostFeatureVector f1 = new PostFeatureVector(1, 0.5, 0.2, 0.1, 0.0, 0.0);
        PostFeatureVector f2 = new PostFeatureVector(0.8, 0.9, 0.8, 0.5, 1.0, 0.0);
        PostFeatureVector f3 = new PostFeatureVector(0.5, 0.1, 0.0, 0.0, 0.0, 0.0);
        when(postFeatureBuilder.buildFeatureVectors(userId, List.of(cand1, cand2, cand3)))
                .thenReturn(List.of(f1, f2, f3));

        // 3. Mock model session to null for rule-based fallback and predictBatch results
        when(modelLoader.getSession()).thenReturn(null);
        when(ruleBasedRankingModel.predictBatch(any())).thenReturn(List.of(0.55, 0.85, 0.15));

        // 4. Mock Post Entities database fetch
        Post post1 = createMockPost("post1", "auth1");
        Post post2 = createMockPost("post2", "auth2");
        when(postRepository.findByIds(any())).thenReturn(List.of(post1, post2));

        // 5. Mock Like status
        when(postLikeRepository.findLikedPostIdsByUserIdAndPostIds(eq(userId), any())).thenReturn(Set.of("post2"));

        // Execute Page 0, Size 2 (should return Rank 1 and 2: post2 and post1)
        ViewGlobalFeedResult result = useCase.execute(userId, 0, 2);

        // Verify ordering and details
        assertThat(result.items()).hasSize(2);
        assertThat(result.items().get(0).postId()).isEqualTo("post2"); // highest score first
        assertThat(result.items().get(0).likedByMe()).isTrue();
        assertThat(result.items().get(1).postId()).isEqualTo("post1"); // second highest score
        assertThat(result.items().get(1).likedByMe()).isFalse();

        // Verify meta
        assertThat(result.meta().totalElements()).isEqualTo(3);
        assertThat(result.meta().totalPages()).isEqualTo(2);
        assertThat(result.meta().hasNext()).isTrue();

        // Verify Async impression logger was triggered with page item IDs
        ArgumentCaptor<List> listCaptor = ArgumentCaptor.forClass(List.class);
        verify(postImpressionLogger).logImpressions(
                eq(userId),
                listCaptor.capture(),
                any(),
                isNull(),
                isNull(),
                any()
        );
        assertThat(listCaptor.getValue()).containsExactly("post2", "post1");
    }

    private Post createMockPost(String id, String authorId) {
        return new Post(
                id,
                authorId,
                "Caption",
                List.of(),
                List.of(),
                PostStatus.ACTIVE,
                PostVisibility.PUBLIC,
                0,
                0,
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
}
