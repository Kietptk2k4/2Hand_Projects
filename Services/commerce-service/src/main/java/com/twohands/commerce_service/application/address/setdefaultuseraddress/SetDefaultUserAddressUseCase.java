package com.twohands.commerce_service.application.address.setdefaultuseraddress;

import com.twohands.commerce_service.domain.address.OwnedUserAddress;
import com.twohands.commerce_service.domain.address.SetDefaultUserAddressRepository;
import com.twohands.commerce_service.domain.address.SetDefaultUserAddressResult;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

@Service
public class SetDefaultUserAddressUseCase {

    private final SetDefaultUserAddressRepository setDefaultUserAddressRepository;
    private final Clock clock;

    public SetDefaultUserAddressUseCase(
            SetDefaultUserAddressRepository setDefaultUserAddressRepository,
            Clock clock
    ) {
        this.setDefaultUserAddressRepository = setDefaultUserAddressRepository;
        this.clock = clock;
    }

    @Transactional
    public SetDefaultUserAddressResult execute(SetDefaultUserAddressCommand command) {
        OwnedUserAddress address = setDefaultUserAddressRepository
                .findOwnedAddress(command.addressId(), command.userId())
                .orElseThrow(() -> new AppException(ErrorCode.ADDRESS_NOT_FOUND));

        Instant now = clock.instant();
        return setDefaultUserAddressRepository.setDefault(address, now);
    }

    public String successMessage() {
        return "Dat dia chi mac dinh thanh cong.";
    }
}
