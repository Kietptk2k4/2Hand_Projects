package com.twohands.commerce_service.application.home.recommendproducts;

import com.twohands.commerce_service.domain.home.CandidateProduct;
import com.twohands.commerce_service.domain.home.HomeFeatureOrder;
import com.twohands.commerce_service.domain.home.HomeInteractionRepository;
import com.twohands.commerce_service.domain.home.HomeProductSnapshot;
import com.twohands.commerce_service.domain.home.HomeRecommendationReadRepository;
import com.twohands.commerce_service.domain.home.HomeRecommendationResult;
import com.twohands.commerce_service.domain.home.RankingMode;
import com.twohands.commerce_service.domain.home.RetrievalSource;
import com.twohands.commerce_service.domain.home.UserInterestProfile;
import com.twohands.commerce_service.domain.home.PopularityNormalizer;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import com.twohands.commerce_service.infrastructure.model.HomeModelLoader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RecommendCommerceHomeUseCase {

    private static final int SOURCE_A_NEW_LIMIT = 40;
    private static final int SOURCE_A_POPULAR_LIMIT = 40;
    private static final int SOURCE_A_RATING_LIMIT = 20;
    private static final int SOURCE_A_CAP = 100;
    private static final int SOURCE_B_CAP = 150;
    private static final int SOURCE_C_CAP = 150;
    private static final int SOURCE_E_CAP = 150;
    private static final int POOL_CAP = 500;
    private static final int TOP_CATEGORY_COUNT = 20;
    private static final int TOP_BRAND_COUNT = 20;
    private static final int TOP_SHOP_COUNT = 10;
    private static final Duration PROFILE_WINDOW = Duration.ofDays(180);
    private static final Duration POPULAR_WINDOW = Duration.ofDays(90);
    private static final double COMPLETED_WEIGHT = 1.0;
    private static final double CART_WEIGHT = 0.6;

    private final HomeRecommendationReadRepository readRepository;
    private final HomeInteractionRepository interactionRepository;
    private final HomeModelLoader homeModelLoader;
    private final Clock clock;
    private final HomeDiversityReranker diversityReranker = new HomeDiversityReranker();

    @Value("${commerce.home.recommend.enabled:false}")
    private boolean enabled;

    @Value("${commerce.home.diversity.enabled:true}")
    private boolean diversityEnabled;

    @Value("${commerce.home.diversity.k:50}")
    private int topK;

    @Value("${commerce.home.diversity.max-per-category:8}")
    private int maxPerCategory;

    @Value("${commerce.home.diversity.max-per-brand:5}")
    private int maxPerBrand;

    @Value("${commerce.home.diversity.max-per-shop:4}")
    private int maxPerShop;

    @Value("${commerce.cf.max-neighbors:30}")
    private int cfMaxNeighbors;

    @Value("${commerce.home.cf.max-products-per-entity:20}")
    private int cfMaxProductsPerEntity;

    @Value("${commerce.home.ar.max-products-per-category:20}")
    private int arMaxProductsPerCategory;

    @Value("${commerce.home.ar.min-confidence:0.05}")
    private double arMinConfidence;

    @Value("${commerce.home.personal.fetch-limit:250}")
    private int personalFetchLimit;

    public RecommendCommerceHomeUseCase(
            HomeRecommendationReadRepository readRepository,
            HomeInteractionRepository interactionRepository,
            HomeModelLoader homeModelLoader,
            Clock clock
    ) {
        this.readRepository = readRepository;
        this.interactionRepository = interactionRepository;
        this.homeModelLoader = homeModelLoader;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public HomeRecommendationResult execute(UUID userId) {
        if (!enabled) {
            throw new AppException(ErrorCode.HOME_RECOMMEND_DISABLED);
        }

        Instant asOf = clock.instant();
        HomeModelLoader.HomeModelRuntime runtime = homeModelLoader.resolveRuntime();
        UserInterestProfile profile = buildProfile(userId, asOf);
        List<CandidateProduct> candidates = buildCandidatePool(userId, profile, asOf);
        String requestId = UUID.randomUUID().toString();

        if (candidates.isEmpty()) {
            return new HomeRecommendationResult(
                    requestId,
                    runtime.rankingMode(),
                    runtime.modelName(),
                    runtime.modelVersion(),
                    List.of()
            );
        }

        List<HomeDiversityReranker.ScoredCandidate> scored = scoreCandidates(candidates, profile, asOf, runtime);
        List<HomeDiversityReranker.ScoredCandidate> diversified = diversityReranker.rerank(
                scored,
                topK,
                diversityEnabled,
                maxPerCategory,
                maxPerBrand,
                maxPerShop
        );

        List<HomeInteractionRepository.ServedCandidate> served = new ArrayList<>(diversified.size());
        List<HomeRecommendationResult.Item> items = new ArrayList<>(diversified.size());
        for (int i = 0; i < diversified.size(); i++) {
            CandidateProduct candidate = diversified.get(i).candidate();
            served.add(new HomeInteractionRepository.ServedCandidate(candidate, i + 1));
            items.add(toItem(candidate.product()));
        }

        interactionRepository.saveImpressions(
                userId,
                asOf,
                requestId,
                runtime.rankingMode(),
                runtime.modelName(),
                runtime.modelVersion(),
                served
        );

        return new HomeRecommendationResult(
                requestId,
                runtime.rankingMode(),
                runtime.modelName(),
                runtime.modelVersion(),
                items
        );
    }

    public String successMessage() {
        return "Lay goi y trang chu thanh cong.";
    }

    private UserInterestProfile buildProfile(UUID userId, Instant asOf) {
        Instant since = asOf.minus(PROFILE_WINDOW);
        Map<UUID, Double> categoryScores = new HashMap<>();
        Map<UUID, Double> brandScores = new HashMap<>();
        Map<UUID, Double> shopScores = new HashMap<>();
        List<BigDecimal> completedPrices = new ArrayList<>();

        for (HomeRecommendationReadRepository.CommerceInteraction interaction
                : readRepository.findCompletedInteractions(userId, since, asOf)) {
            double weight = interaction.baseWeight() * decayWeight(interaction.occurredAt(), asOf, 30);
            addScore(categoryScores, interaction.categoryId(), weight);
            addScore(brandScores, interaction.brandId(), weight);
            addScore(shopScores, interaction.shopId(), weight);
            if (interaction.unitPrice() != null) {
                completedPrices.add(interaction.unitPrice());
            }
        }

        for (HomeRecommendationReadRepository.CommerceInteraction interaction
                : readRepository.findCartInteractions(userId, since, asOf)) {
            double weight = interaction.baseWeight() * decayWeight(interaction.occurredAt(), asOf, 30);
            addScore(categoryScores, interaction.categoryId(), weight);
            addScore(brandScores, interaction.brandId(), weight);
            addScore(shopScores, interaction.shopId(), weight);
        }

        Map<String, Double> hashtagScores = maxNormSocial(
                readRepository.findSocialSignals(userId).stream()
                        .filter(signal -> "HASHTAG".equalsIgnoreCase(signal.tagType()))
                        .toList()
        );
        Map<String, Double> keywordScores = maxNormSocial(
                readRepository.findSocialSignals(userId).stream()
                        .filter(signal -> "KEYWORD".equalsIgnoreCase(signal.tagType()))
                        .toList()
        );

        PriceStats priceStats = calculatePriceStats(completedPrices);
        return new UserInterestProfile(
                topN(maxNorm(categoryScores), TOP_CATEGORY_COUNT),
                topN(maxNorm(brandScores), TOP_BRAND_COUNT),
                topN(maxNorm(shopScores), TOP_SHOP_COUNT),
                hashtagScores,
                keywordScores,
                priceStats.p25(),
                priceStats.p50(),
                priceStats.p75()
        );
    }

    private List<CandidateProduct> buildCandidatePool(UUID userId, UserInterestProfile profile, Instant asOf) {
        LinkedHashMap<UUID, MutableCandidate> merged = new LinkedHashMap<>();

        sourceA(userId, asOf).forEach(candidate -> mergeCandidate(merged, candidate));
        sourceB(userId, profile, asOf).forEach(candidate -> mergeCandidate(merged, candidate));
        sourceE(userId, profile, asOf).forEach(candidate -> mergeCandidate(merged, candidate));
        sourceC(userId, profile, asOf).forEach(candidate -> mergeCandidate(merged, candidate));

        return merged.values().stream()
                .map(MutableCandidate::freeze)
                .filter(this::isEligible)
                .limit(POOL_CAP)
                .toList();
    }

    private List<CandidateProduct> sourceA(UUID userId, Instant asOf) {
        LinkedHashMap<UUID, MutableCandidate> source = new LinkedHashMap<>();
        readRepository.findNewestProducts(userId, asOf, SOURCE_A_NEW_LIMIT)
                .forEach(snapshot -> mergeCandidate(source, popularCandidate(snapshot)));
        readRepository.findPopularProducts90d(userId, asOf.minus(POPULAR_WINDOW), asOf, SOURCE_A_POPULAR_LIMIT)
                .forEach(snapshot -> mergeCandidate(source, popularCandidate(snapshot)));
        readRepository.findTopRatedProducts(userId, asOf, SOURCE_A_RATING_LIMIT)
                .forEach(snapshot -> mergeCandidate(source, popularCandidate(snapshot)));
        return source.values().stream().map(MutableCandidate::freeze).limit(SOURCE_A_CAP).toList();
    }

    private List<CandidateProduct> sourceB(UUID userId, UserInterestProfile profile, Instant asOf) {
        LinkedHashMap<UUID, MutableCandidate> merged = new LinkedHashMap<>();
        readRepository.findProductsByCategories(profile.categoryScores().keySet(), userId, asOf, personalFetchLimit)
                .forEach(snapshot -> mergeCandidate(merged, personalCandidate(snapshot, profile)));
        readRepository.findProductsByBrands(profile.brandScores().keySet(), userId, asOf, personalFetchLimit)
                .forEach(snapshot -> mergeCandidate(merged, personalCandidate(snapshot, profile)));
        readRepository.findProductsByShops(profile.shopScores().keySet(), userId, asOf, personalFetchLimit)
                .forEach(snapshot -> mergeCandidate(merged, personalCandidate(snapshot, profile)));

        return merged.values().stream()
                .map(MutableCandidate::freeze)
                .filter(candidate -> candidate.personalScore() != null && candidate.personalScore() > 0)
                .sorted(Comparator
                        .comparing((CandidateProduct candidate) -> candidate.personalScore(), Comparator.reverseOrder())
                        .thenComparing(candidate -> candidate.product().createdAt(), Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(candidate -> candidate.product().productId()))
                .limit(SOURCE_B_CAP)
                .toList();
    }

    private List<CandidateProduct> sourceE(UUID userId, UserInterestProfile profile, Instant asOf) {
        Map<UUID, Double> rawByProduct = new HashMap<>();
        Map<UUID, MutableCandidate> merged = new LinkedHashMap<>();

        Map<UUID, Double> topCategories = topN(profile.categoryScores(), TOP_CATEGORY_COUNT);
        Map<UUID, Double> topBrands = topN(profile.brandScores(), TOP_BRAND_COUNT);

        for (HomeRecommendationReadRepository.EntityNeighbor neighbor
                : readRepository.findCategoryNeighbors(topCategories.keySet(), cfMaxNeighbors)) {
            Double seedStrength = topCategories.get(neighbor.seedId());
            if (seedStrength == null || seedStrength <= 0) {
                continue;
            }
            double raw = neighbor.score() * seedStrength;
            if (raw <= 0) {
                continue;
            }
            readRepository.findProductsByCategories(List.of(neighbor.neighborId()), userId, asOf, cfMaxProductsPerEntity)
                    .forEach(snapshot -> {
                        rawByProduct.merge(snapshot.productId(), raw, Math::max);
                        mergeCandidate(merged, cfCandidate(snapshot, raw));
                    });
        }

        for (HomeRecommendationReadRepository.EntityNeighbor neighbor
                : readRepository.findBrandNeighbors(topBrands.keySet(), cfMaxNeighbors)) {
            Double seedStrength = topBrands.get(neighbor.seedId());
            if (seedStrength == null || seedStrength <= 0) {
                continue;
            }
            double raw = neighbor.score() * seedStrength;
            if (raw <= 0) {
                continue;
            }
            readRepository.findProductsByBrands(List.of(neighbor.neighborId()), userId, asOf, cfMaxProductsPerEntity)
                    .forEach(snapshot -> {
                        rawByProduct.merge(snapshot.productId(), raw, Math::max);
                        mergeCandidate(merged, cfCandidate(snapshot, raw));
                    });
        }

        double maxRaw = rawByProduct.values().stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
        return merged.values().stream()
                .map(candidate -> candidate.freezeCf(maxRaw))
                .sorted(Comparator
                        .comparing((CandidateProduct candidate) -> candidate.cfScore(), Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(candidate -> candidate.product().createdAt(), Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(candidate -> candidate.product().productId()))
                .limit(SOURCE_E_CAP)
                .toList();
    }

    private List<CandidateProduct> sourceC(UUID userId, UserInterestProfile profile, Instant asOf) {
        List<UserInterestProfile.TagScore> socialTags = profile.allSocialTags();
        if (socialTags.isEmpty()) {
            return List.of();
        }

        Map<String, Double> hashtagScores = profile.hashtagScores();
        Map<String, Double> keywordScores = profile.keywordScores();
        List<HomeRecommendationReadRepository.AssociationRule> hashtagRules = readRepository.findAssociationRules(
                "HASHTAG",
                hashtagScores.keySet(),
                arMinConfidence
        );
        List<HomeRecommendationReadRepository.AssociationRule> keywordRules = readRepository.findAssociationRules(
                "KEYWORD",
                keywordScores.keySet(),
                arMinConfidence
        );

        Map<UUID, MutableCandidate> merged = new LinkedHashMap<>();
        applyAssociationRules(userId, asOf, merged, hashtagRules, hashtagScores);
        applyAssociationRules(userId, asOf, merged, keywordRules, keywordScores);

        return merged.values().stream()
                .map(MutableCandidate::freeze)
                .filter(candidate -> candidate.arScore() != null && candidate.arScore() > 0)
                .sorted(Comparator
                        .comparing((CandidateProduct candidate) -> candidate.arScore(), Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(candidate -> candidate.product().createdAt(), Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(candidate -> candidate.product().productId()))
                .limit(SOURCE_C_CAP)
                .toList();
    }

    private void applyAssociationRules(
            UUID userId,
            Instant asOf,
            Map<UUID, MutableCandidate> merged,
            List<HomeRecommendationReadRepository.AssociationRule> rules,
            Map<String, Double> tagScores
    ) {
        for (HomeRecommendationReadRepository.AssociationRule rule : rules) {
            Double tagScore = tagScores.get(rule.tag());
            if (tagScore == null || tagScore <= 0) {
                continue;
            }
            double arScore = clip01(rule.confidence() * tagScore);
            readRepository.findProductsByCategories(List.of(rule.categoryId()), userId, asOf, arMaxProductsPerCategory)
                    .forEach(snapshot -> mergeCandidate(merged, arCandidate(snapshot, arScore)));
        }
    }

    private List<HomeDiversityReranker.ScoredCandidate> scoreCandidates(
            List<CandidateProduct> candidates,
            UserInterestProfile profile,
            Instant asOf,
            HomeModelLoader.HomeModelRuntime runtime
    ) {
        List<double[]> featureVectors = new ArrayList<>(candidates.size());
        for (CandidateProduct candidate : candidates) {
            featureVectors.add(buildFeatureVector(candidate, profile, runtime.normalizer(), asOf));
        }

        List<Double> scores;
        if (runtime.rankingMode() == RankingMode.LIGHTGBM) {
            scores = homeModelLoader.scoreBatch(featureVectors);
        } else {
            scores = featureVectors.stream()
                    .map(vector -> (0.7 * vector[1]) + (0.3 * vector[0]))
                    .toList();
        }

        List<HomeDiversityReranker.ScoredCandidate> scored = new ArrayList<>(candidates.size());
        for (int i = 0; i < candidates.size(); i++) {
            scored.add(new HomeDiversityReranker.ScoredCandidate(candidates.get(i), scores.get(i), featureVectors.get(i)));
        }

        scored.sort(Comparator
                .comparing(HomeDiversityReranker.ScoredCandidate::score, Comparator.reverseOrder())
                .thenComparing(candidate -> candidate.candidate().product().createdAt(), Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(candidate -> candidate.candidate().product().productId()));
        return scored;
    }

    private double[] buildFeatureVector(
            CandidateProduct candidate,
            UserInterestProfile profile,
            PopularityNormalizer normalizer,
            Instant asOf
    ) {
        HomeProductSnapshot product = candidate.product();
        HomeFeatureBuilder.CandidateInput input = new HomeFeatureBuilder.CandidateInput(
                product.productId(),
                product.categoryId(),
                product.brandId(),
                product.shopId(),
                product.effectivePrice() == null ? null : product.effectivePrice().doubleValue(),
                product.createdAt(),
                product.ratingAvg() == null ? null : product.ratingAvg().doubleValue(),
                product.ratingCount(),
                product.popularityRaw(),
                candidate.sources().stream().map(Enum::name).collect(Collectors.toSet()),
                candidate.personalScore(),
                candidate.cfScore(),
                candidate.arScore()
        );
        HomeFeatureBuilder.ProfileInput profileInput = new HomeFeatureBuilder.ProfileInput(
                profile.categoryScores(),
                profile.brandScores(),
                profile.shopScores(),
                profile.priceP25() == null ? null : profile.priceP25().doubleValue(),
                profile.priceP75() == null ? null : profile.priceP75().doubleValue()
        );
        double[] vector = HomeFeatureBuilder.build(input, profileInput, normalizer, asOf);
        if (vector.length != HomeFeatureOrder.DIM) {
            throw new IllegalStateException("Unexpected home feature vector dimension");
        }
        return vector;
    }

    private CandidateProduct popularCandidate(HomeProductSnapshot snapshot) {
        return new CandidateProduct(snapshot, EnumSet.of(RetrievalSource.POPULAR), null, null, null);
    }

    private CandidateProduct personalCandidate(HomeProductSnapshot snapshot, UserInterestProfile profile) {
        double personalScore = Math.max(
                profile.categoryScores().getOrDefault(snapshot.categoryId(), 0.0),
                Math.max(
                        snapshot.brandId() == null ? 0.0 : profile.brandScores().getOrDefault(snapshot.brandId(), 0.0),
                        profile.shopScores().getOrDefault(snapshot.shopId(), 0.0)
                )
        );
        return new CandidateProduct(snapshot, EnumSet.of(RetrievalSource.PERSONAL), personalScore, null, null);
    }

    private CandidateProduct cfCandidate(HomeProductSnapshot snapshot, double rawScore) {
        return new CandidateProduct(snapshot, EnumSet.of(RetrievalSource.CF), null, rawScore, null);
    }

    private CandidateProduct arCandidate(HomeProductSnapshot snapshot, double arScore) {
        return new CandidateProduct(snapshot, EnumSet.of(RetrievalSource.CROSS_DOMAIN), null, null, arScore);
    }

    private void mergeCandidate(Map<UUID, MutableCandidate> merged, CandidateProduct candidate) {
        MutableCandidate existing = merged.get(candidate.product().productId());
        if (existing == null) {
            merged.put(candidate.product().productId(), new MutableCandidate(candidate));
            return;
        }
        existing.merge(candidate);
    }

    private boolean isEligible(CandidateProduct candidate) {
        HomeProductSnapshot product = candidate.product();
        return product != null
                && product.inStock()
                && !product.shopVacation()
                && product.sellerId() != null;
    }

    private HomeRecommendationResult.Item toItem(HomeProductSnapshot product) {
        HomeRecommendationResult.Rating rating = product.ratingCount() > 0 && product.ratingAvg() != null
                ? new HomeRecommendationResult.Rating(product.ratingAvg(), product.ratingCount())
                : null;
        return new HomeRecommendationResult.Item(
                product.productId(),
                product.title(),
                product.listPrice(),
                product.salePrice(),
                product.effectivePrice(),
                product.thumbnailUrl(),
                new HomeRecommendationResult.Shop(product.shopId(), product.shopName()),
                rating
        );
    }

    private static double decayWeight(Instant occurredAt, Instant asOf, int halfLifeDays) {
        if (occurredAt == null) {
            return 0.0;
        }
        double deltaSeconds = Math.max(0.0, asOf.getEpochSecond() - occurredAt.getEpochSecond());
        return Math.pow(2.0, -deltaSeconds / (halfLifeDays * 86400.0));
    }

    private static void addScore(Map<UUID, Double> scores, UUID key, double weight) {
        if (key == null || weight <= 0.0) {
            return;
        }
        scores.merge(key, weight, Double::sum);
    }

    private static Map<UUID, Double> maxNorm(Map<UUID, Double> raw) {
        double max = raw.values().stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
        if (max <= 0.0) {
            return Map.of();
        }
        return raw.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue() / max
                ));
    }

    private static Map<String, Double> maxNormSocial(List<HomeRecommendationReadRepository.SocialTagSignal> signals) {
        Map<String, Double> raw = new HashMap<>();
        for (HomeRecommendationReadRepository.SocialTagSignal signal : signals) {
            raw.put(signal.tag(), signal.score());
        }
        double max = raw.values().stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
        if (max <= 0.0) {
            return Map.of();
        }
        return raw.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue() / max
                ));
    }

    private static <K> Map<K, Double> topN(Map<K, Double> values, int limit) {
        return values.entrySet().stream()
                .sorted(Map.Entry.<K, Double>comparingByValue().reversed())
                .limit(limit)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (left, right) -> left,
                        LinkedHashMap::new
                ));
    }

    private static double clip01(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }

    private static PriceStats calculatePriceStats(List<BigDecimal> samples) {
        if (samples.size() < 3) {
            return new PriceStats(null, null, null);
        }
        List<BigDecimal> sorted = samples.stream().sorted().toList();
        return new PriceStats(
                percentile(sorted, 0.25),
                percentile(sorted, 0.50),
                percentile(sorted, 0.75)
        );
    }

    private static BigDecimal percentile(List<BigDecimal> sorted, double percentile) {
        if (sorted.size() == 1) {
            return sorted.getFirst();
        }
        double index = percentile * (sorted.size() - 1);
        int lower = (int) Math.floor(index);
        int upper = (int) Math.ceil(index);
        if (lower == upper) {
            return sorted.get(lower);
        }
        BigDecimal lowerValue = sorted.get(lower);
        BigDecimal upperValue = sorted.get(upper);
        BigDecimal weight = BigDecimal.valueOf(index - lower);
        return lowerValue.multiply(BigDecimal.ONE.subtract(weight)).add(upperValue.multiply(weight));
    }

    private record PriceStats(BigDecimal p25, BigDecimal p50, BigDecimal p75) {
    }

    private static final class MutableCandidate {
        private final HomeProductSnapshot product;
        private final EnumSet<RetrievalSource> sources = EnumSet.noneOf(RetrievalSource.class);
        private Double personalScore;
        private Double cfScore;
        private Double arScore;

        private MutableCandidate(CandidateProduct candidate) {
            this.product = candidate.product();
            this.sources.addAll(candidate.sources());
            this.personalScore = candidate.personalScore();
            this.cfScore = candidate.cfScore();
            this.arScore = candidate.arScore();
        }

        private void merge(CandidateProduct candidate) {
            this.sources.addAll(candidate.sources());
            if (candidate.personalScore() != null) {
                this.personalScore = this.personalScore == null
                        ? candidate.personalScore()
                        : Math.max(this.personalScore, candidate.personalScore());
            }
            if (candidate.cfScore() != null) {
                this.cfScore = this.cfScore == null
                        ? candidate.cfScore()
                        : Math.max(this.cfScore, candidate.cfScore());
            }
            if (candidate.arScore() != null) {
                this.arScore = this.arScore == null
                        ? candidate.arScore()
                        : Math.max(this.arScore, candidate.arScore());
            }
        }

        private CandidateProduct freezeCf(double maxRaw) {
            Double normalizedCf = cfScore == null || cfScore <= 0 || maxRaw <= 0
                    ? 0.0
                    : cfScore / maxRaw;
            return new CandidateProduct(product, EnumSet.copyOf(sources), personalScore, normalizedCf, arScore);
        }

        private CandidateProduct freeze() {
            return new CandidateProduct(product, EnumSet.copyOf(sources), personalScore, cfScore, arScore);
        }
    }
}
