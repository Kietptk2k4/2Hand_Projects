package com.twohands.commerce_service.application.address.deleteuseraddress;

import com.twohands.commerce_service.domain.address.DeleteUserAddressRepository;
import com.twohands.commerce_service.domain.address.DeleteUserAddressResult;
import com.twohands.commerce_service.domain.address.OwnedUserAddress;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

@Service
public class DeleteUserAddressUseCase {

    private final DeleteUserAddressRepository deleteUserAddressRepository;
    private final Clock clock;

    public DeleteUserAddressUseCase(
            DeleteUserAddressRepository deleteUserAddressRepository,
            Clock clock
    ) {
        this.deleteUserAddressRepository = deleteUserAddressRepository;
        this.clock = clock;
    }

    @Transactional
    public DeleteUserAddressResult execute(DeleteUserAddressCommand command) {
        OwnedUserAddress address = deleteUserAddressRepository
                .findOwnedAddress(command.addressId(), command.userId())
                .orElseThrow(() -> new AppException(ErrorCode.ADDRESS_NOT_FOUND));

        Instant now = clock.instant();
        return deleteUserAddressRepository.delete(address, now);
    }

    public String successMessage() {
        return "Xoa dia chi giao hang thanh cong.";
    }
}
