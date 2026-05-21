package com.twohands.commerce_service.application.shipping;

import com.twohands.commerce_service.config.CommerceIntegrationProperties;
import com.twohands.commerce_service.domain.shipping.SellerShippingProfile;
import com.twohands.commerce_service.domain.shipping.ShipmentType;
import com.twohands.commerce_service.domain.shipping.ShippingFeeRequest;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import com.twohands.commerce_service.infrastructure.shipping.MockShippingFeeCalculator;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class ShippingFeeQuoteService {

    private final MockShippingFeeCalculator mockShippingFeeCalculator;
    private final CommerceIntegrationProperties integrationProperties;

    public ShippingFeeQuoteService(
            MockShippingFeeCalculator mockShippingFeeCalculator,
            CommerceIntegrationProperties integrationProperties
    ) {
        this.mockShippingFeeCalculator = mockShippingFeeCalculator;
        this.integrationProperties = integrationProperties;
    }

    public BigDecimal quoteGroupFee(
            SellerShippingProfile pickupProfile,
            String destinationProvinceCode,
            String destinationDistrictCode,
            int totalWeightGram,
            ShipmentType shipmentType
    ) {
        if (integrationProperties.getGhn().isEnabled()) {
            if (integrationProperties.getGhn().isMockFallbackEnabled()) {
                return calculateMock(
                        pickupProfile,
                        destinationProvinceCode,
                        destinationDistrictCode,
                        totalWeightGram,
                        shipmentType
                );
            }
            throw new AppException(
                    ErrorCode.SHIPPING_PROVIDER_UNAVAILABLE,
                    "GHN fee API is not integrated yet"
            );
        }
        return calculateMock(
                pickupProfile,
                destinationProvinceCode,
                destinationDistrictCode,
                totalWeightGram,
                shipmentType
        );
    }

    private BigDecimal calculateMock(
            SellerShippingProfile pickupProfile,
            String destinationProvinceCode,
            String destinationDistrictCode,
            int totalWeightGram,
            ShipmentType shipmentType
    ) {
        return mockShippingFeeCalculator.calculate(new ShippingFeeRequest(
                pickupProfile,
                destinationProvinceCode,
                destinationDistrictCode,
                totalWeightGram,
                shipmentType == null ? ShipmentType.STANDARD : shipmentType
        ));
    }
}
