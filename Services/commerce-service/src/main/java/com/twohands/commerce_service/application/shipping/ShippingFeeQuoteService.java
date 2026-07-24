package com.twohands.commerce_service.application.shipping;

import com.twohands.commerce_service.application.shipping.ghn.ResolveGhnServiceUseCase;
import com.twohands.commerce_service.config.CommerceIntegrationProperties;
import com.twohands.commerce_service.domain.shipment.GhnAddressReadinessPolicy;
import com.twohands.commerce_service.domain.shipment.GhnLeadtimeGateway;
import com.twohands.commerce_service.domain.shipment.GhnLeadtimeQuery;
import com.twohands.commerce_service.domain.shipment.GhnLeadtimeResult;
import com.twohands.commerce_service.domain.shipment.GhnResolvedService;
import com.twohands.commerce_service.domain.shipment.GhnShippingFeeGateway;
import com.twohands.commerce_service.domain.shipment.GhnShippingFeeQuery;
import com.twohands.commerce_service.domain.shipment.GhnShippingFeeResult;
import com.twohands.commerce_service.domain.shipping.SellerShippingProfile;
import com.twohands.commerce_service.domain.shipping.ShipmentType;
import com.twohands.commerce_service.domain.shipping.ShippingDeliveryEstimator;
import com.twohands.commerce_service.domain.shipping.ShippingFeeRequest;
import com.twohands.commerce_service.domain.shipping.ShippingGroupFeeQuote;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import com.twohands.commerce_service.infrastructure.ghn.GhnDistrictIdParser;
import com.twohands.commerce_service.infrastructure.shipping.MockShippingFeeCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class ShippingFeeQuoteService {

    private static final Logger log = LoggerFactory.getLogger(ShippingFeeQuoteService.class);

    private final MockShippingFeeCalculator mockShippingFeeCalculator;
    private final CommerceIntegrationProperties integrationProperties;
    private final GhnShippingFeeGateway ghnShippingFeeGateway;
    private final GhnLeadtimeGateway ghnLeadtimeGateway;
    private final ResolveGhnServiceUseCase resolveGhnServiceUseCase;
    private final Clock clock;

    public ShippingFeeQuoteService(
            MockShippingFeeCalculator mockShippingFeeCalculator,
            CommerceIntegrationProperties integrationProperties,
            GhnShippingFeeGateway ghnShippingFeeGateway,
            GhnLeadtimeGateway ghnLeadtimeGateway,
            ResolveGhnServiceUseCase resolveGhnServiceUseCase,
            Clock clock
    ) {
        this.mockShippingFeeCalculator = mockShippingFeeCalculator;
        this.integrationProperties = integrationProperties;
        this.ghnShippingFeeGateway = ghnShippingFeeGateway;
        this.ghnLeadtimeGateway = ghnLeadtimeGateway;
        this.resolveGhnServiceUseCase = resolveGhnServiceUseCase;
        this.clock = clock;
    }

    public BigDecimal quoteGroupFee(
            SellerShippingProfile pickupProfile,
            String destinationProvinceCode,
            String destinationDistrictCode,
            String destinationWardCode,
            int totalWeightGram,
            ShipmentType shipmentType
    ) {
        return quoteGroup(
                pickupProfile,
                destinationProvinceCode,
                destinationDistrictCode,
                destinationWardCode,
                totalWeightGram,
                shipmentType
        ).shippingFee();
    }

    public ShippingGroupFeeQuote quoteGroup(
            SellerShippingProfile pickupProfile,
            String destinationProvinceCode,
            String destinationDistrictCode,
            String destinationWardCode,
            int totalWeightGram,
            ShipmentType shipmentType
    ) {
        CommerceIntegrationProperties.Ghn ghn = integrationProperties.getGhn();
        if (!ghn.isEnabled()) {
            return calculateMockQuote(
                    pickupProfile,
                    destinationProvinceCode,
                    destinationDistrictCode,
                    totalWeightGram,
                    shipmentType
            );
        }

        if (!ghn.isLiveClientConfigured()) {
            if (ghn.isMockFallbackEnabled()) {
                return calculateMockQuote(
                        pickupProfile,
                        destinationProvinceCode,
                        destinationDistrictCode,
                        totalWeightGram,
                        shipmentType
                );
            }
            throw new AppException(ErrorCode.GHN_PROVIDER_UNAVAILABLE, "GHN integration is not configured");
        }

        try {
            return quoteViaGhn(
                    pickupProfile,
                    destinationDistrictCode,
                    destinationWardCode,
                    totalWeightGram,
                    shipmentType
            );
        } catch (AppException ex) {
            if (ghn.isMockFallbackEnabled() && ex.getErrorCode() != ErrorCode.GHN_ADDRESS_NOT_READY) {
                log.warn("GHN fee quote failed ({}), falling back to mock: {}", ex.getErrorCode(), ex.getMessage());
                return calculateMockQuote(
                        pickupProfile,
                        destinationProvinceCode,
                        destinationDistrictCode,
                        totalWeightGram,
                        shipmentType
                );
            }
            throw ex;
        }
    }

    private ShippingGroupFeeQuote quoteViaGhn(
            SellerShippingProfile pickupProfile,
            String destinationDistrictCode,
            String destinationWardCode,
            int totalWeightGram,
            ShipmentType shipmentType
    ) {
        validateGhnAddresses(pickupProfile, destinationDistrictCode, destinationWardCode);

        int fromDistrictId = GhnDistrictIdParser.parseRequired(
                pickupProfile.districtCode(),
                "pickup district_code"
        );
        int toDistrictId = GhnDistrictIdParser.parseRequired(
                destinationDistrictCode,
                "destination district_code"
        );

        GhnResolvedService resolvedService = resolveGhnServiceUseCase.resolveForRoute(fromDistrictId, toDistrictId);

        CommerceIntegrationProperties.Ghn ghn = integrationProperties.getGhn();
        int weightGram = Math.max(totalWeightGram, 1);
        String fromWard = pickupProfile.wardCode().trim();
        String toWard = destinationWardCode.trim();

        GhnShippingFeeQuery feeQuery = new GhnShippingFeeQuery(
                fromDistrictId,
                fromWard,
                toDistrictId,
                toWard,
                weightGram,
                resolvedService.serviceId(),
                resolvedService.serviceTypeId(),
                ghn.getDefaultPackageLengthCm(),
                ghn.getDefaultPackageWidthCm(),
                ghn.getDefaultPackageHeightCm()
        );
        GhnLeadtimeQuery leadtimeQuery = new GhnLeadtimeQuery(
                fromDistrictId,
                fromWard,
                toDistrictId,
                toWard,
                resolvedService.serviceId()
        );

        // Sequential on the request thread: avoids blocking ForkJoinPool.commonPool()
        // under concurrent checkout quotes (fee must succeed; leadtime soft-fails).
        GhnShippingFeeResult feeResult = ghnShippingFeeGateway.calculateFee(feeQuery);
        ShipmentType resolvedType = shipmentType == null ? ShipmentType.STANDARD : shipmentType;
        LocalDate heuristicEta = ShippingDeliveryEstimator.estimateDeliveryDate(
                resolvedType,
                LocalDate.now(clock)
        );
        LocalDate leadtimeEta = fetchLeadtimeOrNull(leadtimeQuery);
        LocalDate estimatedDeliveryDate = leadtimeEta != null ? leadtimeEta : heuristicEta;
        BigDecimal fee = feeResult.totalFee();
        return new ShippingGroupFeeQuote(fee, fee, estimatedDeliveryDate);
    }

    private LocalDate fetchLeadtimeOrNull(GhnLeadtimeQuery query) {
        try {
            GhnLeadtimeResult result = ghnLeadtimeGateway.calculateLeadtime(query);
            if (result == null || result.estimatedDeliveryDate() == null) {
                log.warn("GHN leadtime returned empty date; falling back to heuristic ETA");
                return null;
            }
            return result.estimatedDeliveryDate();
        } catch (RuntimeException ex) {
            log.warn("GHN leadtime failed; falling back to heuristic ETA: {}", ex.getMessage());
            return null;
        }
    }

    private void validateGhnAddresses(
            SellerShippingProfile pickupProfile,
            String destinationDistrictCode,
            String destinationWardCode
    ) {
        List<String> issues = new ArrayList<>();
        issues.addAll(GhnAddressReadinessPolicy.validateDistrictAndWard(
                pickupProfile.districtCode(),
                pickupProfile.wardCode()
        ));
        issues.addAll(GhnAddressReadinessPolicy.validateDistrictAndWard(
                destinationDistrictCode,
                destinationWardCode
        ));
        if (!issues.isEmpty()) {
            throw new AppException(
                    ErrorCode.GHN_ADDRESS_NOT_READY,
                    String.join("; ", issues)
            );
        }
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
