package com.twohands.commerce_service.unit.application.address;

import com.twohands.commerce_service.application.address.viewuseraddresses.ViewUserAddressesCommand;
import com.twohands.commerce_service.application.address.viewuseraddresses.ViewUserAddressesUseCase;
import com.twohands.commerce_service.domain.address.UserAddressListItem;
import com.twohands.commerce_service.domain.address.ViewUserAddressesRepository;
import com.twohands.commerce_service.domain.address.ViewUserAddressesResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ViewUserAddressesUseCaseTest {

    @Mock
    private ViewUserAddressesRepository viewUserAddressesRepository;

    private ViewUserAddressesUseCase useCase;

    private final UUID userId = UUID.randomUUID();
    private final Instant now = Instant.parse("2026-05-21T10:00:00Z");

    @BeforeEach
    void setUp() {
        useCase = new ViewUserAddressesUseCase(viewUserAddressesRepository);
    }

    @Test
    void shouldReturnUserAddresses() {
        UUID defaultId = UUID.randomUUID();
        UUID otherId = UUID.randomUUID();
        when(viewUserAddressesRepository.findByUserId(userId)).thenReturn(new ViewUserAddressesResult(List.of(
                new UserAddressListItem(
                        defaultId,
                        "Default User",
                        "0901111111",
                        "79",
                        "760",
                        "26734",
                        "Default street",
                        true,
                        now,
                        now
                ),
                new UserAddressListItem(
                        otherId,
                        "Other User",
                        "0902222222",
                        "01",
                        "001",
                        "00001",
                        "Other street",
                        false,
                        now.minusSeconds(60),
                        now.minusSeconds(30)
                )
        )));

        ViewUserAddressesResult result = useCase.execute(new ViewUserAddressesCommand(userId));

        assertThat(result.addresses()).hasSize(2);
        assertThat(result.addresses().getFirst().isDefault()).isTrue();
        assertThat(result.addresses().getFirst().id()).isEqualTo(defaultId);
    }

    @Test
    void shouldReturnEmptyListWhenNoAddresses() {
        when(viewUserAddressesRepository.findByUserId(userId))
                .thenReturn(new ViewUserAddressesResult(List.of()));

        ViewUserAddressesResult result = useCase.execute(new ViewUserAddressesCommand(userId));

        assertThat(result.addresses()).isEmpty();
    }
}
