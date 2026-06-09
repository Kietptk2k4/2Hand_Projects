package com.twohands.commerce_service.application.shipment.trackshipment;

import com.twohands.commerce_service.application.shipment.syncghnshipment.SyncGhnShipmentStatusUseCase;
import com.twohands.commerce_service.domain.shipment.TrackShipmentRepository;
import com.twohands.commerce_service.domain.shipment.TrackShipmentResult;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TrackShipmentUseCase {

    private final TrackShipmentRepository trackShipmentRepository;
    private final SyncGhnShipmentStatusUseCase syncGhnShipmentStatusUseCase;

    public TrackShipmentUseCase(
            TrackShipmentRepository trackShipmentRepository,
            SyncGhnShipmentStatusUseCase syncGhnShipmentStatusUseCase
    ) {
        this.trackShipmentRepository = trackShipmentRepository;
        this.syncGhnShipmentStatusUseCase = syncGhnShipmentStatusUseCase;
    }

    @Transactional
    public TrackShipmentResult execute(TrackShipmentCommand command) {
        syncGhnShipmentStatusUseCase.syncIfEligible(command.shipmentId(), command.userId());
        return trackShipmentRepository
                .findByShipmentIdAndUserId(command.shipmentId(), command.userId())
                .orElseThrow(() -> new AppException(ErrorCode.SHIPMENT_NOT_FOUND));
    }

    public String successMessage() {
        return "Lay thong tin tracking shipment thanh cong.";
    }
}
