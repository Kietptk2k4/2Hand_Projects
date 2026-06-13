package com.twohands.commerce_service.unit.application.shipment.common;

import com.twohands.commerce_service.application.shipment.common.ShipmentDeliveredOutboxService;
import com.twohands.commerce_service.application.shipment.common.ShipmentLifecycleOutboxEmitter;
import com.twohands.commerce_service.application.shipment.common.ShipmentReadyToShipOutboxService;
import com.twohands.commerce_service.application.shipment.common.ShipmentShippedOutboxService;
import com.twohands.commerce_service.domain.order.OrderBuyerRepository;
import com.twohands.commerce_service.domain.outbox.OutboxEvent;
import com.twohands.commerce_service.domain.outbox.OutboxEventRepository;
import com.twohands.commerce_service.domain.shipment.SellerShipmentRecord;
import com.twohands.commerce_service.domain.shipment.ShipmentCarrier;
import com.twohands.commerce_service.domain.shipment.ShipmentStatus;
import com.twohands.commerce_service.domain.shipping.ShipmentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShipmentLifecycleOutboxEmitterTest {

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Mock
    private ShipmentReadyToShipOutboxService shipmentReadyToShipOutboxService;

    @Mock
    private ShipmentShippedOutboxService shipmentShippedOutboxService;

    @Mock
    private ShipmentDeliveredOutboxService shipmentDeliveredOutboxService;

    @Mock
    private OrderBuyerRepository orderBuyerRepository;

    private ShipmentLifecycleOutboxEmitter emitter;

    private final UUID shipmentId = UUID.randomUUID();
    private final UUID orderId = UUID.randomUUID();
    private final UUID sellerId = UUID.randomUUID();
    private final UUID buyerId = UUID.randomUUID();
    private final Instant now = Instant.parse("2026-06-04T18:00:00Z");

    @BeforeEach
    void setUp() {
        emitter = new ShipmentLifecycleOutboxEmitter(
                outboxEventRepository,
                shipmentReadyToShipOutboxService,
                shipmentShippedOutboxService,
                shipmentDeliveredOutboxService,
                orderBuyerRepository
        );
    }

    @Test
    void emitsReadyToShipOutboxWhenStatusIsReadyToShip() {
        SellerShipmentRecord shipment = record(ShipmentStatus.PENDING, "TRK-1", null);
        when(orderBuyerRepository.findBuyerIdByOrderId(orderId)).thenReturn(Optional.of(buyerId));
        when(shipmentReadyToShipOutboxService.build(shipmentId, orderId, buyerId, sellerId, "TRK-1", now))
                .thenReturn(sampleOutbox(ShipmentReadyToShipOutboxService.EVENT_TYPE));

        emitter.emitDedicatedNotificationEvents(shipment, ShipmentStatus.READY_TO_SHIP, now, null);

        verify(outboxEventRepository).save(any(OutboxEvent.class));
        verify(shipmentShippedOutboxService, never()).build(any(), any(), any(), any(), any(), any());
        verify(shipmentDeliveredOutboxService, never()).build(any(), any(), any(), any(), any(), any());
    }

    @Test
    void emitsShippedOutboxWhenStatusIsShipped() {
        SellerShipmentRecord shipment = record(ShipmentStatus.READY_TO_SHIP, "TRK-1", null);
        when(orderBuyerRepository.findBuyerIdByOrderId(orderId)).thenReturn(Optional.of(buyerId));
        when(shipmentShippedOutboxService.build(shipmentId, orderId, buyerId, sellerId, "TRK-1", now))
                .thenReturn(sampleOutbox(ShipmentShippedOutboxService.EVENT_TYPE));

        emitter.emitDedicatedNotificationEvents(shipment, ShipmentStatus.SHIPPED, now, null);

        verify(outboxEventRepository).save(any(OutboxEvent.class));
        verify(shipmentDeliveredOutboxService, never()).build(any(), any(), any(), any(), any(), any());
    }

    @Test
    void emitsDeliveredOutboxWhenStatusIsDelivered() {
        SellerShipmentRecord shipment = record(ShipmentStatus.SHIPPED, "TRK-1", null);
        when(orderBuyerRepository.findBuyerIdByOrderId(orderId)).thenReturn(Optional.of(buyerId));
        when(shipmentDeliveredOutboxService.build(shipmentId, orderId, buyerId, sellerId, "TRK-1", now))
                .thenReturn(sampleOutbox(ShipmentDeliveredOutboxService.EVENT_TYPE));

        emitter.emitDedicatedNotificationEvents(shipment, ShipmentStatus.DELIVERED, now, null);

        verify(shipmentShippedOutboxService, never()).build(any(), any(), any(), any(), any(), any());
        verify(outboxEventRepository).save(any(OutboxEvent.class));
    }

    @Test
    void skipsWhenStatusIsNotNotificationMilestone() {
        SellerShipmentRecord shipment = record(ShipmentStatus.PENDING, null, null);

        emitter.emitDedicatedNotificationEvents(shipment, ShipmentStatus.PICKING_UP, now, null);

        verify(outboxEventRepository, never()).save(any());
        verify(orderBuyerRepository, never()).findBuyerIdByOrderId(any());
    }

    private SellerShipmentRecord record(ShipmentStatus status, String tracking, String ghnCode) {
        return new SellerShipmentRecord(
                shipmentId, orderId, sellerId,
                ShipmentCarrier.GHN, ShipmentType.STANDARD, status,
                ghnCode, tracking, BigDecimal.TEN, BigDecimal.ZERO, 500,
                null, null, null, now, now
        );
    }

    private OutboxEvent sampleOutbox(String eventType) {
        return new OutboxEvent(
                UUID.randomUUID(),
                eventType,
                "shipment:test",
                shipmentId,
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
