package com.twohands.commerce_service.unit.application.product;

import com.twohands.commerce_service.application.product.viewproductdetail.ViewProductDetailCommand;
import com.twohands.commerce_service.application.product.viewproductdetail.ViewProductDetailUseCase;
import com.twohands.commerce_service.domain.product.ProductStatus;
import com.twohands.commerce_service.domain.product.ViewProductDetailAttributeItem;
import com.twohands.commerce_service.domain.product.ViewProductDetailCategory;
import com.twohands.commerce_service.domain.product.ViewProductDetailInventorySummary;
import com.twohands.commerce_service.domain.product.ViewProductDetailMediaItem;
import com.twohands.commerce_service.domain.product.ViewProductDetailRepository;
import com.twohands.commerce_service.domain.product.ViewProductDetailResult;
import com.twohands.commerce_service.domain.product.ViewProductDetailShop;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ViewProductDetailUseCaseTest {

    @Mock
    private ViewProductDetailRepository viewProductDetailRepository;

    private ViewProductDetailUseCase useCase;

    private final UUID productId = UUID.randomUUID();
    private final Instant now = Instant.parse("2026-05-21T10:00:00Z");
    private final Clock clock = Clock.fixed(now, ZoneOffset.UTC);

    @BeforeEach
    void setUp() {
        useCase = new ViewProductDetailUseCase(viewProductDetailRepository, clock);
    }

    @Test
    void shouldReturnVisibleProductDetail() {
        when(viewProductDetailRepository.findVisibleByProductId(eq(productId), eq(now)))
                .thenReturn(Optional.of(sampleResult()));

        ViewProductDetailResult result = useCase.execute(new ViewProductDetailCommand(productId));

        assertThat(result.productId()).isEqualTo(productId);
        assertThat(result.status()).isEqualTo(ProductStatus.ACTIVE);
        assertThat(result.media()).hasSize(1);
        assertThat(result.attributes()).hasSize(1);
        assertThat(result.inventorySummary().inStock()).isTrue();
        assertThat(result.ratingCount()).isEqualTo(3);
    }

    @Test
    void shouldRejectWhenProductNotVisible() {
        when(viewProductDetailRepository.findVisibleByProductId(eq(productId), eq(now)))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(new ViewProductDetailCommand(productId)))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.PRODUCT_NOT_FOUND);
    }

    private ViewProductDetailResult sampleResult() {
        UUID categoryId = UUID.randomUUID();
        UUID shopId = UUID.randomUUID();
        return new ViewProductDetailResult(
                productId,
                "iPhone 15",
                "Like new device",
                "LIKE_NEW",
                200,
                ProductStatus.ACTIVE,
                new ViewProductDetailCategory(categoryId, "Phones", "phones"),
                new ViewProductDetailShop(shopId, "Tech Shop", null, null),
                List.of(new ViewProductDetailMediaItem(
                        UUID.randomUUID(),
                        "http://localhost:9000/2hands-commerce-product/p1.jpg",
                        "IMAGE",
                        0
                )),
                List.of(new ViewProductDetailAttributeItem("color", "black")),
                BigDecimal.valueOf(20_000_000),
                BigDecimal.valueOf(18_000_000),
                BigDecimal.valueOf(18_000_000),
                new ViewProductDetailInventorySummary(5, 2, true, false),
                BigDecimal.valueOf(4.67),
                3,
                false,
                null
        );
    }
}
