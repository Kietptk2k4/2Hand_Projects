package com.twohands.commerce_service.application.shipping;

import com.twohands.commerce_service.config.CommerceIntegrationProperties;
import com.twohands.commerce_service.domain.shipping.SellerShippingProfile;
import com.twohands.commerce_service.domain.shipping.ShipmentType;
import com.twohands.commerce_service.domain.shipping.ShippingDeliveryEstimator;
import com.twohands.commerce_service.domain.shipping.ShippingFeeRequest;
import com.twohands.commerce_service.domain.shipping.ShippingGroupFeeQuote;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import com.twohands.commerce_service.infrastructure.shipping.MockShippingFeeCalculator;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;

@Service
public class ShippingFeeQuoteService {

    private final MockShippingFeeCalculator mockShippingFeeCalculator;
    private final CommerceIntegrationProperties integrationProperties;
    private final Clock clock;

    public ShippingFeeQuoteService(
            MockShippingFeeCalculator mockShippingFeeCalculator,
            CommerceIntegrationProperties integrationProperties,
            Clock clock
    ) {
        this.mockShippingFeeCalculator = mockShippingFeeCalculator;
        this.integrationProperties = integrationProperties;
        this.clock = clock;
    }

    public BigDecimal quoteGroupFee(
            SellerShippingProfile pickupProfile,
            String destinationProvinceCode,
            String destinationDistrictCode,
            int totalWeightGram,
            ShipmentType shipmentType
    ) {
        return quoteGroup(
                pickupProfile,
                destinationProvinceCode,
                destinationDistrictCode,
                totalWeightGram,
                shipmentType
        ).shippingFee();
    }

    public ShippingGroupFeeQuote quoteGroup(
            SellerShippingProfile pickupProfile,
            String destinationProvinceCode,
            String destinationDistrictCode,
            int totalWeightGram,
            ShipmentType shipmentType
    ) {
        if (integrationProperties.getGhn().isEnabled() && !integrationProperties.getGhn().isMockFallbackEnabled()) {
            throw new AppException(
                    ErrorCode.SHIPPING_PROVIDER_UNAVAILABLE,
                    "GHN fee API is not integrated yet"
            );
        }
        return calculateMockQuote(
                pickupProfile,
                destinationProvinceCode,
                destinationDistrictCode,
                totalWeightGram,
                shipmentType
        );
    }

    private ShippingGroupFeeQuote calculateMockQuote(
            SellerShippingProfile pickupProfile,
            String destinationProvinceCode,
            String destinationDistrictCode,
            int totalWeightGram,
            ShipmentType shipmentType
    ) {
        ShipmentType resolvedType = shipmentType == null ? ShipmentType.STANDARD : shipmentType;
        BigDecimal fee = mockShippingFeeCalculator.calculate(new ShippingFeeRequest(
                pickupProfile,
                destinationProvinceCode,
                destinationDistrictCode,
                totalWeightGram,
                resolvedType
        ));
        LocalDate estimatedDeliveryDate = ShippingDeliveryEstimator.estimateDeliveryDate(
                resolvedType,
                LocalDate.now(clock)
        );
        return new ShippingGroupFeeQuote(fee, fee, estimatedDeliveryDate);
    }
}
