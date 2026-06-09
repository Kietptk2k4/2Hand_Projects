package com.twohands.commerce_service.domain.shipment;

public record GhnResolvedService(
        int serviceId,
        int serviceTypeId,
        String shortName,
        boolean usedConfiguredDefault
) {
    public static GhnResolvedService fromOption(GhnServiceOption option, boolean usedConfiguredDefault) {
        return new GhnResolvedService(
                option.serviceId(),
                option.serviceTypeId(),
                option.shortName(),
                usedConfiguredDefault
        );
    }
}
