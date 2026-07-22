package com.twohands.social_service.unit.application.admin.viewrecommendationmodelstatus;

import com.twohands.social_service.application.admin.viewrecommendationmodelstatus.ViewRecommendationModelStatusUseCase;
import com.twohands.social_service.domain.post.RankingModelRuntimeStatus;
import com.twohands.social_service.exception.AppException;
import com.twohands.social_service.infrastructure.model.ModelLoader;
import com.twohands.social_service.security.AuthenticatedUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ViewRecommendationModelStatusUseCaseTest {

    private final ModelLoader modelLoader = mock(ModelLoader.class);
    private ViewRecommendationModelStatusUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ViewRecommendationModelStatusUseCase(modelLoader);
        ReflectionTestUtils.setField(useCase, "rankingModelType", "lightgbm");
    }

    @Test
    void returnsLightGbmStatusForAdmin() {
        when(modelLoader.resolveRuntimeStatus("lightgbm"))
                .thenReturn(new RankingModelRuntimeStatus("lightgbm", 3, "feed_ranker", null, "lightgbm"));

        RankingModelRuntimeStatus status = useCase.execute(admin());

        assertThat(status.mode()).isEqualTo("lightgbm");
        assertThat(status.modelVersion()).isEqualTo(3);
        assertThat(status.reason()).isNull();
    }

    @Test
    void returnsRuleBasedFallbackReason() {
        when(modelLoader.resolveRuntimeStatus("lightgbm"))
                .thenReturn(new RankingModelRuntimeStatus(
                        "rule_based", null, null, ModelLoader.REASON_FILE_NOT_FOUND, "lightgbm"));

        RankingModelRuntimeStatus status = useCase.execute(admin());

        assertThat(status.mode()).isEqualTo("rule_based");
        assertThat(status.reason()).isEqualTo("file_not_found");
    }

    @Test
    void rejectsNonAdmin() {
        AuthenticatedUser user = new AuthenticatedUser(UUID.randomUUID(), List.of("USER"), List.of());
        assertThatThrownBy(() -> useCase.execute(user)).isInstanceOf(AppException.class);
    }

    private static AuthenticatedUser admin() {
        return new AuthenticatedUser(UUID.randomUUID(), List.of("ADMIN"), List.of());
    }
}
