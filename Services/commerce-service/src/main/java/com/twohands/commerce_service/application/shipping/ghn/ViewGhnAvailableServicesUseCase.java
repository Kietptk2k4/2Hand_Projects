package com.twohands.commerce_service.application.shipping.ghn;

import com.twohands.commerce_service.config.CommerceIntegrationProperties;
import com.twohands.commerce_service.domain.shipment.GhnAvailableServicesQuery;
import com.twohands.commerce_service.domain.shipment.GhnResolvedService;
import com.twohands.commerce_service.domain.shipment.GhnServiceCatalogGateway;
import com.twohands.commerce_service.domain.shipment.GhnServiceOption;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ViewGhnAvailableServicesUseCase {

    private final GhnServiceCatalogGateway ghnServiceCatalogGateway;
    private final ResolveGhnServiceUseCase resolveGhnServiceUseCase;
    private final CommerceIntegrationProperties.Ghn ghnProperties;

    public ViewGhnAvailableServicesUseCase(
            GhnServiceCatalogGateway ghnServiceCatalogGateway,
            ResolveGhnServiceUseCase resolveGhnServiceUseCase,
            CommerceIntegrationProperties integrationProperties
    ) {
        this.ghnServiceCatalogGateway = ghnServiceCatalogGateway;
        this.resolveGhnServiceUseCase = resolveGhnServiceUseCase;
        this.ghnProperties = integrationProperties.getGhn();
    }

    public ViewGhnAvailableServicesResult execute(ViewGhnAvailableServicesCommand command) {
        List<GhnServiceOption> services = ghnServiceCatalogGateway.listAvailableServices(
                new GhnAvailableServicesQuery(command.fromDistrictId(), command.toDistrictId())
        );
        Optional<GhnResolvedService> resolved = resolveGhnServiceUseCase.resolveFromCatalog(services);
        return new ViewGhnAvailableServicesResult(
                services,
                resolved.orElse(null),
                ghnProperties.getDefaultServiceTypeId(),
                ghnProperties.getDefaultServiceId()
        );
    }

    public String successMessage() {
        return "Lay danh sach dich vu GHN thanh cong.";
    }

    public record ViewGhnAvailableServicesCommand(int fromDistrictId, int toDistrictId) {
    }

    public record ViewGhnAvailableServicesResult(
            List<GhnServiceOption> services,
            GhnResolvedService resolvedService,
            Integer configuredDefaultServiceTypeId,
            Integer configuredDefaultServiceId
    ) {
    }
}
