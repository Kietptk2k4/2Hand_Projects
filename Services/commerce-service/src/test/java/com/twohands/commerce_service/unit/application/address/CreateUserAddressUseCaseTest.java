package com.twohands.commerce_service.unit.application.address;

import com.twohands.commerce_service.application.address.createuseraddress.CreateUserAddressCommand;
import com.twohands.commerce_service.application.address.createuseraddress.CreateUserAddressUseCase;
import com.twohands.commerce_service.domain.address.CreateUserAddressDraft;
import com.twohands.commerce_service.domain.address.CreateUserAddressRepository;
import com.twohands.commerce_service.domain.address.CreateUserAddressResult;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateUserAddressUseCaseTest {

    @Mock
    private CreateUserAddressRepository createUserAddressRepository;

    private CreateUserAddressUseCase useCase;

    private final UUID userId = UUID.randomUUID();
    private final UUID addressId = UUID.randomUUID();
    private final Instant now = Instant.parse("2026-05-21T14:00:00Z");

    @BeforeEach
    void setUp() {
        useCase = new CreateUserAddressUseCase(
                createUserAddressRepository,
                Clock.fixed(now, ZoneOffset.UTC)
        );
    }

    @Test
    void shouldAutoDefaultFirstAddress() {
        when(createUserAddressRepository.hasAnyAddress(userId)).thenReturn(false);
        when(createUserAddressRepository.create(any(CreateUserAddressDraft.class), eq(now)))
                .thenReturn(createdResult(true));

        CreateUserAddressResult result = useCase.execute(validCommand(false));

        assertThat(result.isDefault()).isTrue();
        ArgumentCaptor<CreateUserAddressDraft> captor = ArgumentCaptor.forClass(CreateUserAddressDraft.class);
        verify(createUserAddressRepository).create(captor.capture(), eq(now));
        assertThat(captor.getValue().isDefault()).isTrue();
    }

    @Test
    void shouldSetDefaultWhenRequested() {
        when(createUserAddressRepository.hasAnyAddress(userId)).thenReturn(true);
        when(createUserAddressRepository.create(any(CreateUserAddressDraft.class), eq(now)))
                .thenReturn(createdResult(true));

        useCase.execute(validCommand(true));

        ArgumentCaptor<CreateUserAddressDraft> captor = ArgumentCaptor.forClass(CreateUserAddressDraft.class);
        verify(createUserAddressRepository).create(captor.capture(), eq(now));
        assertThat(captor.getValue().isDefault()).isTrue();
    }

    @Test
    void shouldNotDefaultWhenNotFirstAndNotRequested() {
        when(createUserAddressRepository.hasAnyAddress(userId)).thenReturn(true);
        when(createUserAddressRepository.create(any(CreateUserAddressDraft.class), eq(now)))
                .thenReturn(createdResult(false));

        useCase.execute(validCommand(false));

        ArgumentCaptor<CreateUserAddressDraft> captor = ArgumentCaptor.forClass(CreateUserAddressDraft.class);
        verify(createUserAddressRepository).create(captor.capture(), eq(now));
        assertThat(captor.getValue().isDefault()).isFalse();
    }

    @Test
    void shouldRejectInvalidPhone() {
        assertThatThrownBy(() -> useCase.execute(new CreateUserAddressCommand(
                userId,
                "Nguyen Van A",
                "abc",
                "79",
                "760",
                "26734",
                "123 Street",
                false
        )))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_PHONE);
    }

    @Test
    void shouldRejectBlankReceiverName() {
        assertThatThrownBy(() -> useCase.execute(new CreateUserAddressCommand(
                userId,
                "  ",
                "0901234567",
                "79",
                "760",
                "26734",
                "123 Street",
                false
        )))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.VALIDATION_ERROR);
    }

    private CreateUserAddressCommand validCommand(boolean isDefault) {
        return new CreateUserAddressCommand(
                userId,
                "Nguyen Van A",
                "0901234567",
                "79",
                "760",
                "26734",
                "123 Nguyen Van Linh",
                isDefault
        );
    }

    private CreateUserAddressResult createdResult(boolean isDefault) {
        return new CreateUserAddressResult(
                addressId,
                userId,
                "Nguyen Van A",
                "0901234567",
                "79",
                "760",
                "26734",
                "123 Nguyen Van Linh",
                isDefault,
                now,
                now
        );
    }
}
