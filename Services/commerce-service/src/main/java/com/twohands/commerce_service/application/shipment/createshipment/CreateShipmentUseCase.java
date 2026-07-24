package com.twohands.commerce_service.application.shipment.createshipment;

import com.twohands.commerce_service.application.shipping.ghn.ResolveGhnServiceUseCase;
import com.twohands.commerce_service.config.CommerceIntegrationProperties;
import com.twohands.commerce_service.domain.payment.PaymentMethod;
import com.twohands.commerce_service.domain.payment.PaymentStatus;
import com.twohands.commerce_service.domain.shipment.BuyerDeliveryAddress;
import com.twohands.commerce_service.domain.shipment.CreateShipmentDraft;
import com.twohands.commerce_service.domain.shipment.CreateShipmentOrderContext;
import com.twohands.commerce_service.domain.shipment.CreateShipmentRepository;
import com.twohands.commerce_service.domain.shipment.CreateShipmentResult;
import com.twohands.commerce_service.domain.shipment.GhnAddressReadinessPolicy;
import com.twohands.commerce_service.domain.shipment.GhnCreateOrderCommand;
import com.twohands.commerce_service.domain.shipment.GhnCreateOrderItem;
import com.twohands.commerce_service.domain.shipment.GhnCreateOrderResult;
import com.twohands.commerce_service.domain.shipment.GhnResolvedService;
import com.twohands.commerce_service.domain.shipment.GhnShipmentGateway;
import com.twohands.commerce_service.domain.shipment.SellerPickupAddress;
import com.twohands.commerce_service.domain.shipment.ShipmentCarrier;
import com.twohands.commerce_service.domain.shipment.ShipmentOrderItemLine;
import com.twohands.commerce_service.domain.shipping.SellerShippingProfileRepository;
import com.twohands.commerce_service.domain.shipping.ShipmentType;
import com.twohands.commerce_service.domain.shipping.ShippingDeliveryEstimator;
import com.twohands.commerce_service.domain.shop.SellerShop;
import com.twohands.commerce_service.domain.shop.SellerShopRepository;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import com.twohands.commerce_service.infrastructure.ghn.GhnDistrictIdParser;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class CreateShipmentUseCase {

    private final CreateShipmentRepository createShipmentRepository;
    private final SellerShopRepository sellerShopRepository;
    private final SellerShippingProfileRepository sellerShippingProfileRepository;
    private final GhnShipmentGateway ghnShipmentGateway;
    private final ResolveGhnServiceUseCase resolveGhnServiceUseCase;
    private final CommerceIntegrationProperties.Ghn ghnProperties;
    private final CreateShipmentTransactionService createShipmentTransactionService;
    private final Clock clock;

    public CreateShipmentUseCase(
            CreateShipmentRepository createShipmentRepository,
            SellerShopRepository sellerShopRepository,
            SellerShippingProfileRepository sellerShippingProfileRepository,
            GhnShipmentGateway ghnShipmentGateway,
            ResolveGhnServiceUseCase resolveGhnServiceUseCase,
            CommerceIntegrationProperties integrationProperties,
            CreateShipmentTransactionService createShipmentTransactionService,
            Clock clock
    ) {
        this.createShipmentRepository = createShipmentRepository;
        this.sellerShopRepository = sellerShopRepository;
        this.sellerShippingProfileRepository = sellerShippingProfileRepository;
        this.ghnShipmentGateway = ghnShipmentGateway;
        this.resolveGhnServiceUseCase = resolveGhnServiceUseCase;
        this.ghnProperties = integrationProperties.getGhn();
        this.createShipmentTransactionService = createShipmentTransactionService;
        this.clock = clock;
    }

    public CreateShipmentResult execute(CreateShipmentCommand command) {
        validateCommand(command);

        ShipmentCarrier carrier = parseCarrier(command.carrier());
        ShipmentType shipmentType = parseShipmentType(command.shipmentType());

        CreateShipmentOrderContext order = createShipmentRepository.findOrderContext(command.orderId())
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        if (!"PROCESSING".equals(order.orderStatus())) {
            throw new AppException(ErrorCode.ORDER_NOT_PROCESSING);
        }

        validatePayment(order);

        List<UUID> requestedItemIds = distinctItemIds(command.orderItemIds());
        List<ShipmentOrderItemLine> items = createShipmentRepository.findOrderItemsForSeller(
                command.orderId(),
                command.sellerId(),
                requestedItemIds
        );

        if (items.size() != requestedItemIds.size()) {
            if (items.isEmpty()) {
                throw new AppException(ErrorCode.ORDER_ITEM_NOT_OWNED);
            }
            throw new AppException(ErrorCode.ORDER_ITEM_NOT_FOUND);
        }

        for (ShipmentOrderItemLine item : items) {
            if (item.hasShipment()) {
                throw new AppException(ErrorCode.ORDER_ITEM_ALREADY_SHIPPED);
            }
            if (!item.isFulfillableStatus()) {
                throw new AppException(
                        ErrorCode.ORDER_NOT_PROCESSING,
                        "Order item must be PENDING or PROCESSING to ship"
                );
            }
        }

        SellerShop shop = sellerShopRepository.findBySellerId(command.sellerId())
                .orElseThrow(() -> new AppException(ErrorCode.SELLER_SHOP_NOT_FOUND));

        if (!sellerShippingProfileRepository.findByShopIds(List.of(shop.id())).containsKey(shop.id())) {
            throw new AppException(ErrorCode.SHIPPING_PROFILE_MISSING);
        }

        SellerPickupAddress pickup = createShipmentRepository.findSellerPickupBySellerId(command.sellerId())
                .orElseThrow(() -> new AppException(ErrorCode.SHIPPING_PROFILE_MISSING));

        BuyerDeliveryAddress deliveryAddress = createShipmentRepository.findBuyerDeliveryAddress(order.buyerId())
                .orElseThrow(() -> new AppException(ErrorCode.BUYER_ADDRESS_NOT_FOUND));

        int totalWeightGram = resolveWeight(command.weightGram(), items);
        BigDecimal shippingFee = items.stream()
                .map(ShipmentOrderItemLine::shippingFeeAllocated)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal codAmount = order.paymentMethod() == PaymentMethod.COD
                ? items.stream().map(ShipmentOrderItemLine::finalPrice).reduce(BigDecimal.ZERO, BigDecimal::add)
                .add(shippingFee)
                : BigDecimal.ZERO;

        LocalDate estimatedDeliveryDate = ShippingDeliveryEstimator.estimateDeliveryDate(
                shipmentType,
                LocalDate.now(clock)
        );

        Instant now = clock.instant();
        UUID shipmentId = UUID.randomUUID();
        String trackingNumber = normalizeTracking(command.trackingNumber(), carrier);

        CreateShipmentDraft draft = new CreateShipmentDraft(
                shipmentId,
                command.orderId(),
                command.sellerId(),
                carrier,
                shipmentType,
                totalWeightGram,
                shippingFee,
                codAmount,
                estimatedDeliveryDate,
                trackingNumber,
                requestedItemIds,
                deliveryAddress
        );

        CreateShipmentResult created = createShipmentTransactionService.createLocal(draft, order.buyerId(), now);

        if (carrier == ShipmentCarrier.GHN) {
            validateGhnAddresses(pickup, deliveryAddress);
            created = registerGhnShipment(
                    created,
                    pickup,
                    deliveryAddress,
                    items,
                    totalWeightGram,
                    codAmount,
                    now
            );
        }

        return created;
    }

    private void validateGhnAddresses(SellerPickupAddress pickup, BuyerDeliveryAddress delivery) {
        List<String> issues = new ArrayList<>();
        issues.addAll(GhnAddressReadinessPolicy.validateDistrictAndWard(
                pickup.districtCode(),
                pickup.wardCode()
        ));
        issues.addAll(GhnAddressReadinessPolicy.validateDistrictAndWard(
                delivery.districtCode(),
                delivery.wardCode()
        ));
        if (!issues.isEmpty()) {
            throw new AppException(
                    ErrorCode.GHN_ADDRESS_NOT_READY,
                    String.join("; ", issues)
            );
        }
    }

    private CreateShipmentResult registerGhnShipment(
            CreateShipmentResult local,
            SellerPickupAddress pickup,
            BuyerDeliveryAddress delivery,
            List<ShipmentOrderItemLine> items,
            int totalWeightGram,
            BigDecimal codAmount,
            Instant occurredAt
    ) {
        int fromDistrictId = GhnDistrictIdParser.parseRequired(pickup.districtCode(), "pickup district_code");
        int toDistrictId = GhnDistrictIdParser.parseRequired(delivery.districtCode(), "destination district_code");
        GhnResolvedService resolvedService = resolveGhnServiceUseCase.resolveForRoute(fromDistrictId, toDistrictId);

        GhnCreateOrderCommand ghnCommand = new GhnCreateOrderCommand(
                local.shipmentId(),
                local.orderId(),
                codAmount.intValue(),
                totalWeightGram,
                resolvedService.serviceId(),
                resolvedService.serviceTypeId(),
                ghnProperties.getDefaultPackageLengthCm(),
                ghnProperties.getDefaultPackageWidthCm(),
                ghnProperties.getDefaultPackageHeightCm(),
                delivery.receiverName(),
                delivery.phone(),
                delivery.districtCode(),
                delivery.wardCode(),
                delivery.addressDetail(),
                pickup.pickupName(),
                pickup.phone(),
                pickup.districtCode(),
                pickup.wardCode(),
                pickup.addressDetail(),
                buildParcelContent(items),
                toGhnItems(items)
        );

        GhnCreateOrderResult ghnResult;
        try {
            ghnResult = ghnShipmentGateway.createOrder(ghnCommand);
        } catch (AppException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            throw new AppException(ErrorCode.GHN_PROVIDER_UNAVAILABLE, "GHN provider unavailable", ex);
        }

        createShipmentTransactionService.updateGhnFields(local.shipmentId(), ghnResult, occurredAt);

        LocalDate estimatedDeliveryDate = ghnResult.expectedDeliveryDate() != null
                ? ghnResult.expectedDeliveryDate()
                : local.estimatedDeliveryDate();

        return new CreateShipmentResult(
                local.shipmentId(),
                local.orderId(),
                local.sellerId(),
                local.carrier(),
                local.shipmentType(),
                local.status(),
                ghnResult.ghnOrderCode(),
                ghnResult.trackingNumber() != null ? ghnResult.trackingNumber() : local.trackingNumber(),
                local.shippingFee(),
                local.codAmount(),
                local.weightGram(),
                estimatedDeliveryDate,
                local.orderItemIds(),
                local.createdAt()
        );
    }

    public String successMessage() {
        return "Tao shipment thanh cong.";
    }

    private void validateCommand(CreateShipmentCommand command) {
        if (command.orderId() == null) {
            throw validationError("order_id", "must not be null");
        }
        if (command.orderItemIds() == null || command.orderItemIds().isEmpty()) {
            throw validationError("order_item_ids", "must not be empty");
        }
        if (!StringUtils.hasText(command.carrier())) {
            throw validationError("carrier", "must not be blank");
        }
        if (!StringUtils.hasText(command.shipmentType())) {
            throw validationError("shipment_type", "must not be blank");
        }
    }

    private void validatePayment(CreateShipmentOrderContext order) {
        if (order.paymentMethod() == PaymentMethod.PAYOS && order.paymentStatus() != PaymentStatus.PAID) {
            throw new AppException(
                    ErrorCode.INVALID_PAYMENT_STATE,
                    "PayOS order must be PAID before creating shipment"
            );
        }
    }

    private ShipmentCarrier parseCarrier(String carrier) {
        try {
            return ShipmentCarrier.valueOf(carrier.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new AppException(ErrorCode.INVALID_SHIPMENT_CARRIER, "Invalid carrier: " + carrier, ex);
        }
    }

    private ShipmentType parseShipmentType(String shipmentType) {
        try {
            return ShipmentType.valueOf(shipmentType.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new AppException(ErrorCode.INVALID_SHIPMENT_TYPE, "Invalid shipment type: " + shipmentType, ex);
        }
    }

    private List<UUID> distinctItemIds(List<UUID> orderItemIds) {
        Set<UUID> unique = new HashSet<>(orderItemIds);
        if (unique.size() != orderItemIds.size()) {
            throw validationError("order_item_ids", "must not contain duplicates");
        }
        return List.copyOf(unique);
    }

    private int resolveWeight(Integer requestedWeight, List<ShipmentOrderItemLine> items) {
        if (requestedWeight != null) {
            if (requestedWeight <= 0) {
                throw validationError("weight_gram", "must be greater than 0");
            }
            return requestedWeight;
        }
        int total = items.stream().mapToInt(ShipmentOrderItemLine::weightGram).sum();
        if (total <= 0) {
            throw validationError("weight_gram", "could not be derived from products");
        }
        return total;
    }

    private String normalizeTracking(String trackingNumber, ShipmentCarrier carrier) {
        if (carrier == ShipmentCarrier.GHN) {
            return null;
        }
        if (!StringUtils.hasText(trackingNumber)) {
            return null;
        }
        return trackingNumber.trim();
    }

    private List<GhnCreateOrderItem> toGhnItems(List<ShipmentOrderItemLine> items) {
        return items.stream()
                .map(item -> new GhnCreateOrderItem(
                        item.productNameSnapshot(),
                        StringUtils.hasText(item.skuSnapshot())
                                ? item.skuSnapshot().trim()
                                : item.productId().toString(),
                        item.quantity(),
                        item.unitPriceSnapshot().intValue(),
                        Math.max(item.weightGram(), 1)
                ))
                .toList();
    }

    private String buildParcelContent(List<ShipmentOrderItemLine> items) {
        String content = items.stream()
                .map(ShipmentOrderItemLine::productNameSnapshot)
                .filter(StringUtils::hasText)
                .reduce((left, right) -> left + ", " + right)
                .orElse("2Hands order");
        return content.length() > 500 ? content.substring(0, 500) : content;
    }

    private AppException validationError(String field, String detail) {
        return new AppException(ErrorCode.VALIDATION_ERROR, "Validation failed", field, detail);
    }
}
