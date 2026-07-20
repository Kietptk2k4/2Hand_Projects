package com.twohands.social_service.unit.infrastructure.model;

import com.twohands.social_service.domain.post.PostFeatureVector;
import com.twohands.social_service.infrastructure.model.LightGBMRankingModel;
import com.twohands.social_service.infrastructure.model.ModelLoader;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LightGBMRankingModelTest {

    private final ModelLoader modelLoader = mock(ModelLoader.class);
    private final LightGBMRankingModel rankingModel = new LightGBMRankingModel(modelLoader);

    @Test
    void shouldReturnZeroScoresWhenSessionIsNull() {
        when(modelLoader.getSession()).thenReturn(null);

        PostFeatureVector f1 = new PostFeatureVector(1.0, 0.5, 0.2, 0.1, 0.0, 0.0);
        double score = rankingModel.predict(f1);
        assertThat(score).isZero();

        List<Double> batchScores = rankingModel.predictBatch(List.of(f1));
        assertThat(batchScores).containsExactly(0.0);
    }
}
