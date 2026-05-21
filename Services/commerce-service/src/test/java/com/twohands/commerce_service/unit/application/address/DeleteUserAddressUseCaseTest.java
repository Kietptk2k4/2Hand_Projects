package com.twohands.commerce_service.unit.application.address;

import com.twohands.commerce_service.application.address.deleteuseraddress.DeleteUserAddressCommand;
import com.twohands.commerce_service.application.address.deleteuseraddress.DeleteUserAddressUseCase;
import com.twohands.commerce_service.domain.address.DeleteUserAddressRepository;
import com.twohands.commerce_service.domain.address.DeleteUserAddressResult;
import com.twohands.commerce_service.domain.address.OwnedUserAddress;
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
class DeleteUserAddressUseCaseTest {

    @Mock
    private DeleteUserAddressRepository deleteUserAddressRepository;

    private DeleteUserAddressUseCase useCase;

    private final UUID userId = UUID.randomUUID();
    private final UUID addressId = UUID.randomUUID();
    private final UUID otherDefaultId = UUID.randomUUID();
    private final Instant now = Instant.parse("2026-05-21T15:00:00Z");

    @BeforeEach
    void setUp() {
        useCase = new DeleteUserAddressUseCase(
                deleteUserAddressRepository,
                Clock.fixed(now, ZoneOffset.UTC)
        );
    }

    @Test
    void shouldDeleteNonDefaultAddress() {
        OwnedUserAddress owned = new OwnedUserAddress(addressId, userId, false);
        when(deleteUserAddressRepository.findOwnedAddress(addressId, userId)).thenReturn(Optional.of(owned));
        when(deleteUserAddressRepository.delete(owned, now))
                .thenReturn(new DeleteUserAddressResult(addressId, userId, false, null, now));

        DeleteUserAddressResult result = useCase.execute(new DeleteUserAddressCommand(userId, addressId));

        assertThat(result.wasDefault()).isFalse();
        assertThat(result.newDefaultAddressId()).isNull();
    }

    @Test
    void shouldReassignDefaultWhenDeletingDefaultAddress() {
        OwnedUserAddress owned = new OwnedUserAddress(addressId, userId, true);
        when(deleteUserAddressRepository.findOwnedAddress(addressId, userId)).thenReturn(Optional.of(owned));
        when(deleteUserAddressRepository.delete(owned, now))
                .thenReturn(new DeleteUserAddressResult(addressId, userId, true, otherDefaultId, now));

        DeleteUserAddressResult result = useCase.execute(new DeleteUserAddressCommand(userId, addressId));

        assertThat(result.wasDefault()).isTrue();
        assertThat(result.newDefaultAddressId()).isEqualTo(otherDefaultId);
    }

    @Test
    void shouldRejectWhenAddressNotFound() {
        when(deleteUserAddressRepository.findOwnedAddress(addressId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(new DeleteUserAddressCommand(userId, addressId)))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.ADDRESS_NOT_FOUND);

        verify(deleteUserAddressRepository, never()).delete(eq(owned(addressId)), eq(now));
    }

    private OwnedUserAddress owned(UUID id) {
        return new OwnedUserAddress(id, userId, false);
    }
}
