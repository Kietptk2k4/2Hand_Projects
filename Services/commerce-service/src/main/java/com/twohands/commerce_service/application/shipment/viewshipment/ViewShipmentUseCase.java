package com.twohands.commerce_service.application.shipment.viewshipment;

import com.twohands.commerce_service.domain.shipment.ViewShipmentRepository;
import com.twohands.commerce_service.domain.shipment.ViewShipmentResult;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ViewShipmentUseCase {

    private final ViewShipmentRepository viewShipmentRepository;

    public ViewShipmentUseCase(ViewShipmentRepository viewShipmentRepository) {
        this.viewShipmentRepository = viewShipmentRepository;
    }

    @Transactional(readOnly = true)
    public ViewShipmentResult execute(ViewShipmentCommand command) {
        return viewShipmentRepository
                .findByShipmentIdAndUserId(command.shipmentId(), command.userId())
                .orElseThrow(() -> new AppException(ErrorCode.SHIPMENT_NOT_FOUND));
    }

    public String successMessage() {
        return "Lay chi tiet shipment thanh cong.";
    }
}
