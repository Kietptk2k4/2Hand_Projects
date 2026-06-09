package com.twohands.commerce_service.application.shipment.syncghnshipment;

import com.twohands.commerce_service.application.shipment.common.GhnShipmentStatusUpdateResult;
import com.twohands.commerce_service.application.shipment.common.GhnShipmentStatusUpdateService;
import com.twohands.commerce_service.config.CommerceIntegrationProperties;
import com.twohands.commerce_service.domain.shipment.GhnOrderInfoGateway;
import com.twohands.commerce_service.domain.shipment.GhnOrderInfoResult;
import com.twohands.commerce_service.domain.shipment.ProcessGhnWebhookRepository;
import com.twohands.commerce_service.domain.shipment.SellerShipmentRecord;
import com.twohands.commerce_service.domain.shipment.ShipmentStatus;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import com.twohands.commerce_service.infrastructure.ghn.GhnTrackSyncCooldownRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Optional;
import java.util.UUID;

@Service
public class SyncGhnShipmentStatusUseCase {

    private static final Logger log = LoggerFactory.getLogger(SyncGhnShipmentStatusUseCase.class);

    private final CommerceIntegrationProperties.Ghn ghnProperties;
    private final ProcessGhnWebhookRepository processGhnWebhookRepository;
    private final GhnOrderInfoGateway ghnOrderInfoGateway;
    private final GhnShipmentStatusUpdateService ghnShipmentStatusUpdateService;
    private final GhnTrackSyncCooldownRegistry trackSyncCooldownRegistry;

    public SyncGhnShipmentStatusUseCase(
            CommerceIntegrationProperties integrationProperties,
            ProcessGhnWebhookRepository processGhnWebhookRepository,
            GhnOrderInfoGateway ghnOrderInfoGateway,
            GhnShipmentStatusUpdateService ghnShipmentStatusUpdateService,
            GhnTrackSyncCooldownRegistry trackSyncCooldownRegistry
    ) {
        this.ghnProperties = integrationProperties.getGhn();
        this.processGhnWebhookRepository = processGhnWebhookRepository;
        this.ghnOrderInfoGateway = ghnOrderInfoGateway;
        this.ghnShipmentStatusUpdateService = ghnShipmentStatusUpdateService;
        this.trackSyncCooldownRegistry = trackSyncCooldownRegistry;
    }

    @Transactional
    public SyncGhnShipmentResult execute(SyncGhnShipmentCommand command) {
        if (!ghnProperties.isEnabled() || !ghnProperties.isLiveClientConfigured()) {
            throw new AppException(ErrorCode.GHN_PROVIDER_UNAVAILABLE, "GHN integration is not configured");
        }

        Optional<SellerShipmentRecord> shipmentOptional =
                processGhnWebhookRepository.findGhnShipmentForUserUpdate(command.shipmentId(), command.userId());
        if (shipmentOptional.isEmpty()) {
            throw new AppException(ErrorCode.SHIPMENT_NOT_FOUND);
        }

        SellerShipmentRecord shipment = shipmentOptional.get();
        if (shipment.status() == ShipmentStatus.DELIVERED
                || shipment.status() == ShipmentStatus.CANCELLED
                || shipment.status() == ShipmentStatus.RETURNED) {
            return SyncGhnShipmentResult.skipped(
                    shipment.shipmentId(),
                    shipment.status(),
                    "Shipment is already terminal"
            );
        }

        if (!trackSyncCooldownRegistry.shouldSync(command.shipmentId(), command.force())) {
            return SyncGhnShipmentResult.skipped(
                    shipment.shipmentId(),
                    shipment.status(),
                    "GHN sync cooldown active"
            );
        }

        try {
            GhnOrderInfoResult orderInfo = fetchOrderInfo(shipment);
            GhnShipmentStatusUpdateResult updateResult = ghnShipmentStatusUpdateService.apply(
                    shipment,
                    orderInfo.rawStatus(),
                    orderInfo.orderCode()
            );
            trackSyncCooldownRegistry.markSynced(command.shipmentId());
            ShipmentStatus status = updateResult.applied() ? updateResult.newStatus() : shipment.status();
            return SyncGhnShipmentResult.synced(command.shipmentId(), status);
        } catch (AppException ex) {
            if (ex.getErrorCode() == ErrorCode.GHN_PROVIDER_UNAVAILABLE) {
                log.warn("GHN order-info sync failed for shipment {}: {}", command.shipmentId(), ex.getMessage());
                throw ex;
            }
            throw ex;
        }
    }

    public Optional<ShipmentStatus> syncIfEligible(UUID shipmentId, UUID userId) {
        try {
            SyncGhnShipmentResult result = execute(new SyncGhnShipmentCommand(shipmentId, userId, false));
            if (result.skipped()) {
                return Optional.ofNullable(result.status());
            }
            return Optional.of(result.status());
        } catch (AppException ex) {
            if (ex.getErrorCode() == ErrorCode.GHN_PROVIDER_UNAVAILABLE) {
                return Optional.empty();
            }
            throw ex;
        }
    }

    public String successMessage() {
        return "Dong bo trang thai GHN thanh cong.";
    }

    private GhnOrderInfoResult fetchOrderInfo(SellerShipmentRecord shipment) {
        if (StringUtils.hasText(shipment.ghnOrderCode())) {
            return ghnOrderInfoGateway.fetchByOrderCode(shipment.ghnOrderCode());
        }
        return ghnOrderInfoGateway.fetchByClientOrderCode(shipment.shipmentId().toString());
    }
}
