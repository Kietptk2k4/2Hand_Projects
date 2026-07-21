package com.twohands.social_service.domain.post;

import com.twohands.social_service.domain.integration.UserProductAffinity;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class CrossDomainProductScore {

    public static final double WEIGHT_CATEGORY = 0.6;
    public static final double WEIGHT_SHOP = 0.4;

    private CrossDomainProductScore() {
    }

    public static double compute(UserProductAffinity affinity, List<ProductTag> productTags) {
        if (affinity == null || productTags == null || productTags.isEmpty()) {
            return 0.0;
        }
        Set<String> postCategories = new HashSet<>();
        Set<String> postShops = new HashSet<>();
        for (ProductTag tag : productTags) {
            if (tag == null) {
                continue;
            }
            if (tag.categoryId() != null && !tag.categoryId().isBlank()) {
                postCategories.add(tag.categoryId());
            }
            if (tag.shopId() != null && !tag.shopId().isBlank()) {
                postShops.add(tag.shopId());
            }
        }
        if (postCategories.isEmpty() && postShops.isEmpty()) {
            return 0.0;
        }
        boolean categoryOverlap = intersects(affinity.categoryIds(), postCategories);
        boolean shopOverlap = intersects(affinity.shopIds(), postShops);
        return WEIGHT_CATEGORY * (categoryOverlap ? 1.0 : 0.0)
                + WEIGHT_SHOP * (shopOverlap ? 1.0 : 0.0);
    }

    private static boolean intersects(Set<String> left, Set<String> right) {
        if (left == null || left.isEmpty() || right == null || right.isEmpty()) {
            return false;
        }
        for (String value : right) {
            if (left.contains(value)) {
                return true;
            }
        }
        return false;
    }
}
