package com.twohands.commerce_service.application.address.createuseraddress;

import com.twohands.commerce_service.domain.address.CreateUserAddressDraft;
import com.twohands.commerce_service.domain.address.CreateUserAddressRepository;
import com.twohands.commerce_service.domain.address.CreateUserAddressResult;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class CreateUserAddressUseCase {

    private static final int RECEIVER_NAME_MAX_LENGTH = 255;
    private static final int PHONE_MAX_LENGTH = 50;
    private static final int LOCATION_CODE_MAX_LENGTH = 50;
    private static final Pattern PHONE_PATTERN = Pattern.compile("^(0|\\+84)?[0-9]{9,10}$");

    private final CreateUserAddressRepository createUserAddressRepository;
    private final Clock clock;

    public CreateUserAddressUseCase(
            CreateUserAddressRepository createUserAddressRepository,
            Clock clock
    ) {
        this.createUserAddressRepository = createUserAddressRepository;
        this.clock = clock;
    }

    @Transactional
    public CreateUserAddressResult execute(CreateUserAddressCommand command) {
        validatePayload(command);

        boolean firstAddress = !createUserAddressRepository.hasAnyAddress(command.userId());
        boolean isDefault = firstAddress || Boolean.TRUE.equals(command.isDefault());

        Instant now = clock.instant();
        return createUserAddressRepository.create(
                new CreateUserAddressDraft(
                        command.userId(),
                        command.receiverName().trim(),
                        normalizePhone(command.phone()),
                        command.provinceCode().trim(),
                        command.districtCode().trim(),
                        command.wardCode().trim(),
                        command.addressDetail().trim(),
                        isDefault
                ),
                now
        );
    }

    public String successMessage() {
        return "Them dia chi giao hang thanh cong.";
    }

    private void validatePayload(CreateUserAddressCommand command) {
        requireText(command.receiverName(), "receiver_name");
        if (command.receiverName().trim().length() > RECEIVER_NAME_MAX_LENGTH) {
            throw fieldError("receiver_name", "must be at most " + RECEIVER_NAME_MAX_LENGTH + " characters");
        }
        requireText(command.phone(), "phone");
        String phone = normalizePhone(command.phone());
        if (phone.length() > PHONE_MAX_LENGTH) {
            throw fieldError("phone", "must be at most " + PHONE_MAX_LENGTH + " characters");
        }
        if (!PHONE_PATTERN.matcher(phone).matches()) {
            throw new AppException(ErrorCode.INVALID_PHONE, "Phone number format is invalid");
        }
        requireText(command.provinceCode(), "province_code");
        requireText(command.districtCode(), "district_code");
        requireText(command.wardCode(), "ward_code");
        requireText(command.addressDetail(), "address_detail");
        validateLocationCode(command.provinceCode(), "province_code");
        validateLocationCode(command.districtCode(), "district_code");
        validateLocationCode(command.wardCode(), "ward_code");
    }

    private void validateLocationCode(String value, String field) {
        if (value.trim().length() > LOCATION_CODE_MAX_LENGTH) {
            throw fieldError(field, "must be at most " + LOCATION_CODE_MAX_LENGTH + " characters");
        }
    }

    private String normalizePhone(String phone) {
        return phone.trim().replaceAll("\\s+", "");
    }

    private void requireText(String value, String field) {
        if (!StringUtils.hasText(value)) {
            throw fieldError(field, "must not be blank");
        }
    }

    private AppException fieldError(String field, String reason) {
        return new AppException(ErrorCode.VALIDATION_ERROR, "Validation failed", field, reason);
    }
}
