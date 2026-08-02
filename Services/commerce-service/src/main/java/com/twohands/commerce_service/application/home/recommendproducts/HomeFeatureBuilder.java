package com.twohands.commerce_service.application.home.recommendproducts;

import com.twohands.commerce_service.domain.home.HomeFeatureOrder;
import com.twohands.commerce_service.domain.home.PopularityNormalizer;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Normative Home LTR feature builder — formulas locked with Python home_features.py.
 */
public final class HomeFeatureBuilder {

	private static final double SECONDS_PER_DAY = 86400.0;
	private static final double RECENCY_HALF_LIFE_DAYS = 7.0;

	private HomeFeatureBuilder() {
	}

	public record CandidateInput(
			UUID productId,
			UUID categoryId,
			UUID brandId,
			UUID shopId,
			Double effectivePrice,
			Instant createdAt,
			Double ratingAvg,
			int ratingCount,
			long popularityRaw,
			Set<String> sources,
			Double personalScore,
			Double cfScore,
			Double arScore
	) {
	}

	public record ProfileInput(
			Map<UUID, Double> categoryScores,
			Map<UUID, Double> brandScores,
			Map<UUID, Double> shopScores,
			Double priceP25,
			Double priceP75
	) {
	}

	public static double[] build(
			CandidateInput candidate,
			ProfileInput profile,
			PopularityNormalizer normalizer,
			Instant asOf
	) {
		Objects.requireNonNull(candidate, "candidate");
		Objects.requireNonNull(profile, "profile");
		Objects.requireNonNull(normalizer, "normalizer");
		Objects.requireNonNull(asOf, "asOf");

		double recency;
		if (candidate.createdAt() == null) {
			recency = 0.0;
		} else {
			double delta = Math.max(0.0, asOf.getEpochSecond() - candidate.createdAt().getEpochSecond());
			recency = Math.pow(2.0, -delta / (RECENCY_HALF_LIFE_DAYS * SECONDS_PER_DAY));
		}

		double popularity = normalizer.normalize(PopularityNormalizer.log1pRaw(candidate.popularityRaw()));

		double rating;
		if (candidate.ratingCount() < 3) {
			rating = 0.5;
		} else {
			double avg = candidate.ratingAvg() == null ? 0.0 : candidate.ratingAvg();
			rating = clip01(avg / 5.0);
		}

		double categoryMatch = lookup(profile.categoryScores(), candidate.categoryId());
		double brandMatch = candidate.brandId() == null
				? 0.0
				: lookup(profile.brandScores(), candidate.brandId());
		double shopMatch = lookup(profile.shopScores(), candidate.shopId());

		double priceAffinity = 0.5;
		Double price = candidate.effectivePrice();
		Double p25 = profile.priceP25();
		Double p75 = profile.priceP75();
		if (price != null && p25 != null && p75 != null) {
			double iqr = p75 - p25;
			if (iqr > 0) {
				if (price >= p25 && price <= p75) {
					priceAffinity = 1.0;
				} else if (price < p25) {
					priceAffinity = clip01(1.0 - (p25 - price) / iqr);
				} else {
					priceAffinity = clip01(1.0 - (price - p75) / iqr);
				}
			}
		}

		double cross = clip01(candidate.arScore() == null ? 0.0 : candidate.arScore());
		double cf = clip01(candidate.cfScore() == null ? 0.0 : candidate.cfScore());
		double semantic = 0.0;

		Set<String> sources = candidate.sources() == null ? Set.of() : candidate.sources();
		boolean popular = containsSource(sources, "POPULAR");
		boolean personal = containsSource(sources, "PERSONAL");
		boolean isCf = containsSource(sources, "CF");
		boolean crossDomain = containsSource(sources, "CROSS_DOMAIN");

		double[] vector = new double[]{
				recency,
				popularity,
				rating,
				categoryMatch,
				brandMatch,
				shopMatch,
				priceAffinity,
				cross,
				cf,
				semantic,
				popular ? 1.0 : 0.0,
				personal ? 1.0 : 0.0,
				isCf ? 1.0 : 0.0,
				crossDomain ? 1.0 : 0.0,
				0.0
		};
		if (vector.length != HomeFeatureOrder.DIM) {
			throw new IllegalStateException("Home feature dim mismatch");
		}
		return vector;
	}

	private static double lookup(Map<UUID, Double> map, UUID key) {
		if (key == null || map == null) {
			return 0.0;
		}
		Double v = map.get(key);
		return v == null ? 0.0 : v;
	}

	private static boolean containsSource(Set<String> sources, String name) {
		for (String s : sources) {
			if (s != null && s.equalsIgnoreCase(name)) {
				return true;
			}
		}
		return false;
	}

	private static double clip01(double x) {
		return Math.max(0.0, Math.min(1.0, x));
	}
}
