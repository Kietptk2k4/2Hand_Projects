package com.twohands.commerce_service.application.shipment.viewghnprintlabel;

import com.twohands.commerce_service.config.CommerceIntegrationProperties;
import com.twohands.commerce_service.domain.shipment.GhnPrintLabelGateway;
import com.twohands.commerce_service.domain.shipment.ManageSellerShipmentRepository;
import com.twohands.commerce_service.domain.shipment.SellerShipmentRecord;
import com.twohands.commerce_service.domain.shipment.ShipmentCarrier;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.UUID;

@Service
public class ViewGhnPrintLabelUseCase {

    private final ManageSellerShipmentRepository manageSellerShipmentRepository;
    private final GhnPrintLabelGateway ghnPrintLabelGateway;
    private final CommerceIntegrationProperties.Ghn ghnProperties;

    public ViewGhnPrintLabelUseCase(
            ManageSellerShipmentRepository manageSellerShipmentRepository,
            GhnPrintLabelGateway ghnPrintLabelGateway,
            CommerceIntegrationProperties integrationProperties
    ) {
        this.manageSellerShipmentRepository = manageSellerShipmentRepository;
        this.ghnPrintLabelGateway = ghnPrintLabelGateway;
        this.ghnProperties = integrationProperties.getGhn();
    }

    public ViewGhnPrintLabelResult execute(ViewGhnPrintLabelCommand command) {
        SellerShipmentRecord shipment = manageSellerShipmentRepository.findShipmentForSeller(
                        command.shipmentId(),
                        command.sellerId()
                )
                .orElseThrow(() -> new AppException(ErrorCode.SHIPMENT_NOT_FOUND));

        if (shipment.carrier() != ShipmentCarrier.GHN) {
            throw new AppException(ErrorCode.INVALID_SHIPMENT_CARRIER, "Shipment is not a GHN carrier");
        }
        if (!StringUtils.hasText(shipment.ghnOrderCode())) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Shipment has no GHN order code");
        }

        String token = ghnPrintLabelGateway.generatePrintToken(shipment.ghnOrderCode());
        String format = normalizeFormat(command.format());
        String printUrl = buildPrintUrl(token, format);

        return new ViewGhnPrintLabelResult(
                shipment.shipmentId(),
                shipment.ghnOrderCode(),
                format,
                token,
                printUrl,
                ghnProperties.getPrintTokenTtlMinutes()
        );
    }

    public String successMessage() {
        return "Tao link in van don GHN thanh cong.";
    }

    private String normalizeFormat(String rawFormat) {
        if (!StringUtils.hasText(rawFormat)) {
            return "a5";
        }
        return switch (rawFormat.trim().toLowerCase()) {
            case "a5", "80x80", "52x70" -> rawFormat.trim().toLowerCase();
            default -> throw new AppException(
                    ErrorCode.VALIDATION_ERROR,
                    "format must be one of: a5, 80x80, 52x70"
            );
        };
    }

    private String buildPrintUrl(String token, String format) {
        String path = switch (format) {
            case "80x80" -> "print80x80";
            case "52x70" -> "print52x70";
            default -> "printA5";
        };
        return ghnProperties.getPrintBaseUrl() + "/" + path + "?token=" + token;
    }

    public record ViewGhnPrintLabelCommand(UUID sellerId, UUID shipmentId, String format) {
    }

    public record ViewGhnPrintLabelResult(
            UUID shipmentId,
            String ghnOrderCode,
            String format,
            String printToken,
            String printUrl,
            int expiresInMinutes
    ) {
    }
}
