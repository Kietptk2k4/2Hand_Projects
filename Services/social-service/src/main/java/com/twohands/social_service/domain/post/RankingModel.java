package com.twohands.social_service.domain.post;

public interface RankingModel {
    double predict(PostFeatureVector features);

    default java.util.List<Double> predictBatch(java.util.List<PostFeatureVector> featuresList) {
        return featuresList.stream().map(this::predict).toList();
    }
}
