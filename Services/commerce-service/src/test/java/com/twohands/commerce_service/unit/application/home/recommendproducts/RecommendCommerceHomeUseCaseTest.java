package com.twohands.commerce_service.unit.application.home.recommendproducts;

import com.twohands.commerce_service.application.home.recommendproducts.RecommendCommerceHomeUseCase;
import com.twohands.commerce_service.domain.home.HomeInteractionRepository;
import com.twohands.commerce_service.domain.home.HomeProductSnapshot;
import com.twohands.commerce_service.domain.home.HomeRecommendationReadRepository;
import com.twohands.commerce_service.domain.home.HomeRecommendationResult;
import com.twohands.commerce_service.domain.home.PopularityNormalizer;
import com.twohands.commerce_service.domain.home.RankingMode;
import com.twohands.commerce_service.domain.home.RetrievalSource;
import com.twohands.commerce_service.infrastructure.model.HomeModelLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RecommendCommerceHomeUseCaseTest {

    @Mock
    private HomeRecommendationReadRepository readRepository;

    @Mock
    private HomeInteractionRepository interactionRepository;

    @Mock
    private HomeModelLoader homeModelLoader;

    private RecommendCommerceHomeUseCase useCase;

    private final UUID userId = UUID.fromString("11111111-1111-4111-8111-111111111111");
    private final Instant now = Instant.parse("2026-08-02T12:00:00Z");
    private final Clock clock = Clock.fixed(now, ZoneOffset.UTC);

    @BeforeEach
    void setUp() {
        useCase = new RecommendCommerceHomeUseCase(readRepository, interactionRepository, homeModelLoader, clock);
        ReflectionTestUtils.setField(useCase, "enabled", true);
        ReflectionTestUtils.setField(useCase, "diversityEnabled", false);
        ReflectionTestUtils.setField(useCase, "topK", 50);
        ReflectionTestUtils.setField(useCase, "maxPerCategory", 8);
        ReflectionTestUtils.setField(useCase, "maxPerBrand", 5);
        ReflectionTestUtils.setField(useCase, "maxPerShop", 4);
        ReflectionTestUtils.setField(useCase, "cfMaxNeighbors", 30);
        ReflectionTestUtils.setField(useCase, "cfMaxProductsPerEntity", 20);
        ReflectionTestUtils.setField(useCase, "arMaxProductsPerCategory", 20);
        ReflectionTestUtils.setField(useCase, "arMinConfidence", 0.05d);
        ReflectionTestUtils.setField(useCase, "personalFetchLimit", 250);
        when(readRepository.findCompletedInteractions(any(), any(), any())).thenReturn(List.of());
        when(readRepository.findCartInteractions(any(), any(), any())).thenReturn(List.of());
        when(readRepository.findSocialSignals(any())).thenReturn(List.of());
        when(readRepository.findPopularProducts90d(any(), any(), any(), anyInt())).thenReturn(List.of());
        when(readRepository.findTopRatedProducts(any(), any(), anyInt())).thenReturn(List.of());
        when(readRepository.findProductsByCategories(anyCollection(), any(), any(), anyInt())).thenReturn(List.of());
        when(readRepository.findProductsByBrands(anyCollection(), any(), any(), anyInt())).thenReturn(List.of());
        when(readRepository.findProductsByShops(anyCollection(), any(), any(), anyInt())).thenReturn(List.of());
        when(readRepository.findCategoryNeighbors(anyCollection(), anyInt())).thenReturn(List.of());
        when(readRepository.findBrandNeighbors(anyCollection(), anyInt())).thenReturn(List.of());
        when(readRepository.findAssociationRules(any(), anyCollection(), anyDouble())).thenReturn(List.of());
        when(homeModelLoader.resolveRuntime()).thenReturn(degradedRuntime());
    }

    @Test
    void excludesVacationProductsFromServedPool() {
        when(readRepository.findNewestProducts(eq(userId), eq(now), eq(40))).thenReturn(List.of(
                snapshot(
                        "aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa",
                        10,
                        now.minusSeconds(3600),
                        true,
                        4.5,
                        10
                ),
                snapshot(
                        "bbbbbbbb-bbbb-4bbb-8bbb-bbbbbbbbbbbb",
                        10,
                        now.minusSeconds(7200),
                        false,
                        4.5,
                        10
                )
        ));

        HomeRecommendationResult result = useCase.execute(userId);

        assertThat(result.items()).hasSize(1);
        assertThat(result.items().getFirst().id())
                .isEqualTo(UUID.fromString("bbbbbbbb-bbbb-4bbb-8bbb-bbbbbbbbbbbb"));
    }

    @Test
    void doesNotQueryCrossDomainWhenSocialSignalsMissing() {
        when(readRepository.findNewestProducts(eq(userId), eq(now), eq(40))).thenReturn(List.of(
                snapshot("cccccccc-cccc-4ccc-8ccc-cccccccccccc", 0, now.minusSeconds(3600), false, 0.0, 0)
        ));

        useCase.execute(userId);

        verify(readRepository, never()).findAssociationRules(any(), anyCollection(), anyDouble());
    }

    @Test
    void logsProvenanceForMergedSources() {
        UUID productId = UUID.fromString("dddddddd-dddd-4ddd-8ddd-dddddddddddd");
        UUID categoryId = UUID.fromString("eeeeeeee-eeee-4eee-8eee-eeeeeeeeeeee");
        when(readRepository.findCompletedInteractions(any(), any(), any())).thenReturn(List.of(
                new HomeRecommendationReadRepository.CommerceInteraction(
                        categoryId,
                        null,
                        null,
                        BigDecimal.valueOf(100),
                        now.minusSeconds(7200),
                        1.0
                )
        ));
        when(readRepository.findSocialSignals(any())).thenReturn(List.of(
                new HomeRecommendationReadRepository.SocialTagSignal(
                        "HASHTAG",
                        "vintage",
                        2.0,
                        now.minusSeconds(3600),
                        now
                )
        ));
        when(readRepository.findProductsByCategories(anyCollection(), eq(userId), eq(now), anyInt())).thenAnswer(invocation -> {
            int limit = invocation.getArgument(3);
            return List.of(snapshotWithCategory(productId.toString(), categoryId, 8, now.minusSeconds(1000), false, 4.0, 6))
                    .stream()
                    .limit(limit)
                    .toList();
        });
        when(readRepository.findAssociationRules(eq("HASHTAG"), anyCollection(), anyDouble())).thenReturn(List.of(
                new HomeRecommendationReadRepository.AssociationRule("vintage", categoryId, 0.5)
        ));

        HomeRecommendationResult result = useCase.execute(userId);
        assertThat(result.items()).hasSize(1);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<HomeInteractionRepository.ServedCandidate>> captor = ArgumentCaptor.forClass(List.class);
        verify(interactionRepository).saveImpressions(
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                captor.capture()
        );

        HomeInteractionRepository.ServedCandidate served = captor.getValue().getFirst();
        assertThat(served.candidate().sources())
                .containsExactlyInAnyOrder(RetrievalSource.PERSONAL, RetrievalSource.CROSS_DOMAIN);
        assertThat(served.candidate().personalScore()).isEqualTo(1.0);
        assertThat(served.candidate().arScore()).isEqualTo(0.5);
    }

    @Test
    void usesDeterministicTieBreakInDegradedMode() {
        when(readRepository.findNewestProducts(eq(userId), eq(now), eq(40))).thenReturn(List.of(
                snapshot("00000000-0000-4000-8000-000000000002", 5, now.minusSeconds(3600), false, 4.0, 3),
                snapshot("00000000-0000-4000-8000-000000000001", 5, now.minusSeconds(3600), false, 4.0, 3)
        ));

        HomeRecommendationResult result = useCase.execute(userId);

        assertThat(result.rankingMode()).isEqualTo(RankingMode.DEGRADED);
        assertThat(result.items()).hasSize(2);
        assertThat(result.items().get(0).id())
                .isEqualTo(UUID.fromString("00000000-0000-4000-8000-000000000001"));
        assertThat(result.items().get(1).id())
                .isEqualTo(UUID.fromString("00000000-0000-4000-8000-000000000002"));
    }

    private HomeModelLoader.HomeModelRuntime degradedRuntime() {
        return new HomeModelLoader.HomeModelRuntime(
                RankingMode.DEGRADED,
                null,
                null,
                "onnx_session_missing",
                new PopularityNormalizer(0.0, 1.0)
        );
    }

    private HomeProductSnapshot snapshot(
            String productId,
            long popularityRaw,
            Instant createdAt,
            boolean shopVacation,
            double ratingAvg,
            int ratingCount
    ) {
        return snapshotWithCategory(
                productId,
                UUID.fromString("66666666-6666-4666-8666-666666666666"),
                popularityRaw,
                createdAt,
                shopVacation,
                ratingAvg,
                ratingCount
        );
    }

    private HomeProductSnapshot snapshotWithCategory(
            String productId,
            UUID categoryId,
            long popularityRaw,
            Instant createdAt,
            boolean shopVacation,
            double ratingAvg,
            int ratingCount
    ) {
        return new HomeProductSnapshot(
                UUID.fromString(productId),
                UUID.fromString("99999999-9999-4999-8999-999999999999"),
                UUID.fromString("77777777-7777-4777-8777-777777777777"),
                "Vintage Shop",
                categoryId,
                UUID.fromString("55555555-5555-4555-8555-555555555555"),
                "Item " + productId,
                "https://cdn.example/item.jpg",
                BigDecimal.valueOf(100),
                null,
                BigDecimal.valueOf(100),
                createdAt,
                BigDecimal.valueOf(ratingAvg),
                ratingCount,
                popularityRaw,
                true,
                shopVacation
        );
    }
}
