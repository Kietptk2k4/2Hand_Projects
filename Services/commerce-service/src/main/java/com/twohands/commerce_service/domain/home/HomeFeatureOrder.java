package com.twohands.commerce_service.domain.home;

import java.util.List;

/**
 * Locked HOME_FEATURE_ORDER — must match Python pipelines.home_feature_order.HOME_FEATURE_ORDER.
 */
public final class HomeFeatureOrder {

	public static final List<String> NAMES = List.of(
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

	public static final int DIM = NAMES.size();

	private HomeFeatureOrder() {
	}

	public static boolean matches(List<String> exported) {
		return NAMES.equals(exported);
	}
}
