package com.twohands.commerce_service.unit.application.shipping.ghn;

import com.twohands.commerce_service.application.shipping.ghn.ResolveGhnServiceUseCase;
import com.twohands.commerce_service.config.CommerceIntegrationProperties;
import com.twohands.commerce_service.domain.shipment.GhnResolvedService;
import com.twohands.commerce_service.domain.shipment.GhnServiceCatalogGateway;
import com.twohands.commerce_service.domain.shipment.GhnServiceOption;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ResolveGhnServiceUseCaseTest {

    @Mock
    private GhnServiceCatalogGateway ghnServiceCatalogGateway;

    private CommerceIntegrationProperties properties;
    private ResolveGhnServiceUseCase useCase;

    @BeforeEach
    void setUp() {
        properties = new CommerceIntegrationProperties();
        properties.getGhn().setDefaultServiceTypeId(2);
        useCase = new ResolveGhnServiceUseCase(ghnServiceCatalogGateway, properties);
    }

    @Test
    void resolvesConfiguredServiceType() {
        List<GhnServiceOption> services = List.of(
                new GhnServiceOption(53319, 1, "Nhanh"),
                new GhnServiceOption(53320, 2, "Chuan")
        );

        Optional<GhnResolvedService> resolved = useCase.resolveFromCatalog(services);

        assertThat(resolved).isPresent();
        assertThat(resolved.get().serviceId()).isEqualTo(53320);
        assertThat(resolved.get().usedConfiguredDefault()).isTrue();
    }

    @Test
    void fallsBackToFirstServiceWhenDefaultTypeMissing() {
        properties.getGhn().setDefaultServiceTypeId(99);
        List<GhnServiceOption> services = List.of(
                new GhnServiceOption(53319, 1, "Nhanh")
        );

        Optional<GhnResolvedService> resolved = useCase.resolveFromCatalog(services);

        assertThat(resolved).isPresent();
        assertThat(resolved.get().serviceId()).isEqualTo(53319);
        assertThat(resolved.get().usedConfiguredDefault()).isFalse();
    }

    @Test
    void prefersConfiguredServiceIdOverType() {
        properties.getGhn().setDefaultServiceId(53319);
        properties.getGhn().setDefaultServiceTypeId(2);
        List<GhnServiceOption> services = List.of(
                new GhnServiceOption(53319, 1, "Nhanh"),
                new GhnServiceOption(53320, 2, "Chuan")
        );

        Optional<GhnResolvedService> resolved = useCase.resolveFromCatalog(services);

        assertThat(resolved).isPresent();
        assertThat(resolved.get().serviceId()).isEqualTo(53319);
        assertThat(resolved.get().usedConfiguredDefault()).isTrue();
    }
}
