package com.twohands.commerce_service.application.home.recommendproducts;

import com.twohands.commerce_service.domain.home.CandidateProduct;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

final class HomeDiversityReranker {

    List<ScoredCandidate> rerank(
            List<ScoredCandidate> ranked,
            int topK,
            boolean enabled,
            int maxPerCategory,
            int maxPerBrand,
            int maxPerShop
    ) {
        if (!enabled || ranked.isEmpty()) {
            return ranked.stream().limit(topK).toList();
        }

        Map<UUID, Integer> categoryCounts = new HashMap<>();
        Map<UUID, Integer> brandCounts = new HashMap<>();
        Map<UUID, Integer> shopCounts = new HashMap<>();
        List<ScoredCandidate> selected = new ArrayList<>(topK);

        for (ScoredCandidate candidate : ranked) {
            if (selected.size() >= topK) {
                break;
            }
            if (withinCaps(candidate.candidate(), categoryCounts, brandCounts, shopCounts, maxPerCategory, maxPerBrand, maxPerShop)) {
                selected.add(candidate);
                increment(categoryCounts, candidate.candidate().product().categoryId());
                increment(brandCounts, candidate.candidate().product().brandId(), maxPerBrand);
                increment(shopCounts, candidate.candidate().product().shopId(), maxPerShop);
            }
        }

        if (selected.size() < topK) {
            for (ScoredCandidate candidate : ranked) {
                if (selected.size() >= topK) {
                    break;
                }
                if (!selected.contains(candidate)) {
                    selected.add(candidate);
                }
            }
        }

        return selected;
    }

    private boolean withinCaps(
            CandidateProduct candidate,
            Map<UUID, Integer> categoryCounts,
            Map<UUID, Integer> brandCounts,
            Map<UUID, Integer> shopCounts,
            int maxPerCategory,
            int maxPerBrand,
            int maxPerShop
    ) {
        UUID categoryId = candidate.product().categoryId();
        if (maxPerCategory > 0 && categoryId != null && categoryCounts.getOrDefault(categoryId, 0) >= maxPerCategory) {
            return false;
        }
        UUID brandId = candidate.product().brandId();
        if (maxPerBrand > 0 && brandId != null && brandCounts.getOrDefault(brandId, 0) >= maxPerBrand) {
            return false;
        }
        UUID shopId = candidate.product().shopId();
        return maxPerShop <= 0 || shopId == null || shopCounts.getOrDefault(shopId, 0) < maxPerShop;
    }

    private void increment(Map<UUID, Integer> counts, UUID key) {
        increment(counts, key, 1);
    }

    private void increment(Map<UUID, Integer> counts, UUID key, int enabledFlag) {
        if (key == null || enabledFlag <= 0) {
            return;
        }
        counts.put(key, counts.getOrDefault(key, 0) + 1);
    }

    record ScoredCandidate(CandidateProduct candidate, double score, double[] featureVector) {
    }
}
