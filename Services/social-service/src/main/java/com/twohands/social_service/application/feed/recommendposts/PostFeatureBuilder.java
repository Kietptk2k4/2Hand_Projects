package com.twohands.social_service.application.feed.recommendposts;

import com.twohands.social_service.domain.follow.FollowRepository;
import com.twohands.social_service.domain.integration.UserProductAffinity;
import com.twohands.social_service.domain.integration.UserProductAffinityClient;
import com.twohands.social_service.domain.post.*;
import com.twohands.social_service.domain.search.SearchHistoryRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class PostFeatureBuilder {

    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final PostSaveRepository postSaveRepository;
    private final FollowRepository followRepository;
    private final SearchHistoryRepository searchHistoryRepository;
    private final UserProductAffinityClient userProductAffinityClient;

    public PostFeatureBuilder(
            PostRepository postRepository,
            PostLikeRepository postLikeRepository,
            PostSaveRepository postSaveRepository,
            FollowRepository followRepository,
            SearchHistoryRepository searchHistoryRepository,
            UserProductAffinityClient userProductAffinityClient
    ) {
        this.postRepository = postRepository;
        this.postLikeRepository = postLikeRepository;
        this.postSaveRepository = postSaveRepository;
        this.followRepository = followRepository;
        this.searchHistoryRepository = searchHistoryRepository;
        this.userProductAffinityClient = userProductAffinityClient;
    }

    public List<PostFeatureVector> buildFeatureVectors(UUID userId, List<PostCandidate> candidates) {
        if (candidates == null || candidates.isEmpty()) {
            return List.of();
        }

        Set<UUID> userFollowing = new HashSet<>(followRepository.findAcceptedFolloweeIds(userId));

        List<String> likedPostIds = postLikeRepository.findRecentLikedPostIds(userId, 50);
        PageResult<PostSaveEntry> savedResult = postSaveRepository.findByUserId(userId, 0, 50);
        List<String> savedPostIds = savedResult != null
                ? savedResult.items().stream().map(PostSaveEntry::postId).toList()
                : List.of();

        Set<String> interactionIds = new HashSet<>();
        interactionIds.addAll(likedPostIds);
        interactionIds.addAll(savedPostIds);
        List<Post> interactionPosts = postRepository.findByIds(interactionIds);

        Map<String, Post> postMap = interactionPosts.stream()
                .collect(Collectors.toMap(Post::id, post -> post, (p1, p2) -> p1));

        Set<String> likedTags = new HashSet<>();
        Map<String, Integer> likedAuthorCounts = new HashMap<>();
        for (String postId : likedPostIds) {
            Post post = postMap.get(postId);
            if (post != null) {
                if (post.hashtags() != null) {
                    post.hashtags().forEach(tag -> likedTags.add(tag.toLowerCase()));
                }
                likedAuthorCounts.merge(post.authorId(), 1, Integer::sum);
            }
        }

        Set<String> savedTags = new HashSet<>();
        Map<String, Integer> savedAuthorCounts = new HashMap<>();
        for (String postId : savedPostIds) {
            Post post = postMap.get(postId);
            if (post != null) {
                if (post.hashtags() != null) {
                    post.hashtags().forEach(tag -> savedTags.add(tag.toLowerCase()));
                }
                savedAuthorCounts.merge(post.authorId(), 1, Integer::sum);
            }
        }

        List<String> recentKeywords = searchHistoryRepository.findRecentKeywordsByUserId(userId, 20);
        Set<String> searchTerms = recentKeywords != null
                ? recentKeywords.stream().map(String::toLowerCase).collect(Collectors.toSet())
                : Set.of();

        UserProductAffinity affinity = userProductAffinityClient != null
                ? userProductAffinityClient.findByUserId(userId)
                : UserProductAffinity.empty();

        Map<UUID, Set<UUID>> authorFollowingCache = new HashMap<>();

        int size = candidates.size();
        double[] recencyScores = new double[size];
        double[] rawEngagement = new double[size];
        double[] rawHashtagMatch = new double[size];
        double[] rawAuthorAffinity = new double[size];
        double[] mutualFollowScores = new double[size];
        double[] crossDomainProductScores = new double[size];

        double minEngagement = Double.MAX_VALUE, maxEngagement = -Double.MAX_VALUE;
        double minHashtag = Double.MAX_VALUE, maxHashtag = -Double.MAX_VALUE;
        double minAffinity = Double.MAX_VALUE, maxAffinity = -Double.MAX_VALUE;

        Instant now = Instant.now();

        for (int i = 0; i < size; i++) {
            PostCandidate candidate = candidates.get(i);
            UUID authorUuid = null;
            try {
                authorUuid = UUID.fromString(candidate.authorId());
            } catch (Exception ignored) {
            }

            double deltaSeconds = Math.max(0, now.getEpochSecond() - candidate.createdAt().getEpochSecond());
            double halfLifeSeconds = 7.0 * 24.0 * 3600.0;
            recencyScores[i] = Math.pow(2.0, -deltaSeconds / halfLifeSeconds);

            rawEngagement[i] = Math.log(1.0 + candidate.likeCount()) + 2.0 * Math.log(1.0 + candidate.commentCount());
            minEngagement = Math.min(minEngagement, rawEngagement[i]);
            maxEngagement = Math.max(maxEngagement, rawEngagement[i]);

            double hashtagScore = 0.0;
            if (candidate.hashtags() != null) {
                for (String tag : candidate.hashtags()) {
                    String lowerTag = tag.toLowerCase();
                    if (searchTerms.contains(lowerTag)) {
                        hashtagScore += 1.0;
                    }
                    if (savedTags.contains(lowerTag)) {
                        hashtagScore += 0.8;
                    }
                    if (likedTags.contains(lowerTag)) {
                        hashtagScore += 0.4;
                    }
                }
            }
            rawHashtagMatch[i] = hashtagScore;
            minHashtag = Math.min(minHashtag, rawHashtagMatch[i]);
            maxHashtag = Math.max(maxHashtag, rawHashtagMatch[i]);

            double affinityScore = 0.0;
            if (authorUuid != null) {
                boolean followsAuthor = userFollowing.contains(authorUuid);
                int likedCount = likedAuthorCounts.getOrDefault(candidate.authorId(), 0);
                int savedCount = savedAuthorCounts.getOrDefault(candidate.authorId(), 0);
                affinityScore = (followsAuthor ? 1.0 : 0.0) + (likedCount * 0.5) + (savedCount * 0.6);
            }
            rawAuthorAffinity[i] = affinityScore;
            minAffinity = Math.min(minAffinity, rawAuthorAffinity[i]);
            maxAffinity = Math.max(maxAffinity, rawAuthorAffinity[i]);

            if (authorUuid != null) {
                if (userFollowing.contains(authorUuid)) {
                    mutualFollowScores[i] = 1.0;
                } else {
                    UUID finalAuthorUuid = authorUuid;
                    Set<UUID> authorFollowing = authorFollowingCache.computeIfAbsent(authorUuid, id -> {
                        try {
                            return new HashSet<>(followRepository.findAcceptedFolloweeIds(finalAuthorUuid));
                        } catch (Exception e) {
                            return Set.of();
                        }
                    });

                    Set<UUID> intersection = new HashSet<>(userFollowing);
                    intersection.retainAll(authorFollowing);

                    Set<UUID> union = new HashSet<>(userFollowing);
                    union.addAll(authorFollowing);

                    mutualFollowScores[i] = union.isEmpty() ? 0.0 : (double) intersection.size() / union.size();
                }
            } else {
                mutualFollowScores[i] = 0.0;
            }

            crossDomainProductScores[i] = CrossDomainProductScore.compute(affinity, candidate.productTags());
        }

        List<PostFeatureVector> vectors = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            double engagementNorm = normalize(rawEngagement[i], minEngagement, maxEngagement);
            double hashtagNorm = normalize(rawHashtagMatch[i], minHashtag, maxHashtag);
            double affinityNorm = normalize(rawAuthorAffinity[i], minAffinity, maxAffinity);

            vectors.add(new PostFeatureVector(
                    recencyScores[i],
                    engagementNorm,
                    hashtagNorm,
                    affinityNorm,
                    mutualFollowScores[i],
                    crossDomainProductScores[i]
            ));
        }

        return vectors;
    }

    private double normalize(double val, double min, double max) {
        if (max - min == 0.0) {
            return 0.0;
        }
        return (val - min) / (max - min);
    }
}
