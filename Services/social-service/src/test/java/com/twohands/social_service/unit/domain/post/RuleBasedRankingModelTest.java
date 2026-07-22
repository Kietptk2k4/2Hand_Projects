package com.twohands.social_service.unit.domain.post;

import com.twohands.social_service.domain.post.PostFeatureVector;
import com.twohands.social_service.domain.post.RuleBasedRankingModel;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class RuleBasedRankingModelTest {

    private final RuleBasedRankingModel model = new RuleBasedRankingModel();

    @Test
    void scoresWithLockedSixFeatureWeights() {
        PostFeatureVector features = new PostFeatureVector(1.0, 1.0, 1.0, 1.0, 1.0, 1.0);
        assertThat(model.predict(features)).isCloseTo(1.0, within(1e-9));
    }

    @Test
    void crossDomainAloneContributesExpectedWeight() {
        PostFeatureVector features = new PostFeatureVector(0, 0, 0, 0, 0, 1.0);
        assertThat(model.predict(features)).isCloseTo(0.12, within(1e-9));
    }

    @Test
    void nullFeaturesReturnZero() {
        assertThat(model.predict(null)).isZero();
    }
}
