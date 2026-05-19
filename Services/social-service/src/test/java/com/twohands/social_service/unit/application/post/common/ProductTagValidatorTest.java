package com.twohands.social_service.unit.application.post.common;

import com.twohands.social_service.application.post.common.ProductTagValidationItem;
import com.twohands.social_service.application.post.common.ProductTagValidator;
import com.twohands.social_service.exception.AppException;
import com.twohands.social_service.exception.ErrorCode;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductTagValidatorTest {

    private final ProductTagValidator validator = new ProductTagValidator();

    @Test
    void shouldAcceptValidProductTags() {
        String productId = UUID.randomUUID().toString();

        assertThatCode(() -> validator.validate(List.of(
                new ProductTagValidationItem(productId, new BigDecimal("150000"))
        ))).doesNotThrowAnyException();
    }

    @Test
    void shouldAcceptNullPrice() {
        String productId = UUID.randomUUID().toString();

        assertThatCode(() -> validator.validate(List.of(
                new ProductTagValidationItem(productId, null)
        ))).doesNotThrowAnyException();
    }

    @Test
    void shouldRejectInvalidProductId() {
        assertThatThrownBy(() -> validator.validate(List.of(
                new ProductTagValidationItem("not-a-uuid", null)
        )))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> {
                    AppException appEx = (AppException) ex;
                    assertThat(appEx.getErrorCode()).isEqualTo(ErrorCode.VALIDATION_ERROR);
                    assertThat(appEx.getField()).isEqualTo("productTags[].product_id");
                });
    }

    @Test
    void shouldRejectNegativePrice() {
        assertThatThrownBy(() -> validator.validate(List.of(
                new ProductTagValidationItem(UUID.randomUUID().toString(), new BigDecimal("-1"))
        )))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> {
                    AppException appEx = (AppException) ex;
                    assertThat(appEx.getField()).isEqualTo("productTags[].price");
                });
    }

    @Test
    void shouldRejectDuplicateProductIds() {
        String productId = UUID.randomUUID().toString();

        assertThatThrownBy(() -> validator.validate(List.of(
                new ProductTagValidationItem(productId, null),
                new ProductTagValidationItem(productId, new BigDecimal("100"))
        )))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> {
                    AppException appEx = (AppException) ex;
                    assertThat(appEx.getField()).isEqualTo("productTags");
                });
    }

    @Test
    void shouldRejectTooManyProductTags() {
        List<ProductTagValidationItem> tags = IntStream.range(0, 11)
                .mapToObj(i -> new ProductTagValidationItem(UUID.randomUUID().toString(), null))
                .toList();

        assertThatThrownBy(() -> validator.validate(tags))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> {
                    AppException appEx = (AppException) ex;
                    assertThat(appEx.getField()).isEqualTo("productTags");
                });
    }
}
