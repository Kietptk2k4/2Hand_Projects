package com.twohands.commerce_service.domain.home;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record UserInterestProfile(
        Map<UUID, Double> categoryScores,
        Map<UUID, Double> brandScores,
        Map<UUID, Double> shopScores,
        Map<String, Double> hashtagScores,
        Map<String, Double> keywordScores,
        BigDecimal priceP25,
        BigDecimal priceP50,
        BigDecimal priceP75
) {
    public List<TagScore> allSocialTags() {
        List<TagScore> hashtags = hashtagScores.entrySet().stream()
                .map(entry -> new TagScore("HASHTAG", entry.getKey(), entry.getValue()))
                .toList();
        List<TagScore> keywords = keywordScores.entrySet().stream()
                .map(entry -> new TagScore("KEYWORD", entry.getKey(), entry.getValue()))
                .toList();
        return java.util.stream.Stream.concat(hashtags.stream(), keywords.stream()).toList();
    }

    public record TagScore(String tagType, String tag, double score) {
    }
}
