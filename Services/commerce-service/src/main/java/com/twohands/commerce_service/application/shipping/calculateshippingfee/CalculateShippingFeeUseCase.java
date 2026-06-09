package com.twohands.commerce_service.application.shipping.calculateshippingfee;

import com.twohands.commerce_service.application.shipping.CartShippingQuoteContext;
import com.twohands.commerce_service.application.shipping.CartShippingQuoteLoader;
import com.twohands.commerce_service.application.shipping.SellerWeightGroup;
import com.twohands.commerce_service.application.shipping.ShippingFeeQuoteService;
import com.twohands.commerce_service.domain.shipping.SellerShippingProfile;
import com.twohands.commerce_service.domain.shipping.SellerShippingProfileRepository;
import com.twohands.commerce_service.domain.shipping.ShippingGroupFeeQuote;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class CalculateShippingFeeUseCase {

    private final CartShippingQuoteLoader cartShippingQuoteLoader;
    private final SellerShippingProfileRepository sellerShippingProfileRepository;
    private final ShippingFeeQuoteService shippingFeeQuoteService;

    public CalculateShippingFeeUseCase(
            CartShippingQuoteLoader cartShippingQuoteLoader,
            SellerShippingProfileRepository sellerShippingProfileRepository,
            ShippingFeeQuoteService shippingFeeQuoteService
    ) {
        this.cartShippingQuoteLoader = cartShippingQuoteLoader;
        this.sellerShippingProfileRepository = sellerShippingProfileRepository;
        this.shippingFeeQuoteService = shippingFeeQuoteService;
    }

    @Transactional(readOnly = true)
    public CalculateShippingFeeResult execute(CalculateShippingFeeCommand command) {
        CartShippingQuoteContext context = cartShippingQuoteLoader.load(
                command.userId(),
                command.cartItemIds(),
                command.addressId(),
                command.shipmentType()
        );

        Map<UUID, SellerShippingProfile> profiles = sellerShippingProfileRepository.findByShopIds(
                context.sellerGroups().stream().map(SellerWeightGroup::shopId).distinct().toList()
        );

        List<SellerShippingFeeGroupResult> sellerGroups = new ArrayList<>();
        BigDecimal totalShippingFee = BigDecimal.ZERO;

        for (SellerWeightGroup group : context.sellerGroups()) {
            SellerShippingProfile profile = profiles.get(group.shopId());
            if (profile == null) {
                throw new AppException(
                        ErrorCode.SHIPPING_PROFILE_MISSING,
                        "Seller shipping profile is missing for shop " + group.shopId()
                );
            }

            ShippingGroupFeeQuote quote = shippingFeeQuoteService.quoteGroup(
                    profile,
                    context.destinationAddress().provinceCode(),
                    context.destinationAddress().districtCode(),
                    context.destinationAddress().wardCode(),
                    group.totalWeightGram(),
                    context.shipmentType()
            );

            totalShippingFee = totalShippingFee.add(quote.shippingFee());

            sellerGroups.add(new SellerShippingFeeGroupResult(
                    group.sellerId(),
                    group.shopId(),
                    quote.shippingFee(),
                    quote.shippingFeeOrigin(),
                    quote.estimatedDeliveryDate(),
                    context.shipmentType()
            ));
        }

        return new CalculateShippingFeeResult(sellerGroups, totalShippingFee);
    }

    public String successMessage() {
        return "Tinh phi van chuyen thanh cong.";
    }
}
