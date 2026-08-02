package com.twohands.commerce_service.unit.application.home.recommendproducts;

import com.twohands.commerce_service.application.home.recommendproducts.HomeFeatureBuilder;
import com.twohands.commerce_service.domain.home.HomeFeatureOrder;
import com.twohands.commerce_service.domain.home.PopularityNormalizer;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class HomeFeatureBuilderTest {

	private static final UUID PRODUCT = UUID.fromString("11111111-1111-4111-8111-111111111111");
	private static final UUID CATEGORY = UUID.fromString("22222222-2222-4222-8222-222222222222");
	private static final UUID BRAND = UUID.fromString("33333333-3333-4333-8333-333333333333");
	private static final UUID SHOP = UUID.fromString("44444444-4444-4444-8444-444444444444");

	@Test
	void missingCreatedAtYieldsZeroRecencyAndPriceInsideIqrIsOne() {
		Instant asOf = Instant.parse("2026-08-01T00:00:00Z");
		PopularityNormalizer normalizer = new PopularityNormalizer(0.0, PopularityNormalizer.log1pRaw(10));
		HomeFeatureBuilder.CandidateInput candidate = new HomeFeatureBuilder.CandidateInput(
				PRODUCT,
				CATEGORY,
				BRAND,
				SHOP,
				100.0,
				null,
				4.0,
				10,
				0L,
				Set.of("PERSONAL", "CF"),
				0.8,
				0.5,
				null
		);
		HomeFeatureBuilder.ProfileInput profile = new HomeFeatureBuilder.ProfileInput(
				Map.of(CATEGORY, 0.7),
				Map.of(BRAND, 0.4),
				Map.of(SHOP, 0.2),
				80.0,
				120.0
		);

		double[] vector = HomeFeatureBuilder.build(candidate, profile, normalizer, asOf);

		assertThat(vector).hasSize(HomeFeatureOrder.DIM);
		assertThat(vector[0]).isEqualTo(0.0);
		assertThat(vector[6]).isEqualTo(1.0);
		assertThat(vector[11]).isEqualTo(1.0);
		assertThat(vector[12]).isEqualTo(1.0);
		assertThat(vector[8]).isCloseTo(0.5, within(1e-9));
		assertThat(vector[9]).isEqualTo(0.0);
		assertThat(vector[14]).isEqualTo(0.0);
	}

	@Test
	void sparseRatingDefaultsToHalf() {
		Instant asOf = Instant.parse("2026-08-01T00:00:00Z");
		PopularityNormalizer normalizer = new PopularityNormalizer(0.0, 1.0);
		HomeFeatureBuilder.CandidateInput candidate = new HomeFeatureBuilder.CandidateInput(
				PRODUCT, CATEGORY, null, SHOP, null, asOf, 5.0, 2, 0L,
				Set.of("POPULAR"), null, null, null
		);
		HomeFeatureBuilder.ProfileInput profile = new HomeFeatureBuilder.ProfileInput(
				Map.of(), Map.of(), Map.of(), null, null
		);

		double[] vector = HomeFeatureBuilder.build(candidate, profile, normalizer, asOf);
		assertThat(vector[2]).isEqualTo(0.5);
		assertThat(vector[10]).isEqualTo(1.0);
	}
}
