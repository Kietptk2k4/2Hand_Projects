package com.twohands.commerce_service.unit.domain.home;

import com.twohands.commerce_service.domain.home.HomeFeatureOrder;
import com.twohands.commerce_service.domain.home.PopularityNormalizer;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class HomeFeatureOrderAndPopularityNormalizerTest {

	@Test
	void homeFeatureOrderIsLockedFifteenDimensions() {
		assertThat(HomeFeatureOrder.DIM).isEqualTo(15);
		assertThat(HomeFeatureOrder.NAMES).containsExactly(
				"recency_score",
				"popularity_score",
				"rating_score",
				"category_match",
				"brand_match",
				"shop_match",
				"price_affinity",
				"cross_domain_score",
				"cf_score",
				"semantic_similarity",
				"is_popular",
				"is_personal",
				"is_cf",
				"is_cross_domain",
				"is_semantic"
		);
		assertThat(HomeFeatureOrder.matches(List.copyOf(HomeFeatureOrder.NAMES))).isTrue();
		assertThat(HomeFeatureOrder.matches(List.of("wrong"))).isFalse();
	}

	@Test
	void popularityNormalizerClipsAndScales() {
		double zHi = PopularityNormalizer.log1pRaw(10);
		PopularityNormalizer normalizer = new PopularityNormalizer(0.0, zHi);
		assertThat(normalizer.normalize(0.0)).isEqualTo(0.0);
		assertThat(normalizer.normalize(zHi)).isCloseTo(1.0, within(1e-9));
		assertThat(normalizer.normalize(-1.0)).isEqualTo(0.0);
		assertThat(normalizer.normalize(100.0)).isEqualTo(1.0);
	}
}
