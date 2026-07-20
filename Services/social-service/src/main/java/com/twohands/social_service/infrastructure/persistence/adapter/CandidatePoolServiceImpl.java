package com.twohands.social_service.infrastructure.persistence.adapter;

import com.twohands.social_service.domain.follow.FollowRepository;
import com.twohands.social_service.domain.post.CandidatePoolService;
import com.twohands.social_service.domain.post.PostCandidate;
import com.twohands.social_service.domain.post.ProductTag;
import com.twohands.social_service.infrastructure.persistence.mongo.document.PostDocument;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Component
public class CandidatePoolServiceImpl implements CandidatePoolService {

    private final MongoTemplate mongoTemplate;
    private final FollowRepository followRepository;

    public CandidatePoolServiceImpl(MongoTemplate mongoTemplate, FollowRepository followRepository) {
        this.mongoTemplate = mongoTemplate;
        this.followRepository = followRepository;
    }

    @Override
    public List<PostCandidate> getCandidates(UUID userId, int maxSize) {
        if (userId == null) {
            return List.of();
        }

        // 1. Get followee IDs
        List<UUID> followeeIds = followRepository.findAcceptedFolloweeIds(userId);
        List<String> followeeAuthorIds = followeeIds != null 
                ? followeeIds.stream().map(UUID::toString).toList() 
                : List.of();

        // 2. Base Query Criteria
        Instant sevenDaysAgo = Instant.now().minus(7, ChronoUnit.DAYS);
        Criteria baseCriteria = Criteria.where("status").is("ACTIVE")
                .and("visibility").is("PUBLIC")
                .and("created_at").gte(sevenDaysAgo)
                .andOperator(new Criteria().orOperator(
                        Criteria.where("moderation_status").exists(false),
                        Criteria.where("moderation_status").is(null),
                        Criteria.where("moderation_status").is("NONE")
                ));

        // Source 1: Prioritize posts from author that user follows (~300 candidates)
        List<PostDocument> followeeDocs = List.of();
        if (!followeeAuthorIds.isEmpty()) {
            Query followeeQuery = new Query(
                    new Criteria().andOperator(
                            baseCriteria,
                            Criteria.where("author_id").in(followeeAuthorIds)
                    )
            );
            followeeQuery.with(Sort.by(Sort.Direction.DESC, "created_at"));
            followeeQuery.limit(300);
            followeeDocs = mongoTemplate.find(followeeQuery, PostDocument.class);
        }

        // Source 2: General public posts to fill the rest of the pool
        Query globalQuery = new Query(baseCriteria);
        globalQuery.with(Sort.by(Sort.Direction.DESC, "created_at"));
        globalQuery.limit(maxSize);
        List<PostDocument> globalDocs = mongoTemplate.find(globalQuery, PostDocument.class);

        // 3. Deduplicate, filter seen posts, and merge up to maxSize
        List<PostCandidate> candidates = new ArrayList<>();
        Set<String> addedIds = new HashSet<>();

        // TODO: Query from user_seen_posts database when migration is ready (Nhiệm vụ 3)
        Set<String> seenPostIds = Set.of();

        for (PostDocument doc : followeeDocs) {
            if (addedIds.add(doc.getId()) && !seenPostIds.contains(doc.getId())) {
                candidates.add(toCandidate(doc));
            }
        }

        for (PostDocument doc : globalDocs) {
            if (addedIds.add(doc.getId()) && !seenPostIds.contains(doc.getId())) {
                candidates.add(toCandidate(doc));
                if (candidates.size() >= maxSize) {
                    break;
                }
            }
        }

        return candidates;
    }

    private PostCandidate toCandidate(PostDocument doc) {
        List<ProductTag> productTags = doc.getProductTags() != null
                ? doc.getProductTags().stream().map(this::toProductTag).toList()
                : List.of();
        return new PostCandidate(
                doc.getId(),
                doc.getAuthorId(),
                doc.getCreatedAt(),
                doc.getHashtags() != null ? doc.getHashtags() : List.of(),
                productTags,
                doc.getLikeCount(),
                doc.getReplyCount()
        );
    }

    private ProductTag toProductTag(PostDocument.ProductTagDocument pt) {
        return new ProductTag(
                pt.getProductId(),
                pt.getPrice(),
                pt.getName(),
                pt.getImageUrl(),
                pt.getCategory(),
                pt.getAvailable()
        );
    }
}
