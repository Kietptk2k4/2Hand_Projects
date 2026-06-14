package com.twohands.commerce_service.application.review.common;

import com.twohands.commerce_service.domain.integration.UserPublicProfileReadPort;
import com.twohands.commerce_service.domain.integration.UserPublicProfileSummary;
import com.twohands.commerce_service.domain.order.CommerceBuyerSummary;
import com.twohands.commerce_service.domain.review.ProductReviewListItem;
import com.twohands.commerce_service.domain.review.PublicShopReviewListItem;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ReviewBuyerEnrichmentService {

    private static final String FALLBACK_DISPLAY_NAME = "Ng\u01b0\u1eddi mua";

    private final UserPublicProfileReadPort userPublicProfileReadPort;

    public ReviewBuyerEnrichmentService(UserPublicProfileReadPort userPublicProfileReadPort) {
        this.userPublicProfileReadPort = userPublicProfileReadPort;
    }

    public List<ProductReviewListItem> enrichProductReviews(List<ProductReviewListItem> reviews) {
        if (reviews == null || reviews.isEmpty()) {
            return List.of();
        }
        Map<UUID, UserPublicProfileSummary> profiles = loadProfiles(reviews.stream()
                .map(ProductReviewListItem::buyerId)
                .collect(Collectors.toSet()));
        return reviews.stream()
                .map(review -> enrichProductReview(review, profiles))
                .toList();
    }

    public CommerceBuyerSummary enrichBuyer(UUID buyerId) {
        if (buyerId == null) {
            return CommerceBuyerSummary.empty();
        }
        UserPublicProfileSummary profile = loadProfiles(Set.of(buyerId)).get(buyerId);
        return new CommerceBuyerSummary(
                buyerId,
                resolveDisplayName(profile),
                resolveAvatarUrl(profile)
        );
    }

    public List<PublicShopReviewListItem> enrichShopReviews(List<PublicShopReviewListItem> reviews) {
        if (reviews == null || reviews.isEmpty()) {
            return List.of();
        }
        Map<UUID, UserPublicProfileSummary> profiles = loadProfiles(reviews.stream()
                .map(PublicShopReviewListItem::buyerId)
                .collect(Collectors.toSet()));
        return reviews.stream()
                .map(review -> enrichShopReview(review, profiles))
                .toList();
    }

    private Map<UUID, UserPublicProfileSummary> loadProfiles(Set<UUID> buyerIds) {
        Set<UUID> ids = buyerIds.stream().filter(Objects::nonNull).collect(Collectors.toSet());
        if (ids.isEmpty()) {
            return Map.of();
        }
        return userPublicProfileReadPort.findByUserIds(ids);
    }

    private ProductReviewListItem enrichProductReview(
            ProductReviewListItem review,
            Map<UUID, UserPublicProfileSummary> profiles
    ) {
        UserPublicProfileSummary profile = profiles.get(review.buyerId());
        return new ProductReviewListItem(
                review.reviewId(),
                review.buyerId(),
                resolveDisplayName(profile),
                resolveAvatarUrl(profile),
                review.rating(),
                review.comment(),
                review.createdAt(),
                review.media(),
                review.sellerReply()
        );
    }

    private PublicShopReviewListItem enrichShopReview(
            PublicShopReviewListItem review,
            Map<UUID, UserPublicProfileSummary> profiles
    ) {
        UserPublicProfileSummary profile = profiles.get(review.buyerId());
        return new PublicShopReviewListItem(
                review.reviewId(),
                review.buyerId(),
                resolveDisplayName(profile),
                resolveAvatarUrl(profile),
                review.productNameSnapshot(),
                review.rating(),
                review.comment(),
                review.createdAt(),
                review.media(),
                review.sellerReply()
        );
    }

    private String resolveDisplayName(UserPublicProfileSummary profile) {
        if (profile == null || profile.displayName() == null || profile.displayName().isBlank()) {
            return FALLBACK_DISPLAY_NAME;
        }
        return profile.displayName();
    }

    private String resolveAvatarUrl(UserPublicProfileSummary profile) {
        return profile == null ? null : profile.avatarUrl();
    }
}
