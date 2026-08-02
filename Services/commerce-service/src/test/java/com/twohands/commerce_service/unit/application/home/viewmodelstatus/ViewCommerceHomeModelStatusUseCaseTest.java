package com.twohands.commerce_service.unit.application.home.viewmodelstatus;

import com.twohands.commerce_service.application.home.viewmodelstatus.ViewCommerceHomeModelStatusUseCase;
import com.twohands.commerce_service.domain.home.PopularityNormalizer;
import com.twohands.commerce_service.domain.home.RankingMode;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.infrastructure.model.HomeModelLoader;
import com.twohands.commerce_service.security.AuthenticatedUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ViewCommerceHomeModelStatusUseCaseTest {

    private final HomeModelLoader homeModelLoader = mock(HomeModelLoader.class);

    private ViewCommerceHomeModelStatusUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ViewCommerceHomeModelStatusUseCase(homeModelLoader);
        ReflectionTestUtils.setField(useCase, "configuredRankingModel", "lightgbm");
    }

    @Test
    void returnsRuntimeStatusForAdmin() {
        when(homeModelLoader.resolveRuntime()).thenReturn(new HomeModelLoader.HomeModelRuntime(
                RankingMode.LIGHTGBM,
                "commerce_home_ranker",
                3,
                null,
                new PopularityNormalizer(0.0, 1.0)
        ));

        ViewCommerceHomeModelStatusUseCase.CommerceHomeModelStatus status = useCase.execute(admin());

        assertThat(status.mode()).isEqualTo("LIGHTGBM");
        assertThat(status.modelVersion()).isEqualTo(3);
        assertThat(status.modelName()).isEqualTo("commerce_home_ranker");
        assertThat(status.reason()).isNull();
        assertThat(status.configuredRankingModel()).isEqualTo("lightgbm");
    }

    @Test
    void rejectsNonAdminOrModerator() {
        AuthenticatedUser user = new AuthenticatedUser(UUID.randomUUID(), List.of("USER"), List.of());

        assertThatThrownBy(() -> useCase.execute(user))
                .isInstanceOf(AppException.class);
    }

    private static AuthenticatedUser admin() {
        return new AuthenticatedUser(UUID.randomUUID(), List.of("ADMIN"), List.of());
    }
}
