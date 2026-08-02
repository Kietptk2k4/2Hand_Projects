package com.twohands.commerce_service.domain.home;

/**
 * Fixed train-time popularity scale. Must NOT min-max within a request candidate pool.
 */
public final class PopularityNormalizer {

	private final double zLo;
	private final double zHi;
	private final double eps;

	public PopularityNormalizer(double zLo, double zHi) {
		this(zLo, zHi, 1e-9);
	}

	public PopularityNormalizer(double zLo, double zHi, double eps) {
		this.zLo = zLo;
		this.zHi = zHi;
		this.eps = eps;
	}

	public double normalize(double z) {
		double denom = (zHi - zLo) + eps;
		double score = (z - zLo) / denom;
		return clip01(score);
	}

	public static double log1pRaw(long raw) {
		return Math.log1p(Math.max(0L, raw));
	}

	public double zLo() {
		return zLo;
	}

	public double zHi() {
		return zHi;
	}

	private static double clip01(double x) {
		return Math.max(0.0, Math.min(1.0, x));
	}
}
