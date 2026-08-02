package com.twohands.commerce_service.unit.application.home.viewmodelartifacts;

import com.twohands.commerce_service.application.home.viewmodelartifacts.ViewCommerceHomeModelArtifactsUseCase;
import com.twohands.commerce_service.domain.home.HomeModelArtifactRepository;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.security.AuthenticatedUser;
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

class ViewCommerceHomeModelArtifactsUseCaseTest {

    private final HomeModelArtifactRepository homeModelArtifactRepository = mock(HomeModelArtifactRepository.class);

    private ViewCommerceHomeModelArtifactsUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ViewCommerceHomeModelArtifactsUseCase(homeModelArtifactRepository);
    }

    @Test
    void returnsArtifactsForModeratorAndDefaultsBlankModelName() {
        Instant trainedAt = Instant.parse("2026-08-02T08:30:00Z");
        List<HomeModelArtifactRepository.ModelArtifactListItem> artifacts = List.of(
                new HomeModelArtifactRepository.ModelArtifactListItem(
                        "commerce_home_ranker",
                        3,
                        "ONNX",
                        "artifacts/commerce_home_ranker/v3/model.onnx",
                        true,
                        trainedAt,
                        "{\"ndcg@10\":0.42}"
                ),
                new HomeModelArtifactRepository.ModelArtifactListItem(
                        "commerce_home_ranker",
                        2,
                        "ONNX",
                        "artifacts/commerce_home_ranker/v2/model.onnx",
                        false,
                        trainedAt.minusSeconds(3600),
                        "{\"ndcg@10\":0.39}"
                )
        );
        when(homeModelArtifactRepository.listByModelName("commerce_home_ranker")).thenReturn(artifacts);

        List<HomeModelArtifactRepository.ModelArtifactListItem> result = useCase.execute(moderator(), "   ");

        assertThat(result).hasSize(2);
        assertThat(result.get(0).version()).isEqualTo(3);
        assertThat(result.get(0).isActive()).isTrue();
        assertThat(result.get(1).version()).isEqualTo(2);
        assertThat(result.get(1).metricsJson()).contains("0.39");
        verify(homeModelArtifactRepository).listByModelName("commerce_home_ranker");
    }

    @Test
    void rejectsNonAdminOrModerator() {
        AuthenticatedUser user = new AuthenticatedUser(UUID.randomUUID(), List.of("USER"), List.of());

        assertThatThrownBy(() -> useCase.execute(user, "commerce_home_ranker"))
                .isInstanceOf(AppException.class);
    }

    private static AuthenticatedUser moderator() {
        return new AuthenticatedUser(UUID.randomUUID(), List.of("MODERATOR"), List.of());
    }
}
