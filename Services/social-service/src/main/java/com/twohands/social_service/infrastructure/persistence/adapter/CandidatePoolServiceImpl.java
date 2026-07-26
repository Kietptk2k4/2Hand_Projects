package com.twohands.social_service.infrastructure.persistence.adapter;

import com.twohands.social_service.domain.follow.FollowRepository;
import com.twohands.social_service.domain.post.CandidatePoolService;
import com.twohands.social_service.domain.post.PostCandidate;
import com.twohands.social_service.domain.post.ProductTag;
import com.twohands.social_service.domain.post.UserSeenPostsRepository;
import com.twohands.social_service.infrastructure.persistence.mongo.document.PostDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
public class CandidatePoolServiceImpl implements CandidatePoolService {

    private static final Logger log = LoggerFactory.getLogger(CandidatePoolServiceImpl.class);
    private static final List<Integer> DEFAULT_WINDOW_DAYS = List.of(7, 30, 90);
    private static final int DEFAULT_MIN_POOL_SIZE = 20;

    private final MongoTemplate mongoTemplate;
    private final FollowRepository followRepository;
    private final UserSeenPostsRepository userSeenPostsRepository;

    private int minPoolSize = DEFAULT_MIN_POOL_SIZE;
    private List<Integer> windowDays = DEFAULT_WINDOW_DAYS;

    public CandidatePoolServiceImpl(
            MongoTemplate mongoTemplate,
            FollowRepository followRepository,
            UserSeenPostsRepository userSeenPostsRepository
    ) {
        this.mongoTemplate = mongoTemplate;
        this.followRepository = followRepository;
        this.userSeenPostsRepository = userSeenPostsRepository;
    }

    @Value("${social.recommendation.recall.min-pool-size:20}")
    void setMinPoolSize(int minPoolSize) {
        this.minPoolSize = minPoolSize > 0 ? minPoolSize : DEFAULT_MIN_POOL_SIZE;
    }

    @Value("${social.recommendation.recall.window-days:7,30,90}")
    void setWindowDays(String windowDaysRaw) {
        this.windowDays = parseWindowDays(windowDaysRaw);
    }

    public static List<Integer> parseWindowDays(String windowDaysRaw) {
        if (windowDaysRaw == null || windowDaysRaw.isBlank()) {
            return DEFAULT_WINDOW_DAYS;
        }
        List<Integer> parsed = Arrays.stream(windowDaysRaw.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Integer::valueOf)
                .filter(d -> d > 0)
                .toList();
        return parsed.isEmpty() ? DEFAULT_WINDOW_DAYS : List.copyOf(parsed);
    }

    @Override
    public List<PostCandidate> getCandidates(UUID userId, int maxSize) {
        if (userId == null) {
            return List.of();
        }

        List<UUID> followeeIds = followRepository.findAcceptedFolloweeIds(userId);
        List<String> followeeAuthorIds = followeeIds != null
                ? followeeIds.stream().map(UUID::toString).toList()
                : List.of();

        Set<String> seenPostIds;
        try {
            seenPostIds = userSeenPostsRepository.findSeenPostIds(userId);
        } catch (Exception ex) {
            seenPostIds = Set.of();
        }

        List<PostCandidate> chosen = List.of();
        int chosenWindowDays = windowDays.get(windowDays.size() - 1);

        for (int windowDay : windowDays) {
            List<PostCandidate> pool = buildPoolForWindow(
                    maxSize,
                    windowDay,
                    followeeAuthorIds,
                    seenPostIds
            );
            chosen = pool;
            chosenWindowDays = windowDay;
            if (pool.size() >= minPoolSize) {
                break;
            }
        }

        log.info(
                "Recommend candidate recall: windowDays={}, poolSize={}, minPoolSize={}, userId={}",
                chosenWindowDays,
                chosen.size(),
                minPoolSize,
                userId
        );
        return chosen;
    }

    private List<PostCandidate> buildPoolForWindow(
            int maxSize,
            int windowDays,
            List<String> followeeAuthorIds,
            Set<String> seenPostIds
    ) {
        Instant cutoff = Instant.now().minus(windowDays, ChronoUnit.DAYS);
        Criteria baseCriteria = Criteria.where("status").is("ACTIVE")
                .and("visibility").is("PUBLIC")
                .and("created_at").gte(cutoff)
                .andOperator(new Criteria().orOperator(
                        Criteria.where("moderation_status").exists(false),
                        Criteria.where("moderation_status").is(null),
                        Criteria.where("moderation_status").is("NONE")
                ));

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

        Query globalQuery = new Query(baseCriteria);
        globalQuery.with(Sort.by(Sort.Direction.DESC, "created_at"));
        globalQuery.limit(maxSize);
        List<PostDocument> globalDocs = mongoTemplate.find(globalQuery, PostDocument.class);

        List<PostCandidate> candidates = new ArrayList<>();
        Set<String> addedIds = new HashSet<>();

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
                pt.getCategoryId(),
                pt.getShopId(),
                pt.getAvailable()
        );
    }
}
