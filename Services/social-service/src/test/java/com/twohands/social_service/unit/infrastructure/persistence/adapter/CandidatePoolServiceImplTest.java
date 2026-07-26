package com.twohands.social_service.unit.infrastructure.persistence.adapter;

import com.twohands.social_service.domain.follow.FollowRepository;
import com.twohands.social_service.domain.post.PostCandidate;
import com.twohands.social_service.domain.post.UserSeenPostsRepository;
import com.twohands.social_service.infrastructure.persistence.adapter.CandidatePoolServiceImpl;
import com.twohands.social_service.infrastructure.persistence.mongo.document.PostDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class CandidatePoolServiceImplTest {

    private final MongoTemplate mongoTemplate = mock(MongoTemplate.class);
    private final FollowRepository followRepository = mock(FollowRepository.class);
    private final UserSeenPostsRepository userSeenPostsRepository = mock(UserSeenPostsRepository.class);
    private CandidatePoolServiceImpl candidatePoolService;

    @BeforeEach
    void setUp() {
        candidatePoolService =
                new CandidatePoolServiceImpl(mongoTemplate, followRepository, userSeenPostsRepository);
        ReflectionTestUtils.setField(candidatePoolService, "minPoolSize", 20);
        ReflectionTestUtils.setField(candidatePoolService, "windowDays", List.of(7, 30, 90));
        reset(mongoTemplate, followRepository, userSeenPostsRepository);
    }

    @Test
    void parseWindowDays_defaultsWhenBlank() {
        assertThat(CandidatePoolServiceImpl.parseWindowDays(null)).containsExactly(7, 30, 90);
        assertThat(CandidatePoolServiceImpl.parseWindowDays("  ")).containsExactly(7, 30, 90);
        assertThat(CandidatePoolServiceImpl.parseWindowDays("7,30,90")).containsExactly(7, 30, 90);
    }

    @Test
    void shouldReturnEmptyListWhenUserIdIsNull() {
        List<PostCandidate> candidates = candidatePoolService.getCandidates(null, 500);
        assertThat(candidates).isEmpty();
        verifyNoInteractions(mongoTemplate, followRepository);
    }

    @Test
    void shouldFetchCandidatesCorrectlyIncludingFolloweesAndPublicPool() {
        ReflectionTestUtils.setField(candidatePoolService, "minPoolSize", 3);

        UUID userId = UUID.randomUUID();
        UUID followeeId1 = UUID.randomUUID();
        UUID followeeId2 = UUID.randomUUID();
        when(followRepository.findAcceptedFolloweeIds(userId)).thenReturn(List.of(followeeId1, followeeId2));
        when(userSeenPostsRepository.findSeenPostIds(userId)).thenReturn(Set.of());

        PostDocument doc1 = createMockPostDocument("post1", followeeId1.toString(), "caption 1", 10, 2);
        PostDocument doc2 = createMockPostDocument("post2", followeeId2.toString(), "caption 2", 20, 5);
        PostDocument doc3 = createMockPostDocument("post3", UUID.randomUUID().toString(), "caption 3", 5, 0);

        when(mongoTemplate.find(any(Query.class), eq(PostDocument.class)))
                .thenReturn(List.of(doc1, doc2))
                .thenReturn(List.of(doc1, doc2, doc3));

        List<PostCandidate> candidates = candidatePoolService.getCandidates(userId, 500);

        ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate, times(2)).find(queryCaptor.capture(), eq(PostDocument.class));

        List<Query> queries = queryCaptor.getAllValues();
        assertThat(queries).hasSize(2);
        assertThat(queries.get(0).getQueryObject().toString()).contains("author_id");
        assertThat(queries.get(1).getQueryObject().toString()).doesNotContain("author_id");

        assertThat(candidates).hasSize(3);
        assertThat(candidates.get(0).postId()).isEqualTo("post1");
        assertThat(candidates.get(0).authorId()).isEqualTo(followeeId1.toString());
        assertThat(candidates.get(0).likeCount()).isEqualTo(10);
        assertThat(candidates.get(0).commentCount()).isEqualTo(2);
        assertThat(candidates.get(1).postId()).isEqualTo("post2");
        assertThat(candidates.get(2).postId()).isEqualTo("post3");
    }

    @Test
    void shouldRespectMaxLimitOfCandidates() {
        ReflectionTestUtils.setField(candidatePoolService, "minPoolSize", 1);

        UUID userId = UUID.randomUUID();
        when(followRepository.findAcceptedFolloweeIds(userId)).thenReturn(List.of());
        when(userSeenPostsRepository.findSeenPostIds(userId)).thenReturn(Set.of());

        PostDocument doc1 = createMockPostDocument("post1", UUID.randomUUID().toString(), "c1", 1, 1);
        PostDocument doc2 = createMockPostDocument("post2", UUID.randomUUID().toString(), "c2", 2, 2);
        PostDocument doc3 = createMockPostDocument("post3", UUID.randomUUID().toString(), "c3", 3, 3);

        when(mongoTemplate.find(any(Query.class), eq(PostDocument.class)))
                .thenReturn(List.of(doc1, doc2, doc3));

        List<PostCandidate> candidates = candidatePoolService.getCandidates(userId, 2);

        assertThat(candidates).hasSize(2);
        assertThat(candidates.get(0).postId()).isEqualTo("post1");
        assertThat(candidates.get(1).postId()).isEqualTo("post2");
        verify(mongoTemplate, times(1)).find(any(Query.class), eq(PostDocument.class));
    }

    @Test
    void shouldStopAtSevenDayWindowWhenPoolMeetsMinSize() {
        ReflectionTestUtils.setField(candidatePoolService, "minPoolSize", 20);

        UUID userId = UUID.randomUUID();
        when(followRepository.findAcceptedFolloweeIds(userId)).thenReturn(List.of());
        when(userSeenPostsRepository.findSeenPostIds(userId)).thenReturn(Set.of());

        List<PostDocument> twenty = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            twenty.add(createMockPostDocument("p" + i, UUID.randomUUID().toString(), "c", 1, 0));
        }
        when(mongoTemplate.find(any(Query.class), eq(PostDocument.class))).thenReturn(twenty);

        List<PostCandidate> candidates = candidatePoolService.getCandidates(userId, 500);

        assertThat(candidates).hasSize(20);
        verify(mongoTemplate, times(1)).find(any(Query.class), eq(PostDocument.class));
    }

    @Test
    void shouldFallbackToThirtyDaysWhenSevenDayPoolBelowMin() {
        ReflectionTestUtils.setField(candidatePoolService, "minPoolSize", 20);

        UUID userId = UUID.randomUUID();
        when(followRepository.findAcceptedFolloweeIds(userId)).thenReturn(List.of());
        when(userSeenPostsRepository.findSeenPostIds(userId)).thenReturn(Set.of());

        List<PostDocument> few = List.of(
                createMockPostDocument("a", UUID.randomUUID().toString(), "c", 1, 0)
        );
        List<PostDocument> thirty = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            thirty.add(createMockPostDocument("w30-" + i, UUID.randomUUID().toString(), "c", 1, 0));
        }

        AtomicInteger calls = new AtomicInteger();
        when(mongoTemplate.find(any(Query.class), eq(PostDocument.class))).thenAnswer(inv -> {
            int n = calls.getAndIncrement();
            return n == 0 ? few : thirty;
        });

        List<PostCandidate> candidates = candidatePoolService.getCandidates(userId, 500);

        assertThat(candidates).hasSize(20);
        assertThat(candidates.get(0).postId()).startsWith("w30-");
        verify(mongoTemplate, times(2)).find(any(Query.class), eq(PostDocument.class));
    }

    @Test
    void shouldUseNinetyDayWindowWhenNarrowerWindowsBelowMin() {
        ReflectionTestUtils.setField(candidatePoolService, "minPoolSize", 20);

        UUID userId = UUID.randomUUID();
        when(followRepository.findAcceptedFolloweeIds(userId)).thenReturn(List.of());
        when(userSeenPostsRepository.findSeenPostIds(userId)).thenReturn(Set.of());

        List<PostDocument> empty = List.of();
        List<PostDocument> few = List.of(
                createMockPostDocument("only", UUID.randomUUID().toString(), "c", 1, 0)
        );

        AtomicInteger calls = new AtomicInteger();
        when(mongoTemplate.find(any(Query.class), eq(PostDocument.class))).thenAnswer(inv -> {
            int n = calls.getAndIncrement();
            if (n <= 1) {
                return empty;
            }
            return few;
        });

        List<PostCandidate> candidates = candidatePoolService.getCandidates(userId, 500);

        assertThat(candidates).hasSize(1);
        assertThat(candidates.get(0).postId()).isEqualTo("only");
        verify(mongoTemplate, times(3)).find(any(Query.class), eq(PostDocument.class));
    }

    @Test
    void shouldReturnEmptyWhenWidestWindowHasNoUnseenPosts() {
        ReflectionTestUtils.setField(candidatePoolService, "minPoolSize", 20);

        UUID userId = UUID.randomUUID();
        when(followRepository.findAcceptedFolloweeIds(userId)).thenReturn(List.of());
        when(userSeenPostsRepository.findSeenPostIds(userId)).thenReturn(Set.of());
        when(mongoTemplate.find(any(Query.class), eq(PostDocument.class))).thenReturn(List.of());

        List<PostCandidate> candidates = candidatePoolService.getCandidates(userId, 500);

        assertThat(candidates).isEmpty();
        verify(mongoTemplate, times(3)).find(any(Query.class), eq(PostDocument.class));
    }

    @Test
    void shouldExcludeSeenPostsEvenWhenOnlyPresentInWiderWindow() {
        ReflectionTestUtils.setField(candidatePoolService, "minPoolSize", 20);

        UUID userId = UUID.randomUUID();
        when(followRepository.findAcceptedFolloweeIds(userId)).thenReturn(List.of());
        when(userSeenPostsRepository.findSeenPostIds(userId)).thenReturn(Set.of("seen-old"));

        PostDocument seen = createMockPostDocument("seen-old", UUID.randomUUID().toString(), "old", 1, 0);
        PostDocument fresh = createMockPostDocument("fresh", UUID.randomUUID().toString(), "new", 1, 0);

        AtomicInteger calls = new AtomicInteger();
        when(mongoTemplate.find(any(Query.class), eq(PostDocument.class))).thenAnswer(inv -> {
            int n = calls.getAndIncrement();
            if (n == 0) {
                return List.of();
            }
            if (n == 1) {
                return List.of(seen);
            }
            return List.of(seen, fresh);
        });

        List<PostCandidate> candidates = candidatePoolService.getCandidates(userId, 500);

        assertThat(candidates).extracting(PostCandidate::postId).containsExactly("fresh");
        assertThat(candidates).noneMatch(c -> c.postId().equals("seen-old"));
        verify(mongoTemplate, times(3)).find(any(Query.class), eq(PostDocument.class));
    }

    private PostDocument createMockPostDocument(String id, String authorId, String caption, long likes, long comments) {
        PostDocument doc = new PostDocument();
        doc.setId(id);
        doc.setAuthorId(authorId);
        doc.setCaption(caption);
        doc.setStatus("ACTIVE");
        doc.setVisibility("PUBLIC");
        doc.setLikeCount(likes);
        doc.setReplyCount(comments);
        doc.setCreatedAt(Instant.now());
        doc.setHashtags(List.of("tag1"));

        PostDocument.ProductTagDocument ptDoc = new PostDocument.ProductTagDocument();
        ptDoc.setProductId("prod1");
        ptDoc.setPrice(new BigDecimal("100000"));
        ptDoc.setName("Product 1");
        ptDoc.setCategory("Cat1");
        ptDoc.setAvailable(true);
        doc.setProductTags(List.of(ptDoc));
        return doc;
    }
}
