package com.twohands.social_service.unit.application.feed.recommendposts;

import com.twohands.social_service.application.feed.recommendposts.PostFeatureBuilder;
import com.twohands.social_service.domain.follow.FollowRepository;
import com.twohands.social_service.domain.integration.UserProductAffinity;
import com.twohands.social_service.domain.integration.UserProductAffinityClient;
import com.twohands.social_service.domain.post.*;
import com.twohands.social_service.domain.search.SearchHistoryRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PostFeatureBuilderTest {

    private final PostRepository postRepository = mock(PostRepository.class);
    private final PostLikeRepository postLikeRepository = mock(PostLikeRepository.class);
    private final PostSaveRepository postSaveRepository = mock(PostSaveRepository.class);
    private final FollowRepository followRepository = mock(FollowRepository.class);
    private final SearchHistoryRepository searchHistoryRepository = mock(SearchHistoryRepository.class);
    private final UserProductAffinityClient userProductAffinityClient = mock(UserProductAffinityClient.class);

    private final PostFeatureBuilder featureBuilder = new PostFeatureBuilder(
            postRepository, postLikeRepository, postSaveRepository, followRepository, searchHistoryRepository,
            userProductAffinityClient
    );

    @Test
    void shouldBuildFeatureVectorsWithCorrectNormalizationAndCalculations() {
        UUID userId = UUID.randomUUID();
        UUID authorId1 = UUID.randomUUID();
        UUID authorId2 = UUID.randomUUID();
        UUID commonFollowee = UUID.randomUUID();

        // 1. Mock Following
        when(followRepository.findAcceptedFolloweeIds(userId)).thenReturn(List.of(authorId1, commonFollowee));
        // Direct follow check will return true for authorId1, false for authorId2
        when(followRepository.findAcceptedFolloweeIds(authorId2)).thenReturn(List.of(commonFollowee));

        // 2. Mock Likes & Saves Interactions
        String likedPostId = "likedPost1";
        String savedPostId = "savedPost1";
        when(postLikeRepository.findRecentLikedPostIds(userId, 50)).thenReturn(List.of(likedPostId));
        
        PageResult<PostSaveEntry> mockSavedPage = new PageResult<>(
                List.of(new PostSaveEntry(savedPostId, Instant.now())), 0, 50, 1, 1, false
        );
        when(postSaveRepository.findByUserId(userId, 0, 50)).thenReturn(mockSavedPage);

        // Mock interaction posts details to retrieve hashtags & authorIds
        Post likedPost = createMockPost(likedPostId, authorId2.toString(), List.of("fashion", "shoes"));
        Post savedPost = createMockPost(savedPostId, authorId1.toString(), List.of("shoes", "soccer"));
        when(postRepository.findByIds(any())).thenReturn(List.of(likedPost, savedPost));

        // 3. Mock Search Keywords
        when(searchHistoryRepository.findRecentKeywordsByUserId(userId, 20)).thenReturn(List.of("Soccer", "sports"));
        when(userProductAffinityClient.findByUserId(userId)).thenReturn(UserProductAffinity.empty());

        // 4. Candidates input
        Instant now = Instant.now();
        PostCandidate cand1 = new PostCandidate(
                "cand1",
                authorId1.toString(),
                now.minus(1, ChronoUnit.HOURS),
                List.of("soccer", "sports"), // Matches soccer (search keyword + save), sports (search keyword)
                List.of(),
                10, // raw engagement
                2
        );

        PostCandidate cand2 = new PostCandidate(
                "cand2",
                authorId2.toString(),
                now.minus(3, ChronoUnit.DAYS),
                List.of("fashion"), // Matches fashion (liked post tag)
                List.of(),
                100, // raw engagement (higher than cand1)
                10
        );

        List<PostFeatureVector> vectors = featureBuilder.buildFeatureVectors(userId, List.of(cand1, cand2));

        assertThat(vectors).hasSize(2);

        // Verify Recency Score (cand1 newer, score closer to 1.0; cand2 older, decayed)
        assertThat(vectors.get(0).recencyScore()).isGreaterThan(vectors.get(1).recencyScore());
        assertThat(vectors.get(0).recencyScore()).isCloseTo(1.0, offset(0.1));

        // Verify Engagement Score (Normalized: cand2 is max -> 1.0, cand1 is min -> 0.0)
        assertThat(vectors.get(1).engagementScore()).isEqualTo(1.0);
        assertThat(vectors.get(0).engagementScore()).isEqualTo(0.0);

        // Verify Hashtag Match Score (cand1 matches search "soccer","sports" + save "soccer" -> high score -> 1.0; cand2 matches liked "fashion" -> low score -> 0.0)
        assertThat(vectors.get(0).hashtagMatchScore()).isEqualTo(1.0);
        assertThat(vectors.get(1).hashtagMatchScore()).isEqualTo(0.0);

        // Verify Author Affinity Score (cand1 author followed -> high -> 1.0, cand2 author not followed but liked 1 post -> low -> 0.0)
        assertThat(vectors.get(0).authorAffinityScore()).isEqualTo(1.0);
        assertThat(vectors.get(1).authorAffinityScore()).isEqualTo(0.0);

        // Verify Mutual Follow Score (cand1 author is directly followed -> 1.0)
        assertThat(vectors.get(0).mutualFollowScore()).isEqualTo(1.0);

        // Verify Jaccard on following for cand2 (user follows [authorId1, commonFollowee], authorId2 follows [commonFollowee])
        // Intersection = [commonFollowee] (size 1)
        // Union = [authorId1, commonFollowee] (size 2)
        // Jaccard = 1 / 2 = 0.5
        assertThat(vectors.get(1).mutualFollowScore()).isEqualTo(0.5);

        // Verify Cross Domain Product Score is always 0.0
        assertThat(vectors.get(0).crossDomainProductScore()).isEqualTo(0.0);
        assertThat(vectors.get(1).crossDomainProductScore()).isEqualTo(0.0);
    }

    private Post createMockPost(String id, String authorId, List<String> hashtags) {
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
                hashtags,
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
