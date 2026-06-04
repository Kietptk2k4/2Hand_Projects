package com.twohands.commerce_service.application.shipment.createshipment;

import com.twohands.commerce_service.application.shipment.common.ShipmentCreatedOutboxService;
import com.twohands.commerce_service.domain.outbox.OutboxEventRepository;
import com.twohands.commerce_service.domain.shipment.CreateShipmentDraft;
import com.twohands.commerce_service.domain.shipment.CreateShipmentRepository;
import com.twohands.commerce_service.domain.shipment.CreateShipmentResult;
import com.twohands.commerce_service.domain.shipment.GhnCreateOrderResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class CreateShipmentTransactionService {

    private final CreateShipmentRepository createShipmentRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ShipmentCreatedOutboxService shipmentCreatedOutboxService;

    public CreateShipmentTransactionService(
            CreateShipmentRepository createShipmentRepository,
            OutboxEventRepository outboxEventRepository,
            ShipmentCreatedOutboxService shipmentCreatedOutboxService
    ) {
        this.createShipmentRepository = createShipmentRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.shipmentCreatedOutboxService = shipmentCreatedOutboxService;
    }

    @Transactional
    public CreateShipmentResult createLocal(CreateShipmentDraft draft, UUID buyerId, Instant occurredAt) {
        CreateShipmentResult created = createShipmentRepository.createShipment(draft, occurredAt);
        outboxEventRepository.save(shipmentCreatedOutboxService.build(
                created.shipmentId(),
                created.orderId(),
                buyerId,
                created.sellerId(),
                created.carrier(),
                created.orderItemIds(),
                draft.trackingNumber(),
                occurredAt
        ));
        return created;
    }

    @Transactional
    public void updateGhnFields(UUID shipmentId, GhnCreateOrderResult ghnResult, Instant occurredAt) {
        createShipmentRepository.updateGhnProviderFields(shipmentId, ghnResult, occurredAt);
    }
}
