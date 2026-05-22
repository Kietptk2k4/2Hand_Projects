package com.twohands.commerce_service.application.shipment.trackshipment;

import com.twohands.commerce_service.domain.shipment.TrackShipmentRepository;
import com.twohands.commerce_service.domain.shipment.TrackShipmentResult;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TrackShipmentUseCase {

    private final TrackShipmentRepository trackShipmentRepository;

    public TrackShipmentUseCase(TrackShipmentRepository trackShipmentRepository) {
        this.trackShipmentRepository = trackShipmentRepository;
    }

    @Transactional(readOnly = true)
    public TrackShipmentResult execute(TrackShipmentCommand command) {
        return trackShipmentRepository
                .findByShipmentIdAndUserId(command.shipmentId(), command.userId())
                .orElseThrow(() -> new AppException(ErrorCode.SHIPMENT_NOT_FOUND));
    }

    public String successMessage() {
        return "Lay thong tin tracking shipment thanh cong.";
    }
}
