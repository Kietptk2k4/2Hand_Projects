package com.twohands.social_service.unit.application.admin.viewrecommendationmodelartifacts;

import com.twohands.social_service.application.admin.viewrecommendationmodelartifacts.ViewRecommendationModelArtifactsUseCase;
import com.twohands.social_service.domain.post.ModelArtifactRepository;
import com.twohands.social_service.exception.AppException;
import com.twohands.social_service.security.AuthenticatedUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ViewRecommendationModelArtifactsUseCaseTest {

    private final ModelArtifactRepository modelArtifactRepository = mock(ModelArtifactRepository.class);
    private ViewRecommendationModelArtifactsUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ViewRecommendationModelArtifactsUseCase(modelArtifactRepository);
    }

    @Test
    void returnsArtifactsForModeratorAndDefaultsBlankModelName() {
        Instant trainedAt = Instant.parse("2026-07-22T08:30:00Z");
        List<ModelArtifactRepository.ModelArtifactListItem> artifacts = List.of(
                new ModelArtifactRepository.ModelArtifactListItem(
                        "feed_ranker",
                        3,
                        "ONNX",
                        "artifacts/feed_ranker/v3/model.onnx",
                        true,
                        trainedAt,
                        "{\"gate\":{\"status\":\"passed\"}}"
                ),
                new ModelArtifactRepository.ModelArtifactListItem(
                        "feed_ranker",
                        2,
                        "ONNX",
                        "artifacts/feed_ranker/v2/model.onnx",
                        false,
                        trainedAt.minusSeconds(3600),
                        "{\"gate\":{\"status\":\"rejected_by_metrics\"}}"
                )
        );
        when(modelArtifactRepository.listByModelName("feed_ranker")).thenReturn(artifacts);

        List<ModelArtifactRepository.ModelArtifactListItem> result = useCase.execute(moderator(), "   ");

        assertThat(result).hasSize(2);
        assertThat(result.get(0).version()).isEqualTo(3);
        assertThat(result.get(0).isActive()).isTrue();
        assertThat(result.get(1).version()).isEqualTo(2);
        assertThat(result.get(1).isActive()).isFalse();
        assertThat(result.get(1).metricsJson()).contains("rejected_by_metrics");
        verify(modelArtifactRepository).listByModelName("feed_ranker");
    }

    @Test
    void rejectsNonAdminOrModerator() {
        AuthenticatedUser user = new AuthenticatedUser(UUID.randomUUID(), List.of("USER"), List.of());

        assertThatThrownBy(() -> useCase.execute(user, "feed_ranker"))
                .isInstanceOf(AppException.class);
    }

    private static AuthenticatedUser moderator() {
        return new AuthenticatedUser(UUID.randomUUID(), List.of("MODERATOR"), List.of());
    }
}
