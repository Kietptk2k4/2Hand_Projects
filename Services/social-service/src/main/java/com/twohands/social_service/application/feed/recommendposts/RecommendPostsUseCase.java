package com.twohands.social_service.application.feed.recommendposts;

import com.twohands.social_service.application.feed.viewglobalfeed.ViewGlobalFeedResult;
import com.twohands.social_service.application.post.common.ProductTagSnapshotData;
import com.twohands.social_service.domain.post.*;
import com.twohands.social_service.exception.AppException;
import com.twohands.social_service.exception.ErrorCode;
import com.twohands.social_service.infrastructure.model.LightGBMRankingModel;
import com.twohands.social_service.infrastructure.model.ModelLoader;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendPostsUseCase {

    private static final int MIN_PAGE = 0;
    private static final int MIN_SIZE = 1;
    private static final int MAX_SIZE = 50;
    private static final String SUCCESS_MESSAGE = "Lay recommend feed thanh cong.";

    private final CandidatePoolService candidatePoolService;
    private final PostFeatureBuilder postFeatureBuilder;
    private final RuleBasedRankingModel ruleBasedRankingModel;
    private final LightGBMRankingModel lightGbmRankingModel;
    private final ModelLoader modelLoader;
    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final PostImpressionLogger postImpressionLogger;

    @org.springframework.beans.factory.annotation.Value("${social.recommendation.ranking.model:lightgbm}")
    private String rankingModelType;

    public RecommendPostsUseCase(
            CandidatePoolService candidatePoolService,
            PostFeatureBuilder postFeatureBuilder,
            RuleBasedRankingModel ruleBasedRankingModel,
            LightGBMRankingModel lightGbmRankingModel,
            ModelLoader modelLoader,
            PostRepository postRepository,
            PostLikeRepository postLikeRepository,
            PostImpressionLogger postImpressionLogger
    ) {
        this.candidatePoolService = candidatePoolService;
        this.postFeatureBuilder = postFeatureBuilder;
        this.ruleBasedRankingModel = ruleBasedRankingModel;
        this.lightGbmRankingModel = lightGbmRankingModel;
        this.modelLoader = modelLoader;
        this.postRepository = postRepository;
        this.postLikeRepository = postLikeRepository;
        this.postImpressionLogger = postImpressionLogger;
    }

    public ViewGlobalFeedResult execute(UUID userId, int page, int size) {
        if (userId == null) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "Authentication required");
        }
        validatePagination(page, size);

        // 1. Fetch Candidate Pool
        List<PostCandidate> candidates = candidatePoolService.getCandidates(userId, 500);
        if (candidates == null || candidates.isEmpty()) {
            return ViewGlobalFeedResult.from(new PageResult<>(List.of(), page, size, 0, 0, false));
        }

        // 2. Build Feature Vectors
        List<PostFeatureVector> features = postFeatureBuilder.buildFeatureVectors(userId, candidates);

        // 3. Resolve active model and predict scores
        boolean useLightGbm = "lightgbm".equalsIgnoreCase(rankingModelType) && modelLoader.getSession() != null;
        RankingModel activeModel = useLightGbm ? lightGbmRankingModel : ruleBasedRankingModel;

        List<Double> scores = activeModel.predictBatch(features);

        List<CandidateWithScore> scoredCandidates = new ArrayList<>(candidates.size());
        for (int i = 0; i < candidates.size(); i++) {
            scoredCandidates.add(new CandidateWithScore(candidates.get(i), scores.get(i)));
        }
        Collections.sort(scoredCandidates);

        // 4. Paginate
        int totalElements = scoredCandidates.size();
        int start = page * size;
        int end = Math.min(start + size, totalElements);

        List<CandidateWithScore> pageSlice = (start >= totalElements)
                ? List.of()
                : scoredCandidates.subList(start, end);

        // 5. Fetch full Post entities for the page
        List<String> pagePostIds = pageSlice.stream().map(c -> c.candidate().postId()).toList();
        List<Post> posts = postRepository.findByIds(pagePostIds);

        // Sort posts to match the order of scored candidates
        Map<String, Post> postMap = posts.stream()
                .collect(Collectors.toMap(Post::id, p -> p, (p1, p2) -> p1));
        List<Post> orderedPosts = new ArrayList<>();
        for (CandidateWithScore cs : pageSlice) {
            Post post = postMap.get(cs.candidate().postId());
            if (post != null) {
                orderedPosts.add(post);
            }
        }

        // 6. Fetch user like status for the posts
        Set<String> likedPostIds = postLikeRepository.findLikedPostIdsByUserIdAndPostIds(
                userId,
                orderedPosts.stream().map(Post::id).toList()
        );

        // 7. Map to response view objects
        List<ViewGlobalFeedResult.FeedPostItem> items = orderedPosts.stream()
                .map(post -> toItem(post, likedPostIds.contains(post.id())))
                .toList();

        int totalPages = (int) Math.ceil((double) totalElements / size);
        boolean hasNext = end < totalElements;

        // 8. Log impressions asynchronously
        postImpressionLogger.logImpressions(userId, pagePostIds);

        return ViewGlobalFeedResult.from(new PageResult<>(
                items,
                page,
                size,
                totalElements,
                totalPages,
                hasNext
        ));
    }

    public String successMessage() {
        return SUCCESS_MESSAGE;
    }

    private void validatePagination(int page, int size) {
        if (page < MIN_PAGE) {
            throw new AppException(
                    ErrorCode.INVALID_PAGINATION,
                    "Tham so pagination khong hop le.",
                    "page",
                    "MUST_BE_GREATER_THAN_OR_EQUAL_TO_0"
            );
        }
        if (size < MIN_SIZE || size > MAX_SIZE) {
            throw new AppException(
                    ErrorCode.INVALID_PAGINATION,
                    "Tham so pagination khong hop le.",
                    "size",
                    "MUST_BE_BETWEEN_1_AND_50"
            );
        }
    }

    private ViewGlobalFeedResult.FeedPostItem toItem(Post post, boolean likedByMe) {
        List<ViewGlobalFeedResult.MediaItemData> media = post.media()
                .stream()
                .map(m -> new ViewGlobalFeedResult.MediaItemData(m.url(), m.type(), m.width(), m.height()))
                .toList();
        List<ProductTagSnapshotData> productTags = post.productTags() == null
                ? List.of()
                : post.productTags().stream().map(ProductTagSnapshotData::fromDomain).toList();

        return new ViewGlobalFeedResult.FeedPostItem(
                post.id(),
                post.authorId(),
                post.caption(),
                media,
                post.visibility().name(),
                post.likeCount(),
                post.replyCount(),
                likedByMe,
                post.hashtags(),
                productTags,
                post.allowComments(),
                post.createdAt().toString(),
                post.updatedAt().toString()
        );
    }

    private record CandidateWithScore(PostCandidate candidate, double score) implements Comparable<CandidateWithScore> {
        @Override
        public int compareTo(CandidateWithScore o) {
            // Sort descending: highest score first
            return Double.compare(o.score, this.score);
        }
    }
}
