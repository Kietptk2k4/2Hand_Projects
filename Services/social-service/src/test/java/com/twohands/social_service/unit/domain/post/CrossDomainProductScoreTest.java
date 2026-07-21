package com.twohands.social_service.unit.domain.post;

import com.twohands.social_service.domain.integration.UserProductAffinity;
import com.twohands.social_service.domain.post.CrossDomainProductScore;
import com.twohands.social_service.domain.post.ProductTag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;

class CrossDomainProductScoreTest {

    @Test
    void shouldScoreCategoryOverlap() {
        UserProductAffinity affinity = new UserProductAffinity(Set.of("cat-a"), Set.of());
        ProductTag tag = new ProductTag("p1", BigDecimal.ONE, "n", null, "Laptop", "cat-a", "shop-x", true);
        assertThat(CrossDomainProductScore.compute(affinity, List.of(tag)))
                .isCloseTo(0.6, offset(1e-9));
    }

    @Test
    void shouldScoreShopOverlap() {
        UserProductAffinity affinity = new UserProductAffinity(Set.of(), Set.of("shop-1"));
        ProductTag tag = new ProductTag("p1", BigDecimal.ONE, "n", null, null, "cat-z", "shop-1", true);
        assertThat(CrossDomainProductScore.compute(affinity, List.of(tag)))
                .isCloseTo(0.4, offset(1e-9));
    }

    @Test
    void shouldReturnZeroWhenNoProfile() {
        ProductTag tag = new ProductTag("p1", BigDecimal.ONE, "n", null, null, "cat-a", "shop-1", true);
        assertThat(CrossDomainProductScore.compute(UserProductAffinity.empty(), List.of(tag))).isZero();
    }

    @Test
    void shouldReturnZeroWhenNoProductTags() {
        UserProductAffinity affinity = new UserProductAffinity(Set.of("cat-a"), Set.of("shop-1"));
        assertThat(CrossDomainProductScore.compute(affinity, List.of())).isZero();
        assertThat(CrossDomainProductScore.compute(affinity, null)).isZero();
    }
}
