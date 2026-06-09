package com.twohands.commerce_service.application.shipping.ghn;

import com.twohands.commerce_service.config.CommerceIntegrationProperties;
import com.twohands.commerce_service.domain.shipment.GhnAvailableServicesQuery;
import com.twohands.commerce_service.domain.shipment.GhnResolvedService;
import com.twohands.commerce_service.domain.shipment.GhnServiceCatalogGateway;
import com.twohands.commerce_service.domain.shipment.GhnServiceOption;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ResolveGhnServiceUseCase {

    private final GhnServiceCatalogGateway ghnServiceCatalogGateway;
    private final CommerceIntegrationProperties.Ghn ghnProperties;

    public ResolveGhnServiceUseCase(
            GhnServiceCatalogGateway ghnServiceCatalogGateway,
            CommerceIntegrationProperties integrationProperties
    ) {
        this.ghnServiceCatalogGateway = ghnServiceCatalogGateway;
        this.ghnProperties = integrationProperties.getGhn();
    }

    public Optional<GhnResolvedService> resolveFromCatalog(List<GhnServiceOption> services) {
        if (services == null || services.isEmpty()) {
            return Optional.empty();
        }

        Integer configuredServiceId = ghnProperties.getDefaultServiceId();
        if (configuredServiceId != null && configuredServiceId > 0) {
            Optional<GhnServiceOption> byId = services.stream()
                    .filter(option -> option.serviceId() == configuredServiceId)
                    .findFirst();
            if (byId.isPresent()) {
                return Optional.of(GhnResolvedService.fromOption(byId.get(), true));
            }
        }

        Integer configuredTypeId = ghnProperties.getDefaultServiceTypeId();
        if (configuredTypeId != null && configuredTypeId > 0) {
            Optional<GhnServiceOption> byType = services.stream()
                    .filter(option -> option.serviceTypeId() == configuredTypeId)
                    .findFirst();
            if (byType.isPresent()) {
                return Optional.of(GhnResolvedService.fromOption(byType.get(), true));
            }
        }

        return Optional.of(GhnResolvedService.fromOption(services.get(0), false));
    }

    public GhnResolvedService resolveForRoute(int fromDistrictId, int toDistrictId) {
        List<GhnServiceOption> services = ghnServiceCatalogGateway.listAvailableServices(
                new GhnAvailableServicesQuery(fromDistrictId, toDistrictId)
        );
        return resolveFromCatalog(services).orElseThrow(() -> new AppException(
                ErrorCode.GHN_PROVIDER_UNAVAILABLE,
                "No GHN service available for the given districts"
        ));
    }
}
