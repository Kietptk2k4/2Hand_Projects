package com.twohands.commerce_service.unit.application.address;

import com.twohands.commerce_service.application.address.setdefaultuseraddress.SetDefaultUserAddressCommand;
import com.twohands.commerce_service.application.address.setdefaultuseraddress.SetDefaultUserAddressUseCase;
import com.twohands.commerce_service.domain.address.OwnedUserAddress;
import com.twohands.commerce_service.domain.address.SetDefaultUserAddressRepository;
import com.twohands.commerce_service.domain.address.SetDefaultUserAddressResult;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SetDefaultUserAddressUseCaseTest {

    @Mock
    private SetDefaultUserAddressRepository setDefaultUserAddressRepository;

    private SetDefaultUserAddressUseCase useCase;

    private final UUID userId = UUID.randomUUID();
    private final UUID addressId = UUID.randomUUID();
    private final Instant now = Instant.parse("2026-05-21T15:30:00Z");

    @BeforeEach
    void setUp() {
        useCase = new SetDefaultUserAddressUseCase(
                setDefaultUserAddressRepository,
                Clock.fixed(now, ZoneOffset.UTC)
        );
    }

    @Test
    void shouldSetDefaultAddressForOwner() {
        OwnedUserAddress owned = new OwnedUserAddress(addressId, userId, false);
        SetDefaultUserAddressResult expected = sampleResult();
        when(setDefaultUserAddressRepository.findOwnedAddress(addressId, userId)).thenReturn(Optional.of(owned));
        when(setDefaultUserAddressRepository.setDefault(owned, now)).thenReturn(expected);

        SetDefaultUserAddressResult result = useCase.execute(new SetDefaultUserAddressCommand(userId, addressId));

        assertThat(result.isDefault()).isTrue();
        assertThat(result.addressId()).isEqualTo(addressId);
        verify(setDefaultUserAddressRepository).setDefault(owned, now);
    }

    @Test
    void shouldAllowSettingAlreadyDefaultAddress() {
        OwnedUserAddress owned = new OwnedUserAddress(addressId, userId, true);
        when(setDefaultUserAddressRepository.findOwnedAddress(addressId, userId)).thenReturn(Optional.of(owned));
        when(setDefaultUserAddressRepository.setDefault(owned, now)).thenReturn(sampleResult());

        useCase.execute(new SetDefaultUserAddressCommand(userId, addressId));

        verify(setDefaultUserAddressRepository).setDefault(owned, now);
    }

    @Test
    void shouldRejectWhenAddressNotFoundOrNotOwned() {
        when(setDefaultUserAddressRepository.findOwnedAddress(addressId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(new SetDefaultUserAddressCommand(userId, addressId)))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.ADDRESS_NOT_FOUND);

        verify(setDefaultUserAddressRepository, never()).setDefault(eq(owned(addressId)), eq(now));
    }

    private OwnedUserAddress owned(UUID id) {
        return new OwnedUserAddress(id, userId, false);
    }

    private SetDefaultUserAddressResult sampleResult() {
        return new SetDefaultUserAddressResult(
                addressId,
                userId,
                "Nguyen Van A",
                "0901234567",
                "79",
                "760",
                "26734",
                "123 Nguyen Van Linh",
                true,
                now.minusSeconds(3600),
                now
        );
    }
}
