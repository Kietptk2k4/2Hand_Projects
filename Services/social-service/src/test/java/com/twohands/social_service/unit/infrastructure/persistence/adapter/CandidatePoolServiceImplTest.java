package com.twohands.social_service.unit.infrastructure.persistence.adapter;

import com.twohands.social_service.domain.follow.FollowRepository;
import com.twohands.social_service.domain.post.PostCandidate;
import com.twohands.social_service.domain.post.ProductTag;
import com.twohands.social_service.infrastructure.persistence.adapter.CandidatePoolServiceImpl;
import com.twohands.social_service.infrastructure.persistence.mongo.document.PostDocument;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class CandidatePoolServiceImplTest {

    private final MongoTemplate mongoTemplate = mock(MongoTemplate.class);
    private final FollowRepository followRepository = mock(FollowRepository.class);
    private final CandidatePoolServiceImpl candidatePoolService = new CandidatePoolServiceImpl(mongoTemplate, followRepository);

    @Test
    void shouldReturnEmptyListWhenUserIdIsNull() {
        List<PostCandidate> candidates = candidatePoolService.getCandidates(null, 500);
        assertThat(candidates).isEmpty();
        verifyNoInteractions(mongoTemplate, followRepository);
    }

    @Test
    void shouldFetchCandidatesCorrectlyIncludingFolloweesAndPublicPool() {
        UUID userId = UUID.randomUUID();
        UUID followeeId1 = UUID.randomUUID();
        UUID followeeId2 = UUID.randomUUID();
        when(followRepository.findAcceptedFolloweeIds(userId)).thenReturn(List.of(followeeId1, followeeId2));

        // Create mock PostDocuments for Followee Source
        PostDocument doc1 = createMockPostDocument("post1", followeeId1.toString(), "caption 1", 10, 2);
        PostDocument doc2 = createMockPostDocument("post2", followeeId2.toString(), "caption 2", 20, 5);

        // Create mock PostDocuments for Public Pool (includes duplicate post2 and new post3)
        PostDocument doc3 = createMockPostDocument("post3", UUID.randomUUID().toString(), "caption 3", 5, 0);

        when(mongoTemplate.find(any(Query.class), eq(PostDocument.class)))
                .thenReturn(List.of(doc1, doc2)) // First call: followees
                .thenReturn(List.of(doc1, doc2, doc3)); // Second call: global

        List<PostCandidate> candidates = candidatePoolService.getCandidates(userId, 500);

        // Verify calls to mongoTemplate
        ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate, times(2)).find(queryCaptor.capture(), eq(PostDocument.class));

        List<Query> queries = queryCaptor.getAllValues();
        assertThat(queries).hasSize(2);

        // Query 1 checks for followee author_ids
        assertThat(queries.get(0).getQueryObject().toString()).contains("author_id");
        // Query 2 is global base query
        assertThat(queries.get(1).getQueryObject().toString()).doesNotContain("author_id");

        // Verify deduplication and conversion
        assertThat(candidates).hasSize(3);
        assertThat(candidates.get(0).postId()).isEqualTo("post1");
        assertThat(candidates.get(0).authorId()).isEqualTo(followeeId1.toString());
        assertThat(candidates.get(0).likeCount()).isEqualTo(10);
        assertThat(candidates.get(0).commentCount()).isEqualTo(2);

        assertThat(candidates.get(1).postId()).isEqualTo("post2");
        assertThat(candidates.get(1).authorId()).isEqualTo(followeeId2.toString());

        assertThat(candidates.get(2).postId()).isEqualTo("post3");
    }

    @Test
    void shouldRespectMaxLimitOfCandidates() {
        UUID userId = UUID.randomUUID();
        when(followRepository.findAcceptedFolloweeIds(userId)).thenReturn(List.of());

        PostDocument doc1 = createMockPostDocument("post1", UUID.randomUUID().toString(), "c1", 1, 1);
        PostDocument doc2 = createMockPostDocument("post2", UUID.randomUUID().toString(), "c2", 2, 2);
        PostDocument doc3 = createMockPostDocument("post3", UUID.randomUUID().toString(), "c3", 3, 3);

        when(mongoTemplate.find(any(Query.class), eq(PostDocument.class)))
                .thenReturn(List.of(doc1, doc2, doc3));

        List<PostCandidate> candidates = candidatePoolService.getCandidates(userId, 2);

        assertThat(candidates).hasSize(2);
        assertThat(candidates.get(0).postId()).isEqualTo("post1");
        assertThat(candidates.get(1).postId()).isEqualTo("post2");
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
