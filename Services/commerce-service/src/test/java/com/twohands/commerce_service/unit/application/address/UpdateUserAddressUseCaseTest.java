package com.twohands.commerce_service.unit.application.address;

import com.twohands.commerce_service.application.address.updateuseraddress.UpdateUserAddressCommand;
import com.twohands.commerce_service.application.address.updateuseraddress.UpdateUserAddressUseCase;
import com.twohands.commerce_service.domain.address.UpdateUserAddressDraft;
import com.twohands.commerce_service.domain.address.UpdateUserAddressRepository;
import com.twohands.commerce_service.domain.address.UpdateUserAddressResult;
import com.twohands.commerce_service.domain.address.UpdateUserAddressSnapshot;
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
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateUserAddressUseCaseTest {

    @Mock
    private UpdateUserAddressRepository updateUserAddressRepository;

    private UpdateUserAddressUseCase useCase;

    private final UUID userId = UUID.randomUUID();
    private final UUID addressId = UUID.randomUUID();
    private final Instant now = Instant.parse("2026-05-21T14:00:00Z");

    @BeforeEach
    void setUp() {
        useCase = new UpdateUserAddressUseCase(
                updateUserAddressRepository,
                Clock.fixed(now, ZoneOffset.UTC)
        );
    }

    @Test
    void shouldMergePartialUpdateAndPersist() {
        when(updateUserAddressRepository.findByIdAndUserId(addressId, userId))
                .thenReturn(Optional.of(existingSnapshot()));
        when(updateUserAddressRepository.update(org.mockito.ArgumentMatchers.any(UpdateUserAddressDraft.class), eq(now)))
                .thenReturn(updatedResult("Tran Thi B", "0912345678", false));

        UpdateUserAddressResult result = useCase.execute(new UpdateUserAddressCommand(
                userId,
                addressId,
                "Tran Thi B",
                null,
                null,
                null,
                null,
                null,
                null
        ));

        assertThat(result.receiverName()).isEqualTo("Tran Thi B");
        ArgumentCaptor<UpdateUserAddressDraft> captor = ArgumentCaptor.forClass(UpdateUserAddressDraft.class);
        verify(updateUserAddressRepository).update(captor.capture(), eq(now));
        assertThat(captor.getValue().receiverName()).isEqualTo("Tran Thi B");
        assertThat(captor.getValue().phone()).isEqualTo("0901234567");
        assertThat(captor.getValue().isDefault()).isFalse();
    }

    @Test
    void shouldSetDefaultWhenRequested() {
        when(updateUserAddressRepository.findByIdAndUserId(addressId, userId))
                .thenReturn(Optional.of(existingSnapshot()));
        when(updateUserAddressRepository.update(org.mockito.ArgumentMatchers.any(UpdateUserAddressDraft.class), eq(now)))
                .thenReturn(updatedResult("Nguyen Van A", "0901234567", true));

        useCase.execute(new UpdateUserAddressCommand(
                userId,
                addressId,
                null,
                null,
                null,
                null,
                null,
                null,
                true
        ));

        ArgumentCaptor<UpdateUserAddressDraft> captor = ArgumentCaptor.forClass(UpdateUserAddressDraft.class);
        verify(updateUserAddressRepository).update(captor.capture(), eq(now));
        assertThat(captor.getValue().isDefault()).isTrue();
    }

    @Test
    void shouldRejectEmptyPatch() {
        assertThatThrownBy(() -> useCase.execute(new UpdateUserAddressCommand(
                userId,
                addressId,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        )))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.VALIDATION_ERROR);
    }

    @Test
    void shouldRejectAddressNotFound() {
        when(updateUserAddressRepository.findByIdAndUserId(addressId, userId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(new UpdateUserAddressCommand(
                userId,
                addressId,
                "New Name",
                null,
                null,
                null,
                null,
                null,
                null
        )))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.ADDRESS_NOT_FOUND);
    }

    @Test
    void shouldRejectInvalidPhoneOnUpdate() {
        when(updateUserAddressRepository.findByIdAndUserId(addressId, userId))
                .thenReturn(Optional.of(existingSnapshot()));

        assertThatThrownBy(() -> useCase.execute(new UpdateUserAddressCommand(
                userId,
                addressId,
                null,
                "not-a-phone",
                null,
                null,
                null,
                null,
                null
        )))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_PHONE);
    }

    @Test
    void shouldRejectBlankMergedReceiverName() {
        when(updateUserAddressRepository.findByIdAndUserId(addressId, userId))
                .thenReturn(Optional.of(existingSnapshot()));

        assertThatThrownBy(() -> useCase.execute(new UpdateUserAddressCommand(
                userId,
                addressId,
                "   ",
                null,
                null,
                null,
                null,
                null,
                null
        )))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.VALIDATION_ERROR);
    }

    private UpdateUserAddressSnapshot existingSnapshot() {
        return new UpdateUserAddressSnapshot(
                addressId,
                userId,
                "Nguyen Van A",
                "0901234567",
                "79",
                "760",
                "26734",
                "123 Nguyen Van Linh",
                false
        );
    }

    private UpdateUserAddressResult updatedResult(String receiverName, String phone, boolean isDefault) {
        return new UpdateUserAddressResult(
                addressId,
                userId,
                receiverName,
                phone,
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
