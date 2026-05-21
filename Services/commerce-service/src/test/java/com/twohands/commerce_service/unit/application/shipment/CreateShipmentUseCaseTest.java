package com.twohands.commerce_service.unit.application.shipment;

import com.twohands.commerce_service.application.shipment.createshipment.CreateShipmentCommand;
import com.twohands.commerce_service.application.shipment.createshipment.CreateShipmentTransactionService;
import com.twohands.commerce_service.application.shipment.createshipment.CreateShipmentUseCase;
import com.twohands.commerce_service.domain.payment.PaymentMethod;
import com.twohands.commerce_service.domain.payment.PaymentStatus;
import com.twohands.commerce_service.domain.shipment.BuyerDeliveryAddress;
import com.twohands.commerce_service.domain.shipment.CreateShipmentDraft;
import com.twohands.commerce_service.domain.shipment.CreateShipmentOrderContext;
import com.twohands.commerce_service.domain.shipment.CreateShipmentRepository;
import com.twohands.commerce_service.domain.shipment.CreateShipmentResult;
import com.twohands.commerce_service.domain.shipment.GhnCreateOrderResult;
import com.twohands.commerce_service.domain.shipment.GhnShipmentGateway;
import com.twohands.commerce_service.domain.shipment.SellerPickupAddress;
import com.twohands.commerce_service.domain.shipment.ShipmentCarrier;
import com.twohands.commerce_service.domain.shipment.ShipmentOrderItemLine;
import com.twohands.commerce_service.domain.shipment.ShipmentStatus;
import com.twohands.commerce_service.domain.shipping.SellerShippingProfile;
import com.twohands.commerce_service.domain.shipping.SellerShippingProfileRepository;
import com.twohands.commerce_service.domain.shipping.ShipmentType;
import com.twohands.commerce_service.domain.shop.SellerShop;
import com.twohands.commerce_service.domain.shop.SellerShopRepository;
import com.twohands.commerce_service.domain.shop.ShopStatus;
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
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
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
class CreateShipmentUseCaseTest {

    @Mock
    private CreateShipmentRepository createShipmentRepository;

    @Mock
    private SellerShopRepository sellerShopRepository;

    @Mock
    private SellerShippingProfileRepository sellerShippingProfileRepository;

    @Mock
    private GhnShipmentGateway ghnShipmentGateway;

    @Mock
    private CreateShipmentTransactionService createShipmentTransactionService;

    private CreateShipmentUseCase useCase;

    private final UUID sellerId = UUID.randomUUID();
    private final UUID shopId = UUID.randomUUID();
    private final UUID orderId = UUID.randomUUID();
    private final UUID orderItemId = UUID.randomUUID();
    private final UUID buyerId = UUID.randomUUID();
    private final Instant now = Instant.parse("2026-05-21T10:00:00Z");

    @BeforeEach
    void setUp() {
        useCase = new CreateShipmentUseCase(
                createShipmentRepository,
                sellerShopRepository,
                sellerShippingProfileRepository,
                ghnShipmentGateway,
                createShipmentTransactionService,
                Clock.fixed(now, ZoneOffset.UTC)
        );
    }

    @Test
    void shouldCreateManualShipmentForProcessingPaidOrder() {
        stubHappyPath(PaymentMethod.PAYOS, PaymentStatus.PAID, localResult(ShipmentCarrier.MANUAL, "TRK-001"));

        CreateShipmentResult result = useCase.execute(command("MANUAL", "STANDARD", null, "TRK-001"));

        assertThat(result.carrier()).isEqualTo(ShipmentCarrier.MANUAL);
        assertThat(result.trackingNumber()).isEqualTo("TRK-001");
        verify(ghnShipmentGateway, never()).createOrder(any());
    }

    @Test
    void shouldCreateGhnShipmentWithMockProvider() {
        CreateShipmentResult local = localResult(ShipmentCarrier.GHN, null);
        stubHappyPath(PaymentMethod.PAYOS, PaymentStatus.PAID, local);
        when(ghnShipmentGateway.createOrder(any())).thenReturn(new GhnCreateOrderResult(
                "GHN-MOCK-123",
                "SHOP-1",
                "TRK-GHN-1",
                "{\"mock\":true}",
                true
        ));

        CreateShipmentResult result = useCase.execute(command("GHN", "EXPRESS", 500, null));

        assertThat(result.ghnOrderCode()).isEqualTo("GHN-MOCK-123");
        assertThat(result.trackingNumber()).isEqualTo("TRK-GHN-1");
        verify(createShipmentTransactionService).updateGhnFields(eq(local.shipmentId()), any(GhnCreateOrderResult.class), eq(now));
    }

