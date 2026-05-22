package com.twohands.commerce_service.application.shipment.viewshippingaddresssnapshot;

import com.twohands.commerce_service.domain.shipment.ViewShippingAddressSnapshotRepository;
import com.twohands.commerce_service.domain.shipment.ViewShippingAddressSnapshotResult;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ViewShippingAddressSnapshotUseCase {

    private final ViewShippingAddressSnapshotRepository viewShippingAddressSnapshotRepository;

    public ViewShippingAddressSnapshotUseCase(
            ViewShippingAddressSnapshotRepository viewShippingAddressSnapshotRepository
    ) {
        this.viewShippingAddressSnapshotRepository = viewShippingAddressSnapshotRepository;
    }

    @Transactional(readOnly = true)
    public ViewShippingAddressSnapshotResult execute(ViewShippingAddressSnapshotCommand command) {
        return viewShippingAddressSnapshotRepository
                .findByShipmentIdAndUserId(command.shipmentId(), command.userId())
                .orElseThrow(() -> new AppException(ErrorCode.SHIPMENT_NOT_FOUND));
    }

    public String successMessage() {
        return "Lay shipping address snapshot thanh cong.";
    }
}
