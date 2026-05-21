package com.twohands.commerce_service.unit.application.product;

import com.twohands.commerce_service.application.product.common.ProductCreatedOutboxService;
import com.twohands.commerce_service.application.product.createproduct.CreateProductCommand;
import com.twohands.commerce_service.application.product.createproduct.CreateProductUseCase;
import com.twohands.commerce_service.domain.catalog.ProductCategoryRepository;
import com.twohands.commerce_service.domain.outbox.OutboxEvent;
import com.twohands.commerce_service.domain.outbox.OutboxEventRepository;
import com.twohands.commerce_service.domain.product.CreateProductDraft;
import com.twohands.commerce_service.domain.product.CreateProductRepository;
import com.twohands.commerce_service.domain.product.CreateProductResult;
import com.twohands.commerce_service.domain.product.ProductStatus;
import com.twohands.commerce_service.domain.shop.SellerShop;
import com.twohands.commerce_service.domain.shop.SellerShopRepository;
import com.twohands.commerce_service.domain.shop.ShopStatus;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateProductUseCaseTest {

    @Mock
    private SellerShopRepository sellerShopRepository;

    @Mock
    private ProductCategoryRepository productCategoryRepository;

    @Mock
    private CreateProductRepository createProductRepository;

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Mock
    private ProductCreatedOutboxService productCreatedOutboxService;

    @InjectMocks
    private CreateProductUseCase useCase;

    private final UUID sellerId = UUID.randomUUID();
    private final UUID shopId = UUID.randomUUID();
    private final UUID categoryId = UUID.randomUUID();
    private final UUID productId = UUID.randomUUID();
    private final Instant now = Instant.parse("2026-05-21T10:00:00Z");

    @BeforeEach
    void setUp() {
        useCase = new CreateProductUseCase(
                sellerShopRepository,
                productCategoryRepository,
                createProductRepository,
                outboxEventRepository,
                productCreatedOutboxService,
                Clock.fixed(now, ZoneOffset.UTC)
        );
    }

    @Test
    void shouldCreateDraftProductForActiveShop() {
        when(sellerShopRepository.findBySellerId(sellerId))
                .thenReturn(Optional.of(new SellerShop(shopId, sellerId, ShopStatus.ACTIVE)));
        when(productCategoryRepository.existsActiveById(categoryId)).thenReturn(true);
        when(createProductRepository.create(any(CreateProductDraft.class), eq(now)))
                .thenReturn(createdResult());
        when(productCreatedOutboxService.build(any(), any(), any(), any(), any()))
                .thenReturn(sampleOutbox());

        CreateProductResult result = useCase.execute(validCommand());

        assertThat(result.status()).isEqualTo(ProductStatus.DRAFT);
        assertThat(result.shopId()).isEqualTo(shopId);

        ArgumentCaptor<CreateProductDraft> draftCaptor = ArgumentCaptor.forClass(CreateProductDraft.class);
        verify(createProductRepository).create(draftCaptor.capture(), eq(now));
        assertThat(draftCaptor.getValue().sellerId()).isEqualTo(sellerId);
        assertThat(draftCaptor.getValue().shopId()).isEqualTo(shopId);
        assertThat(draftCaptor.getValue().productType()).isEqualTo("PHYSICAL");
        verify(outboxEventRepository).save(any(OutboxEvent.class));
    }

    @Test
    void shouldRejectWhenSellerHasNoShop() {
        when(sellerShopRepository.findBySellerId(sellerId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(validCommand()))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.SELLER_SHOP_NOT_FOUND);

        verify(createProductRepository, never()).create(any(), any());
    }

    @Test
    void shouldRejectWhenShopSuspended() {
        when(sellerShopRepository.findBySellerId(sellerId))
                .thenReturn(Optional.of(new SellerShop(shopId, sellerId, ShopStatus.SUSPENDED)));

        assertThatThrownBy(() -> useCase.execute(validCommand()))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.SHOP_NOT_OPERATING);
    }

    @Test
    void shouldRejectWhenCategoryInactive() {
        when(sellerShopRepository.findBySellerId(sellerId))
                .thenReturn(Optional.of(new SellerShop(shopId, sellerId, ShopStatus.ACTIVE)));
        when(productCategoryRepository.existsActiveById(categoryId)).thenReturn(false);

        assertThatThrownBy(() -> useCase.execute(validCommand()))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.CATEGORY_NOT_FOUND);
    }

    @Test
    void shouldRejectWhenWeightInvalid() {
        CreateProductCommand command = new CreateProductCommand(
                sellerId,
                "PHYSICAL",
                categoryId,
                null,
                "NEW",
                "Phone",
                "Description",
                0
        );

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.VALIDATION_ERROR);
    }

    private CreateProductCommand validCommand() {
        return new CreateProductCommand(
                sellerId,
                "PHYSICAL",
                categoryId,
                null,
                "NEW",
                " iPhone 15 ",
                " Like new ",
                500
        );
    }

    private CreateProductResult createdResult() {
        return new CreateProductResult(
                productId,
                sellerId,
                shopId,
                ProductStatus.DRAFT,
                "PHYSICAL",
                categoryId,
                null,
                "NEW",
                "iPhone 15",
                "Like new",
                500,
                now,
                now
        );
    }

    private OutboxEvent sampleOutbox() {
        return new OutboxEvent(
                UUID.randomUUID(),
                ProductCreatedOutboxService.EVENT_TYPE,
                "product:created",
                productId,
                "commerce",
                "{}",
                com.twohands.commerce_service.domain.outbox.OutboxStatus.PENDING,
                0,
                now,
                null,
                null
        );
    }
}
