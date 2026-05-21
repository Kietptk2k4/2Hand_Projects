package com.twohands.commerce_service.infrastructure.shipping;

import com.twohands.commerce_service.domain.shipping.ShipmentType;
import com.twohands.commerce_service.domain.shipping.ShippingFeeCalculator;
import com.twohands.commerce_service.domain.shipping.ShippingFeeRequest;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Deterministic MVP shipping fee estimate when GHN is disabled or as fallback.
 */
@Component
public class MockShippingFeeCalculator implements ShippingFeeCalculator {

    private static final BigDecimal BASE_FEE = BigDecimal.valueOf(30_000);
    private static final BigDecimal FEE_PER_KG = BigDecimal.valueOf(5_000);
    private static final BigDecimal INTER_PROVINCE_SURCHARGE = BigDecimal.valueOf(10_000);

    @Override
    public BigDecimal calculate(ShippingFeeRequest request) {
        int weightGram = Math.max(request.totalWeightGram(), 0);
        BigDecimal weightKg = BigDecimal.valueOf(weightGram)
                .divide(BigDecimal.valueOf(1000), 0, RoundingMode.CEILING);

        BigDecimal fee = BASE_FEE.add(weightKg.multiply(FEE_PER_KG));

        if (!request.pickup().provinceCode().equals(request.destinationProvinceCode())) {
            fee = fee.add(INTER_PROVINCE_SURCHARGE);
        }

        fee = fee.multiply(shipmentTypeMultiplier(request.shipmentType()));
        return fee.setScale(0, RoundingMode.HALF_UP);
    }

    private BigDecimal shipmentTypeMultiplier(ShipmentType shipmentType) {
        if (shipmentType == null) {
            return BigDecimal.ONE;
        }
        return switch (shipmentType) {
            case EXPRESS -> BigDecimal.valueOf(1.5);
            case SAME_DAY -> BigDecimal.valueOf(2);
            default -> BigDecimal.ONE;
        };
    }
}
