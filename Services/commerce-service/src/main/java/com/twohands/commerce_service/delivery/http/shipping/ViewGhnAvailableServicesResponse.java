package com.twohands.commerce_service.delivery.http.shipping;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.twohands.commerce_service.application.shipping.ghn.ViewGhnAvailableServicesUseCase.ViewGhnAvailableServicesResult;

import java.util.List;

public record ViewGhnAvailableServicesResponse(
        @JsonProperty("services") List<GhnServiceOptionResponse> services,
        @JsonProperty("resolved_service") GhnResolvedServiceResponse resolvedService,
        @JsonProperty("configured_default_service_type_id") Integer configuredDefaultServiceTypeId,
        @JsonProperty("configured_default_service_id") Integer configuredDefaultServiceId
) {
    public static ViewGhnAvailableServicesResponse from(ViewGhnAvailableServicesResult result) {
        return new ViewGhnAvailableServicesResponse(
                result.services().stream().map(GhnServiceOptionResponse::from).toList(),
                result.resolvedService() != null
                        ? GhnResolvedServiceResponse.from(result.resolvedService())
                        : null,
                result.configuredDefaultServiceTypeId(),
                result.configuredDefaultServiceId()
        );
    }
}
