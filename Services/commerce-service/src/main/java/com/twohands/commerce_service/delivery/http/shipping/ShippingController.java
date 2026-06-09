package com.twohands.commerce_service.delivery.http.shipping;

import com.twohands.commerce_service.application.shipping.calculateshippingfee.CalculateShippingFeeCommand;
import com.twohands.commerce_service.application.shipping.calculateshippingfee.CalculateShippingFeeResult;
import com.twohands.commerce_service.application.shipping.calculateshippingfee.CalculateShippingFeeUseCase;
import com.twohands.commerce_service.application.shipping.calculateshippingfee.SellerShippingFeeGroupResult;
import com.twohands.commerce_service.application.shipping.ghn.ViewGhnAvailableServicesUseCase;
import com.twohands.commerce_service.application.shipping.ghn.ViewGhnDistrictsUseCase;
import com.twohands.commerce_service.application.shipping.ghn.ViewGhnProvincesUseCase;
import com.twohands.commerce_service.application.shipping.ghn.ViewGhnWardsUseCase;
import com.twohands.commerce_service.common.dto.ApiResponse;
import com.twohands.commerce_service.delivery.http.shipping.GhnAddressMasterDataResponses.ViewGhnDistrictsResponse;
import com.twohands.commerce_service.delivery.http.shipping.GhnAddressMasterDataResponses.ViewGhnProvincesResponse;
import com.twohands.commerce_service.delivery.http.shipping.GhnAddressMasterDataResponses.ViewGhnWardsResponse;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import com.twohands.commerce_service.security.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/commerce/api/v1/shipping")
public class ShippingController {

    private final CalculateShippingFeeUseCase calculateShippingFeeUseCase;
    private final ViewGhnAvailableServicesUseCase viewGhnAvailableServicesUseCase;
    private final ViewGhnProvincesUseCase viewGhnProvincesUseCase;
    private final ViewGhnDistrictsUseCase viewGhnDistrictsUseCase;
    private final ViewGhnWardsUseCase viewGhnWardsUseCase;

    public ShippingController(
            CalculateShippingFeeUseCase calculateShippingFeeUseCase,
            ViewGhnAvailableServicesUseCase viewGhnAvailableServicesUseCase,
            ViewGhnProvincesUseCase viewGhnProvincesUseCase,
            ViewGhnDistrictsUseCase viewGhnDistrictsUseCase,
            ViewGhnWardsUseCase viewGhnWardsUseCase
    ) {
        this.calculateShippingFeeUseCase = calculateShippingFeeUseCase;
        this.viewGhnAvailableServicesUseCase = viewGhnAvailableServicesUseCase;
        this.viewGhnProvincesUseCase = viewGhnProvincesUseCase;
        this.viewGhnDistrictsUseCase = viewGhnDistrictsUseCase;
        this.viewGhnWardsUseCase = viewGhnWardsUseCase;
    }

    @GetMapping("/ghn/provinces")
    public ResponseEntity<ApiResponse<ViewGhnProvincesResponse>> viewGhnProvinces(Authentication authentication) {
        resolveUserId(authentication);
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                viewGhnProvincesUseCase.successMessage(),
                ViewGhnProvincesResponse.from(viewGhnProvincesUseCase.execute())
        ));
    }

    @GetMapping("/ghn/districts")
    public ResponseEntity<ApiResponse<ViewGhnDistrictsResponse>> viewGhnDistricts(
            @RequestParam("province_id") int provinceId,
            Authentication authentication
    ) {
        resolveUserId(authentication);
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                viewGhnDistrictsUseCase.successMessage(),
                ViewGhnDistrictsResponse.from(viewGhnDistrictsUseCase.execute(provinceId))
        ));
    }

    @GetMapping("/ghn/wards")
    public ResponseEntity<ApiResponse<ViewGhnWardsResponse>> viewGhnWards(
            @RequestParam("district_id") int districtId,
            Authentication authentication
    ) {
        resolveUserId(authentication);
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                viewGhnWardsUseCase.successMessage(),
                ViewGhnWardsResponse.from(viewGhnWardsUseCase.execute(districtId))
        ));
    }

    @PostMapping("/ghn/available-services")
    public ResponseEntity<ApiResponse<ViewGhnAvailableServicesResponse>> viewGhnAvailableServices(
            @RequestBody @Valid ViewGhnAvailableServicesRequest request,
            Authentication authentication
    ) {
        resolveUserId(authentication);
        var result = viewGhnAvailableServicesUseCase.execute(
                new ViewGhnAvailableServicesUseCase.ViewGhnAvailableServicesCommand(
                        request.fromDistrictId(),
                        request.toDistrictId()
                )
        );

        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                viewGhnAvailableServicesUseCase.successMessage(),
                ViewGhnAvailableServicesResponse.from(result)
        ));
    }

    @PostMapping("/fee")
    public ResponseEntity<ApiResponse<CalculateShippingFeeResponse>> calculateShippingFee(
            @RequestBody @Valid CalculateShippingFeeRequest request,
            Authentication authentication
    ) {
        UUID userId = resolveUserId(authentication);
        CalculateShippingFeeResult result = calculateShippingFeeUseCase.execute(
                new CalculateShippingFeeCommand(
                        userId,
                        request.cartItemIds(),
                        request.addressId(),
                        request.shipmentType()
                )
        );

        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                calculateShippingFeeUseCase.successMessage(),
                toResponse(result)
        ));
    }

    private UUID resolveUserId(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser principal)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        return principal.userId();
    }

    private CalculateShippingFeeResponse toResponse(CalculateShippingFeeResult result) {
        return new CalculateShippingFeeResponse(
                result.sellerGroups().stream().map(this::toGroupResponse).toList(),
                result.totalShippingFee()
        );
    }

    private SellerShippingFeeGroupResponse toGroupResponse(SellerShippingFeeGroupResult group) {
        return new SellerShippingFeeGroupResponse(
                group.sellerId(),
                group.shopId(),
                group.shippingFee(),
                group.shippingFeeOrigin(),
                group.estimatedDeliveryDate(),
                group.shipmentType()
        );
    }
}