    @Test
    void shouldRejectWhenOrderNotProcessing() {
        when(createShipmentRepository.findOrderContext(orderId))
                .thenReturn(Optional.of(orderContext("AWAITING_PAYMENT", PaymentMethod.PAYOS, PaymentStatus.PAID)));

        assertThatThrownBy(() -> useCase.execute(command("MANUAL", "STANDARD", null, null)))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.ORDER_NOT_PROCESSING);
    }

    @Test
    void shouldRejectPayOsWhenPaymentNotPaid() {
        when(createShipmentRepository.findOrderContext(orderId))
                .thenReturn(Optional.of(orderContext("PROCESSING", PaymentMethod.PAYOS, PaymentStatus.PENDING)));

        assertThatThrownBy(() -> useCase.execute(command("MANUAL", "STANDARD", null, null)))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_PAYMENT_STATE);
    }

    @Test
    void shouldRejectWhenItemAlreadyShipped() {
        when(createShipmentRepository.findOrderContext(orderId))
                .thenReturn(Optional.of(orderContext("PROCESSING", PaymentMethod.PAYOS, PaymentStatus.PAID)));
        when(createShipmentRepository.findOrderItemsForSeller(orderId, sellerId, List.of(orderItemId)))
                .thenReturn(List.of(new ShipmentOrderItemLine(
                        orderItemId,
                        UUID.randomUUID(),
                        sellerId,
                        "PENDING",
                        UUID.randomUUID(),
                        1,
                        BigDecimal.valueOf(100_000),
                        BigDecimal.valueOf(20_000),
                        200
                )));

        assertThatThrownBy(() -> useCase.execute(command("MANUAL", "STANDARD", null, null)))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.ORDER_ITEM_ALREADY_SHIPPED);
    }

    private void stubHappyPath(PaymentMethod paymentMethod, PaymentStatus paymentStatus, CreateShipmentResult created) {
        when(createShipmentRepository.findOrderContext(orderId))
                .thenReturn(Optional.of(orderContext("PROCESSING", paymentMethod, paymentStatus)));
        when(createShipmentRepository.findOrderItemsForSeller(orderId, sellerId, List.of(orderItemId)))
                .thenReturn(List.of(fulfillableItem()));
        when(sellerShopRepository.findBySellerId(sellerId))
                .thenReturn(Optional.of(new SellerShop(shopId, sellerId, ShopStatus.ACTIVE)));
        when(sellerShippingProfileRepository.findByShopIds(List.of(shopId)))
                .thenReturn(Map.of(shopId, new SellerShippingProfile(shopId, sellerId, "79", "760", "26734")));
        when(createShipmentRepository.findSellerPickupBySellerId(sellerId))
                .thenReturn(Optional.of(pickupAddress()));
        when(createShipmentRepository.findBuyerDeliveryAddress(buyerId))
                .thenReturn(Optional.of(buyerAddress()));
        when(createShipmentTransactionService.createLocal(any(CreateShipmentDraft.class), eq(now)))
                .thenReturn(created);
    }

    private CreateShipmentCommand command(String carrier, String type, Integer weight, String tracking) {
        return new CreateShipmentCommand(
                sellerId,
                orderId,
                List.of(orderItemId),
                carrier,
                type,
                weight,
                tracking
        );
    }

    private CreateShipmentOrderContext orderContext(
            String status,
            PaymentMethod paymentMethod,
            PaymentStatus paymentStatus
    ) {
        return new CreateShipmentOrderContext(
                orderId,
                buyerId,
                status,
                paymentMethod,
                paymentStatus,
                BigDecimal.valueOf(120_000)
        );
    }

    private ShipmentOrderItemLine fulfillableItem() {
        return new ShipmentOrderItemLine(
                orderItemId,
                UUID.randomUUID(),
                sellerId,
                "PENDING",
                null,
                1,
                BigDecimal.valueOf(100_000),
                BigDecimal.valueOf(20_000),
                200
        );
    }

    private SellerPickupAddress pickupAddress() {
        return new SellerPickupAddress(
                shopId,
                sellerId,
                "Seller Pickup",
                "0901111111",
                "79",
                "760",
                "26734",
                "123 Seller St"
        );
    }

    private BuyerDeliveryAddress buyerAddress() {
        return new BuyerDeliveryAddress(
                UUID.randomUUID(),
                "Buyer",
                "0902222222",
                "01",
                "001",
                "00001",
                "456 Buyer St"
        );
    }

    private CreateShipmentResult localResult(ShipmentCarrier carrier, String tracking) {
        return new CreateShipmentResult(
                UUID.randomUUID(),
                orderId,
                sellerId,
                carrier,
                ShipmentType.STANDARD,
                ShipmentStatus.PENDING,
                carrier == ShipmentCarrier.GHN ? null : null,
                tracking,
                BigDecimal.valueOf(20_000),
                BigDecimal.ZERO,
                200,
                LocalDate.of(2026, 5, 24),
                List.of(orderItemId),
                now
        );
    }
}
